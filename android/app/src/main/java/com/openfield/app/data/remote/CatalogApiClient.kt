package com.openfield.app.data.remote

import com.openfield.app.feature.library.KnowledgePackManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed interface CatalogResult {
    data class Success(val items: List<KnowledgePackManifest>, val total: Int) : CatalogResult
    data class Error(val message: String, val isOffline: Boolean = false) : CatalogResult
}

/**
 * Remote API client for OpenField Knowledge Pack Catalog.
 * Supports:
 * - Direct zero-cost hosting via GitHub Releases & GitHub Raw JSON.
 * - Local developer endpoints (FastAPI on 127.0.0.1 or Android emulator 10.0.2.2).
 * - Automatic HTTP redirect handling (GitHub Release CDN routing).
 * - Streaming download with real-time progress callbacks.
 * Network activity is strictly user-initiated.
 * Never transmits personal prompts, documents, or private device data.
 */
class CatalogApiClient(
    private val candidateUrls: List<String> = listOf(
        "https://raw.githubusercontent.com/itzdineshx/openfield-ai/master/catalog/packs.json",
        "https://raw.githubusercontent.com/itzdineshx/openfield-ai/main/catalog/packs.json",
        "https://raw.githubusercontent.com/openfield-ai/openfield/master/catalog/packs.json",
        "https://raw.githubusercontent.com/openfield-ai/openfield/main/catalog/packs.json",
        "http://127.0.0.1:8000",
        "http://10.0.2.2:8000"
    )
) {
    constructor(baseUrl: String) : this(listOf(baseUrl))

    suspend fun fetchCatalog(
        query: String? = null,
        category: String? = null,
        cursor: Int = 0,
        limit: Int = 20
    ): CatalogResult = withContext(Dispatchers.IO) {
        var lastError: CatalogResult.Error? = null
        for (baseUrl in candidateUrls) {
            var connection: HttpURLConnection? = null
            try {
                val urlString = if (baseUrl.endsWith(".json", ignoreCase = true)) {
                    baseUrl
                } else {
                    val queryParams = mutableListOf<String>()
                    if (!query.isNullOrBlank()) queryParams.add("query=${query.trim()}")
                    if (!category.isNullOrBlank()) queryParams.add("category=${category.trim()}")
                    queryParams.add("cursor=$cursor")
                    queryParams.add("limit=$limit")
                    "$baseUrl/api/v1/packs?${queryParams.joinToString("&")}"
                }

                val url = URL(urlString)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 6000
                    instanceFollowRedirects = true
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    val itemsArray: JSONArray
                    var total: Int

                    if (responseText.trim().startsWith("[")) {
                        itemsArray = JSONArray(responseText)
                        total = itemsArray.length()
                    } else {
                        val json = JSONObject(responseText)
                        itemsArray = json.optJSONArray("items") ?: JSONArray()
                        total = json.optInt("total", itemsArray.length())
                    }

                    val manifests = mutableListOf<KnowledgePackManifest>()
                    val q = query?.trim()?.lowercase()
                    val cat = category?.trim()?.lowercase()

                    for (i in 0 until itemsArray.length()) {
                        val obj = itemsArray.getJSONObject(i)
                        val packCategory = obj.optString("category", "General")
                        val packTitle = obj.optString("title", "")
                        val packDesc = obj.optString("description", "")

                        val topicsArray = obj.optJSONArray("topics")
                        val topics = mutableListOf<String>()
                        if (topicsArray != null) {
                            for (t in 0 until topicsArray.length()) {
                                topics.add(topicsArray.getString(t))
                            }
                        }

                        // Apply client-side filters when loading from static GitHub JSON
                        val matchesCategory = cat.isNullOrBlank() || packCategory.lowercase() == cat
                        val matchesQuery = q.isNullOrBlank() ||
                            packTitle.lowercase().contains(q) ||
                            packDesc.lowercase().contains(q) ||
                            topics.any { it.lowercase().contains(q) }

                        if (matchesCategory && matchesQuery) {
                            val downloadUrl = obj.optString("download_url").ifBlank { null }
                            manifests.add(
                                KnowledgePackManifest(
                                    packId = obj.getString("pack_id"),
                                    title = packTitle,
                                    description = packDesc,
                                    category = packCategory,
                                    version = obj.getString("version"),
                                    size = obj.getString("size"),
                                    iconColorHex = obj.optLong("icon_color_hex", 0xFF1E88E5),
                                    checksumSha256 = obj.getString("checksum_sha256"),
                                    provenance = obj.optString("provenance", "OpenField Curated Catalog"),
                                    topics = topics,
                                    downloadUrl = downloadUrl
                                )
                            )
                        }
                    }

                    return@withContext CatalogResult.Success(items = manifests, total = total)
                } else {
                    lastError = CatalogResult.Error("Catalog service returned HTTP $responseCode")
                }
            } catch (e: java.net.ConnectException) {
                lastError = CatalogResult.Error("Catalog service is offline or unreachable.", isOffline = true)
            } catch (e: java.net.UnknownHostException) {
                lastError = CatalogResult.Error("Unable to resolve catalog service host.", isOffline = true)
            } catch (e: java.net.SocketTimeoutException) {
                lastError = CatalogResult.Error("Catalog request timed out. Please check your connection.", isOffline = true)
            } catch (e: Exception) {
                lastError = CatalogResult.Error(e.message ?: "Failed to refresh knowledge pack catalog.")
            } finally {
                connection?.disconnect()
            }
        }
        return@withContext lastError ?: CatalogResult.Error("Catalog service is unreachable.", isOffline = true)
    }

    /**
     * Downloads a knowledge pack archive (.zip) from GitHub Releases or remote server.
     * Automatically follows HTTP redirects (301, 302, 307, 308) to reach CDN storage.
     */
    suspend fun downloadPackArchive(
        downloadUrl: String,
        destinationFile: File,
        onProgress: ((Float) -> Unit)? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            var currentUrl = downloadUrl
            var redirectCount = 0
            val maxRedirects = 5

            // Follow HTTP redirects (GitHub Releases redirects to AWS/Azure/Fastly CDN)
            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 15000
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "OpenField-Android")
                }

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                        ?: return@withContext Result.failure(IllegalStateException("Redirect without Location header"))
                    currentUrl = location
                    connection.disconnect()
                    redirectCount++
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    break
                } else {
                    return@withContext Result.failure(IllegalStateException("HTTP download failed with code $responseCode"))
                }
            }

            val conn = connection ?: return@withContext Result.failure(IllegalStateException("No connection established"))
            val totalBytes = conn.contentLength.toLong()
            var downloadedBytes = 0L

            conn.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        if (totalBytes > 0 && onProgress != null) {
                            val progress = (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }
                }
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            if (destinationFile.exists()) destinationFile.delete()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
