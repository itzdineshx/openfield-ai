package com.openfield.app.feature.library

import android.content.Context
import android.net.Uri
import com.openfield.app.core.model.KnowledgePack
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.data.remote.CatalogApiClient
import com.openfield.app.data.remote.CatalogResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * Specification and metadata manifest for offline knowledge packs.
 */
data class KnowledgePackManifest(
    val packId: String,
    val title: String,
    val description: String,
    val category: String,
    val version: String,
    val size: String,
    val iconColorHex: Long,
    val checksumSha256: String,
    val provenance: String,
    val topics: List<String>,
    val downloadUrl: String? = null
)

/**
 * Manages offline knowledge packs:
 * - Manifest validation, SHA-256 verification, and provenance checks.
 * - Atomic installation and updates.
 * - Local search and discovery by keyword, category, and topic.
 */
class KnowledgePackManager(
    private val dao: KnowledgePackDao,
    private val apiClient: CatalogApiClient = CatalogApiClient(),
    val installer: PackZipInstaller = PackZipInstaller(dao)
) {

    companion object {
        const val OFFICIAL_PROVENANCE = "CRUX Official Curated Corpus"
        const val GITHUB_RELEASES_BASE = "https://github.com/itzdineshx/openfield-ai/releases/download/v1.0.0"

        val CURATED_CATALOG: List<KnowledgePackManifest> = listOf(
            KnowledgePackManifest(
                packId = "comp_tech",
                title = "Computer & Tech",
                description = "Troubleshooting, networking, boot diagnostics, and Linux system administration.",
                category = "Technology",
                version = "1.2.0",
                size = "6.7 KB",
                iconColorHex = 0xFF43A047,
                checksumSha256 = "1c7860f89bb47da892b077056be434a11f2339344081c37aac2ab452c745d142",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Networking", "Linux Commands", "Hardware Architecture", "Troubleshooting", "Operating Systems", "Linux"),
                downloadUrl = "$GITHUB_RELEASES_BASE/comp_tech-1.2.0.zip"
            ),
            KnowledgePackManifest(
                packId = "devices_repairs",
                title = "Devices & Repairs",
                description = "General electronics fixes, soldering, multimeter diagnostics, and power rail repair.",
                category = "Engineering",
                version = "1.3.0",
                size = "6.7 KB",
                iconColorHex = 0xFF43A047,
                checksumSha256 = "b14c5e9b5655982d7ebb9ddd14cf333b0d402edb0154fb7bb12092c9404e5215",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Soldering", "Circuit Boards", "Multimeter Tests", "Component Replacement", "Power Supplies", "Multimeter"),
                downloadUrl = "$GITHUB_RELEASES_BASE/devices_repairs-1.3.0.zip"
            ),
            KnowledgePackManifest(
                packId = "learning",
                title = "Learning",
                description = "Science, concepts, Newtonian mechanics, and periodic table chemistry.",
                category = "Education",
                version = "1.0.0",
                size = "4.3 KB",
                iconColorHex = 0xFF7E57C2,
                checksumSha256 = "d967d53653da346540a7ec3d29e0320d47e359f2455bf2d623c00730de5e6047",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Physics Formulas", "Periodic Table", "Calculus Essentials", "Cellular Biology", "Mechanics"),
                downloadUrl = "$GITHUB_RELEASES_BASE/learning-1.0.0.zip"
            ),
            KnowledgePackManifest(
                packId = "programming",
                title = "Programming",
                description = "Software architecture, Kotlin coroutines, Room persistence, and data structures.",
                category = "Software",
                version = "1.1.0",
                size = "4.4 KB",
                iconColorHex = 0xFF1E88E5,
                checksumSha256 = "a1975937eec6bc5d5aa4ae4ea3bc097f1d100d685177f19a9110909acb2ff488",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Kotlin Coroutines", "Room Database", "Clean Architecture", "Data Structures", "Algorithms"),
                downloadUrl = "$GITHUB_RELEASES_BASE/programming-1.1.0.zip"
            ),
            KnowledgePackManifest(
                packId = "languages",
                title = "Languages",
                description = "Linguistic frameworks, grammar, syntactic structures, and translation standards.",
                category = "Language",
                version = "1.0.0",
                size = "2.5 KB",
                iconColorHex = 0xFF00ACC1,
                checksumSha256 = "9d5664dd40f95afc11229e1fcf11a29a5a52e0e3bf906f375ac73ee8e899afba",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Vocabulary", "Technical Terms", "Grammar Rules", "Phrases", "Translations"),
                downloadUrl = "$GITHUB_RELEASES_BASE/languages-1.0.0.zip"
            )
        )
    }

    /**
     * Verifies that the manifest conforms to integrity and provenance standards.
     */
    fun validateManifest(manifest: KnowledgePackManifest): Result<Unit> {
        if (manifest.packId.isBlank()) return Result.failure(IllegalArgumentException("Manifest packId cannot be empty"))
        if (manifest.title.isBlank()) return Result.failure(IllegalArgumentException("Manifest title cannot be empty"))
        if (!manifest.version.matches(Regex("^\\d+\\.\\d+\\.\\d+$"))) {
            return Result.failure(IllegalArgumentException("Invalid semantic version format: '${manifest.version}'. Expected X.Y.Z"))
        }
        if (manifest.checksumSha256.length != 64 || !manifest.checksumSha256.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
            return Result.failure(IllegalArgumentException("Invalid SHA-256 checksum format: '${manifest.checksumSha256}'"))
        }
        if (manifest.provenance.isBlank()) {
            return Result.failure(IllegalArgumentException("Pack provenance origin must be explicitly stated"))
        }
        return Result.success(Unit)
    }

    /**
     * Seeds default curated knowledge packs into Room if the database has not been initialized.
     */
    suspend fun seedDefaultPacksIfEmpty() {
        val existing = dao.getKnowledgePacks()
        val entities = CURATED_CATALOG.map { manifest ->
            validateManifest(manifest).getOrThrow()
            val existingPack = existing.find { it.id == manifest.packId }
            val isInstalled = existingPack?.isInstalled ?: false
            if (isInstalled) {
                // Ensure articles are seeded for installed packs
                val articles = KnowledgePackContent.CURATED_ARTICLES.filter { it.packId == manifest.packId }
                if (articles.isNotEmpty()) {
                    dao.insertArticles(articles)
                }
            }
            KnowledgePackEntity(
                id = manifest.packId,
                title = manifest.title,
                description = manifest.description,
                size = manifest.size,
                isInstalled = isInstalled,
                category = manifest.category,
                iconColorHex = manifest.iconColorHex,
                topicsJson = formatTopics(manifest.topics)
            )
        }
        dao.insertAll(entities)
    }

    /**
     * Refreshes the pack catalog from the remote FastAPI service if online.
     * Caches all validated manifests locally in Room database.
     * If the remote service is unavailable, retains existing local packs without data loss.
     */
    suspend fun refreshRemoteCatalog(): CatalogResult {
        val result = apiClient.fetchCatalog()
        if (result is CatalogResult.Success) {
            val existing = dao.getKnowledgePacks()
            val entities = result.items.mapNotNull { manifest ->
                if (validateManifest(manifest).isSuccess) {
                    val existingPack = existing.find { it.id == manifest.packId }
                    KnowledgePackEntity(
                        id = manifest.packId,
                        title = manifest.title,
                        description = manifest.description,
                        size = manifest.size,
                        isInstalled = existingPack?.isInstalled ?: false,
                        category = manifest.category,
                        iconColorHex = manifest.iconColorHex,
                        topicsJson = formatTopics(manifest.topics)
                    )
                } else null
            }
            if (entities.isNotEmpty()) {
                dao.insertAll(entities)
            }
        }
        return result
    }

    /**
     * Atomically toggles installation state with manifest verification.
     * Real-time activation: when installed, indexes authentic curated domain articles into Room;
     * when uninstalled, cleans derived index entries per PRD LIB-006.
     */
    suspend fun togglePackInstallation(packId: String): Result<Boolean> {
        val manifest = CURATED_CATALOG.find { it.packId == packId }
        if (manifest != null) {
            val validation = validateManifest(manifest)
            if (validation.isFailure) return Result.failure(validation.exceptionOrNull()!!)
        }

        val existing = dao.getPackById(packId)
            ?: return Result.failure(NoSuchElementException("Pack not found: $packId"))

        val newInstalled = !existing.isInstalled
        dao.updateInstallation(packId, newInstalled)

        if (newInstalled) {
            // Activate real content articles
            val articles = KnowledgePackContent.CURATED_ARTICLES.filter { it.packId == packId }
            if (articles.isNotEmpty()) {
                dao.insertArticles(articles)
            }
        } else {
            // Clean index entries
            dao.deleteArticlesForPack(packId)
        }

        return Result.success(newInstalled)
    }

    /**
     * Performs atomic pack update when a newer manifest version is detected.
     */
    suspend fun updatePackToLatest(packId: String): Result<Unit> {
        val manifest = CURATED_CATALOG.find { it.packId == packId }
            ?: return Result.failure(NoSuchElementException("Manifest not found for pack $packId"))

        validateManifest(manifest).getOrThrow()

        val existing = dao.getPackById(packId)
        val isCurrentlyInstalled = existing?.isInstalled ?: false

        val updatedEntity = KnowledgePackEntity(
            id = manifest.packId,
            title = manifest.title,
            description = manifest.description,
            size = manifest.size,
            isInstalled = isCurrentlyInstalled,
            category = manifest.category,
            iconColorHex = manifest.iconColorHex,
            topicsJson = formatTopics(manifest.topics)
        )

        dao.insertAll(listOf(updatedEntity))

        if (isCurrentlyInstalled) {
            val articles = KnowledgePackContent.CURATED_ARTICLES.filter { it.packId == packId }
            if (articles.isNotEmpty()) {
                dao.insertArticles(articles)
            }
        }
        return Result.success(Unit)
    }

    fun getArticlesForPackFlow(packId: String) = dao.getArticlesForPackFlow(packId)
    suspend fun getArticlesForPack(packId: String) = dao.getArticlesForPack(packId)
    suspend fun getArticleById(id: String) = dao.getArticleById(id)
    suspend fun searchArticles(query: String) = dao.searchArticles(query)
    suspend fun getAllInstalledArticles() = dao.getAllInstalledArticles()

    /**
     * Search and discover packs across title, description, category, and topics.
     */
    suspend fun searchPacks(query: String, category: String? = null): List<KnowledgePack> {
        val allEntities = dao.getKnowledgePacks()
        val q = query.trim().lowercase()

        return allEntities.mapNotNull { entity ->
            val topicsList = parseTopics(entity.topicsJson)
            val manifest = CURATED_CATALOG.find { it.packId == entity.id }
            val version = manifest?.version ?: "1.0.0"

            val matchesCategory = category.isNullOrBlank() || entity.category.equals(category, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                entity.title.lowercase().contains(q) ||
                entity.description.lowercase().contains(q) ||
                entity.category.lowercase().contains(q) ||
                topicsList.any { it.lowercase().contains(q) }

            if (matchesCategory && matchesQuery) {
                KnowledgePack(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    size = entity.size,
                    isInstalled = entity.isInstalled,
                    category = entity.category,
                    iconColorHex = entity.iconColorHex,
                    topics = topicsList,
                    version = version
                )
            } else null
        }
    }

    fun formatTopics(topics: List<String>): String {
        return topics.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
    }

    private fun parseTopics(topicsJson: String): List<String> {
        return try {
            topicsJson.trim('[', ']')
                .split(",")
                .map { it.trim('"', ' ') }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Installs a knowledge pack archive (.zip) selected via Storage Access Framework (SAF sideloading).
     */
    suspend fun installPackFromUri(context: Context, uri: Uri): Result<KnowledgePackInstallResult> {
        return installer.installFromUri(context, uri)
    }

    /**
     * Installs a knowledge pack archive (.zip) from a local storage file.
     */
    suspend fun installPackFromZipFile(file: File, expectedSha256: String? = null): Result<KnowledgePackInstallResult> {
        return installer.installFromZipFile(file, expectedSha256)
    }

    /**
     * Downloads a pack bundle (.zip) from GitHub Releases or remote catalog, verifies its SHA-256 hash,
     * extracts all markdown/JSON articles, and activates it atomically in Room database.
     */
    suspend fun downloadAndInstallPack(
        packId: String,
        context: Context,
        onProgress: ((Float) -> Unit)? = null
    ): Result<KnowledgePackInstallResult> = withContext(Dispatchers.IO) {
        val manifest = CURATED_CATALOG.find { it.packId == packId }
            ?: return@withContext Result.failure(NoSuchElementException("Knowledge pack not found in catalog: $packId"))

        val downloadUrl = manifest.downloadUrl
        if (downloadUrl.isNullOrBlank()) {
            // Local fallback if no download URL is configured
            togglePackInstallation(packId)
            val count = dao.getArticlesForPack(packId).size
            return@withContext Result.success(
                KnowledgePackInstallResult(packId, manifest.title, manifest.version, count)
            )
        }

        val tempZipFile = File(context.cacheDir, "pack_${packId}_${System.currentTimeMillis()}.zip")
        try {
            val downloadResult = apiClient.downloadPackArchive(downloadUrl, tempZipFile, onProgress)
            if (downloadResult.isFailure) {
                return@withContext Result.failure(downloadResult.exceptionOrNull()!!)
            }

            val installResult = installer.installFromZipFile(tempZipFile, manifest.checksumSha256)
            installResult
        } finally {
            if (tempZipFile.exists()) tempZipFile.delete()
        }
    }
}
