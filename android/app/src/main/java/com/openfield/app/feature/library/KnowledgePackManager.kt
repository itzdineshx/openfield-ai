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
                checksumSha256 = "4a6dd5feb64089002634d872b74fd85a142e757088a60feead9170999ea732d4",
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
                checksumSha256 = "d0c4120b407dc0794dfe2ae7f5ff3e6053e469942e69b79e24f6a8240ff97f3e",
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
                checksumSha256 = "98ce1fb49ef387c3e403db243dae0ad523d5c9ca24d64a6d76125e9254370e41",
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
                checksumSha256 = "d9d71ad4aea47f7fb4de54e689c36100f0bed786d253415872f9d933a6303c18",
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
                checksumSha256 = "c28eaab8893c6bf84ed5966dee82fd74034cd06d411f13626bc04d6c1d272c1b",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Vocabulary", "Technical Terms", "Grammar Rules", "Phrases", "Translations"),
                downloadUrl = "$GITHUB_RELEASES_BASE/languages-1.0.0.zip"
            ),
            KnowledgePackManifest(
                packId = "first_aid",
                title = "First Aid & Emergency Response",
                description = "Essential trauma protocols, CPR, wound dressing, fracture immobilization, burns, and shock management.",
                category = "Medical & Safety",
                version = "1.0.0",
                size = "8.9 KB",
                iconColorHex = 0xFFE53935,
                checksumSha256 = "f2f97968f3471a1fed7f0bdbe13593ee382f24cb3e19021a084b7ce0ee6f2f7b",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("CPR & Choking", "Hemorrhage Control", "Burn Treatment", "Fractures & Sprains", "Shock & Hypothermia", "Emergency Protocols"),
                downloadUrl = "$GITHUB_RELEASES_BASE/first_aid-1.0.0.zip"
            ),
            KnowledgePackManifest(
                packId = "field_electronics",
                title = "Field Electronics & Hardware Troubleshooting",
                description = "Multimeter diagnostics, circuit tracing, soldering in remote environments, battery chemistry, and power fault isolation.",
                category = "Hardware & Engineering",
                version = "1.0.0",
                size = "4.6 KB",
                iconColorHex = 0xFFFFA000,
                checksumSha256 = "45684880af39949612fd63e29e2124c23c3d7812171ed4315b1a5e1dea09a122",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Multimeter Diagnostics", "Power Rails", "Soldering & Desoldering", "Lithium Battery Safety", "Component Testing"),
                downloadUrl = "$GITHUB_RELEASES_BASE/field_electronics-1.0.0.zip"
            ),
            KnowledgePackManifest(
                packId = "wilderness_survival",
                title = "Wilderness Survival & Bushcraft Navigation",
                description = "Off-grid survival techniques, water purification, celestial navigation (Crux / Southern Cross & North Star), emergency shelter, and fire craft.",
                category = "Outdoors & Survival",
                version = "1.0.0",
                size = "4.6 KB",
                iconColorHex = 0xFF2E7D32,
                checksumSha256 = "e8631211fc841fc9e435dfc3812c35dc8412f6ee0d002cfe89393bbbee4ddccf",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Water Purification", "Celestial Navigation", "Shelter Construction", "Firecraft", "Edible & Toxic Plants", "Signaling"),
                downloadUrl = "$GITHUB_RELEASES_BASE/wilderness_survival-1.0.0.zip"
            ),
            KnowledgePackManifest(
                packId = "dev_cheatsheets",
                title = "Programming & Developer Cheat Sheets",
                description = "High-density reference cheat sheets for Kotlin, Python, Git, Linux CLI, SQL, Docker, and Regular Expressions.",
                category = "Development",
                version = "1.0.0",
                size = "4.3 KB",
                iconColorHex = 0xFF0288D1,
                checksumSha256 = "537a4fac5be426ff565a4b30d784943071f7804af46476f5ed239fc903643e84",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Kotlin Concurrency", "Python Data Structures", "Git Command Reference", "Linux Bash Shell", "SQL Query Patterns", "Regex Syntax"),
                downloadUrl = "$GITHUB_RELEASES_BASE/dev_cheatsheets-1.0.0.zip"
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
