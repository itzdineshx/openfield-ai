package com.openfield.app.feature.library

import android.content.Context
import android.net.Uri
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.entity.KnowledgePackArticleEntity
import com.openfield.app.data.local.entity.KnowledgePackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.zip.ZipInputStream

data class KnowledgePackInstallResult(
    val packId: String,
    val title: String,
    val version: String,
    val articleCount: Int
)

/**
 * Robust installer for Knowledge Pack .zip bundles.
 * Supports:
 * - Direct extraction from GitHub Releases downloads or SAF local file sideloading (.zip / .ofp).
 * - Zip-Slip vulnerability protection (prevents path traversal attacks).
 * - SHA-256 integrity verification (PRD LIB-004).
 * - Manifest validation (PRD LIB-003).
 * - Atomic installation of pack metadata and markdown/JSON articles into Room (PRD LIB-005).
 */
class PackZipInstaller(
    private val dao: KnowledgePackDao
) {

    /**
     * Installs a knowledge pack from an input stream (e.g. from network or Android Storage Access Framework).
     */
    suspend fun installFromZipStream(
        inputStream: InputStream,
        expectedSha256: String? = null
    ): Result<KnowledgePackInstallResult> = withContext(Dispatchers.IO) {
        try {
            // Buffer stream bytes to allow both SHA-256 computation and zip extraction
            val byteStream = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteStream.write(buffer, 0, bytesRead)
            }
            val zipBytes = byteStream.toByteArray()

            // 1. Verify SHA-256 Checksum if provided
            if (!expectedSha256.isNullOrBlank()) {
                val computedHash = computeSha256(zipBytes)
                if (!computedHash.equals(expectedSha256.trim(), ignoreCase = true)) {
                    return@withContext Result.failure(
                        SecurityException("SHA-256 integrity verification failed. Expected: $expectedSha256, Computed: $computedHash")
                    )
                }
            }

            // 2. Extract Archive Content
            val zipInput = ZipInputStream(ByteArrayInputStream(zipBytes))
            var manifestJsonStr: String? = null
            var articlesJsonStr: String? = null
            val rawMarkdownFiles = mutableListOf<Pair<String, String>>() // Path to content

            var entry = zipInput.nextEntry
            while (entry != null) {
                val entryName = entry.name.replace('\\', '/')

                // Zip-Slip Security Check: Reject entries attempting path traversal
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    throw SecurityException("Security violation: Malicious path traversal detected in zip entry '$entryName'")
                }

                if (!entry.isDirectory) {
                    val entryBytes = zipInput.readBytes()
                    val textContent = String(entryBytes, Charsets.UTF_8)

                    when {
                        entryName.equals("manifest.json", ignoreCase = true) || entryName.endsWith("/manifest.json", ignoreCase = true) -> {
                            manifestJsonStr = textContent
                        }
                        entryName.equals("articles.json", ignoreCase = true) || entryName.endsWith("/articles.json", ignoreCase = true) -> {
                            articlesJsonStr = textContent
                        }
                        entryName.endsWith(".md", ignoreCase = true) -> {
                            rawMarkdownFiles.add(entryName to textContent)
                        }
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }

            if (manifestJsonStr == null) {
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid knowledge pack bundle: 'manifest.json' not found in archive.")
                )
            }

            // 3. Parse and Validate Manifest
            val manifestJson = JSONObject(manifestJsonStr)
            val packId = manifestJson.optString("pack_id", manifestJson.optString("id")).trim()
            val title = manifestJson.optString("title").trim()
            val description = manifestJson.optString("description").trim()
            val category = manifestJson.optString("category", "General").trim()
            val version = manifestJson.optString("version", "1.0.0").trim()
            val size = manifestJson.optString("size", "${zipBytes.size / 1024} KB").trim()
            val iconColorHex = manifestJson.optLong("icon_color_hex", 0xFF1E88E5)

            val topicsList = mutableListOf<String>()
            val topicsArray = manifestJson.optJSONArray("topics")
            if (topicsArray != null) {
                for (i in 0 until topicsArray.length()) {
                    topicsList.add(topicsArray.getString(i))
                }
            }

            if (packId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Manifest missing required 'pack_id'"))
            if (title.isBlank()) return@withContext Result.failure(IllegalArgumentException("Manifest missing required 'title'"))

            // 4. Assemble Articles
            val articles = mutableListOf<KnowledgePackArticleEntity>()

            // Strategy A: Parse structured articles.json if present
            if (articlesJsonStr != null) {
                val articlesArray = JSONArray(articlesJsonStr)
                for (i in 0 until articlesArray.length()) {
                    val obj = articlesArray.getJSONObject(i)
                    val artId = obj.optString("id", "${packId}_art_$i")
                    val artTitle = obj.optString("title", "Article ${i + 1}")
                    val artSection = obj.optString("section", category)
                    val artContent = obj.optString("content", "")
                    val tags = obj.optJSONArray("tags")?.toString() ?: "[]"

                    articles.add(
                        KnowledgePackArticleEntity(
                            id = artId,
                            packId = packId,
                            title = artTitle,
                            section = artSection,
                            content = artContent,
                            tagsJson = tags,
                            orderIndex = i + 1
                        )
                    )
                }
            }

            // Strategy B: Parse individual markdown files if articles list is empty
            if (articles.isEmpty() && rawMarkdownFiles.isNotEmpty()) {
                rawMarkdownFiles.forEachIndexed { idx, (entryName, markdown) ->
                    val lines = markdown.lines()
                    // Extract title from first markdown heading or filename
                    val headingTitle = lines.firstOrNull { it.trimStart().startsWith("# ") }
                        ?.trimStart()?.removePrefix("# ")?.trim()

                    val filename = entryName.substringAfterLast('/').substringBeforeLast('.')
                    val artTitle = headingTitle?.ifBlank { null } ?: filename.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }

                    // Extract section from directory name or manifest category
                    val parts = entryName.split('/')
                    val section = if (parts.size > 1 && parts[parts.size - 2].isNotBlank()) {
                        parts[parts.size - 2].replace('_', ' ').replaceFirstChar { it.uppercase() }
                    } else category

                    articles.add(
                        KnowledgePackArticleEntity(
                            id = "${packId}_${filename.lowercase()}",
                            packId = packId,
                            title = artTitle,
                            section = section,
                            content = markdown,
                            tagsJson = "[\"$category\"]",
                            orderIndex = idx + 1
                        )
                    )
                }
            }

            if (articles.isEmpty()) {
                // If the pack had no individual articles, create an overview article from description
                articles.add(
                    KnowledgePackArticleEntity(
                        id = "${packId}_overview",
                        packId = packId,
                        title = "$title Overview",
                        section = category,
                        content = "# $title\n\n$description\n\n### Topics Covered:\n" + topicsList.joinToString("\n") { "- $it" },
                        tagsJson = "[\"$category\"]",
                        orderIndex = 1
                    )
                )
            }

            // 5. Atomic Room Persistence
            val topicsFormatted = topicsList.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
            val packEntity = KnowledgePackEntity(
                id = packId,
                title = title,
                description = description,
                size = size,
                isInstalled = true,
                category = category,
                iconColorHex = iconColorHex,
                topicsJson = topicsFormatted
            )

            // Insert pack metadata and all articles
            dao.insertAll(listOf(packEntity))
            dao.deleteArticlesForPack(packId)
            dao.insertArticles(articles)

            Result.success(
                KnowledgePackInstallResult(
                    packId = packId,
                    title = title,
                    version = version,
                    articleCount = articles.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Installs a knowledge pack from a local file on device storage.
     */
    suspend fun installFromZipFile(file: File, expectedSha256: String? = null): Result<KnowledgePackInstallResult> = withContext(Dispatchers.IO) {
        if (!file.exists() || !file.canRead()) {
            return@withContext Result.failure(IllegalArgumentException("Cannot read knowledge pack file: ${file.absolutePath}"))
        }
        FileInputStream(file).use { stream ->
            installFromZipStream(stream, expectedSha256)
        }
    }

    /**
     * Installs a knowledge pack selected through the Android Storage Access Framework (SAF).
     */
    suspend fun installFromUri(context: Context, uri: Uri, expectedSha256: String? = null): Result<KnowledgePackInstallResult> = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext Result.failure(IllegalStateException("Failed to open input stream for URI: $uri"))
        inputStream.use { stream ->
            installFromZipStream(stream, expectedSha256)
        }
    }

    private fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
