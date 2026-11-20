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
                packId = "first_aid",
                title = "First Aid & Emergency Response",
                description = "Clinical trauma protocols, CPR/BLS, hemorrhage control, burn staging, fractures, anaphylaxis, and envenomation.",
                category = "Medical & Safety",
                version = "1.1.0",
                size = "21.6 KB",
                iconColorHex = 0xFFE53935L,
                checksumSha256 = "c08f728a774476fce5b5be1359dec1a5ca582890d6b211c1bcd5a98440770a33",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("CPR & Resuscitation", "Hemorrhage & Tourniquets", "Burns & Thermal Care", "Fractures & Splinting", "Anaphylaxis", "Environmental Shock", "Venom & Bites"),
                downloadUrl = "$GITHUB_RELEASES_BASE/first_aid-1.1.0.zip"
            ),
            KnowledgePackManifest(
                packId = "field_electronics",
                title = "Field Electronics & Hardware Troubleshooting",
                description = "Multimeter component testing, lithium battery diagnostics, SMPS topologies, IPC soldering standards, and field oscilloscope usage.",
                category = "Hardware & Engineering",
                version = "1.1.0",
                size = "14.5 KB",
                iconColorHex = 0xFFFFA000L,
                checksumSha256 = "3ea000a54cbffecd58bc2d9394d13035c7470a2fa7a2ea405415ebddac6e4468",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Multimeter Diagnostics", "Lithium Chemistry & BMS", "SMPS Power Rails", "IPC Soldering Standards", "Field Signal Integrity", "Wire Splicing"),
                downloadUrl = "$GITHUB_RELEASES_BASE/field_electronics-1.1.0.zip"
            ),
            KnowledgePackManifest(
                packId = "wilderness_survival",
                title = "Wilderness Survival & Bushcraft Navigation",
                description = "Water filtration and purification, celestial navigation using Crux and Polaris, thermal debris shelters, firecraft, and emergency signaling.",
                category = "Outdoors & Survival",
                version = "1.1.0",
                size = "11.3 KB",
                iconColorHex = 0xFF2E7D32L,
                checksumSha256 = "81e778ffea95511598b6a33da13fb20aa46b6b749b6569ab93f800ccc6a84326",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Water Sourcing & Purification", "Celestial Navigation", "Thermal Shelters", "Friction Firecraft", "Ground-to-Air Signaling", "Bushcraft Knots"),
                downloadUrl = "$GITHUB_RELEASES_BASE/wilderness_survival-1.1.0.zip"
            ),
            KnowledgePackManifest(
                packId = "dev_cheatsheets",
                title = "Programming & Developer Cheat Sheets",
                description = "High-density technical cheat sheets for Kotlin coroutines, Git version control, Regular Expressions, Linux Bash scripting, and SQL optimization.",
                category = "Development",
                version = "1.1.0",
                size = "8.7 KB",
                iconColorHex = 0xFF0288D1L,
                checksumSha256 = "029e1f468157e44d0c38024f05151411b98467b9f9716d9a17777f7b038576df",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Kotlin Concurrency", "Git Disaster Recovery", "Regex Syntax", "Linux Bash Scripting", "SQL Query Optimization", "Docker CLI"),
                downloadUrl = "$GITHUB_RELEASES_BASE/dev_cheatsheets-1.1.0.zip"
            ),
            KnowledgePackManifest(
                packId = "comp_tech",
                title = "Computer & Tech",
                description = "Linux system administration, network diagnostics, PC boot sequence, and systemd service management.",
                category = "Technology",
                version = "1.3.0",
                size = "4.3 KB",
                iconColorHex = 0xFF43A047L,
                checksumSha256 = "a3b9539b411b40ef224d661c062590819c62f05b5578ea4f9de32ecfa22c3f14",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Linux Permissions", "Network Diagnostics", "Boot Architecture", "Systemd Services", "Operating Systems", "Networking"),
                downloadUrl = "$GITHUB_RELEASES_BASE/comp_tech-1.3.0.zip"
            ),
            KnowledgePackManifest(
                packId = "devices_repairs",
                title = "Devices & Repairs",
                description = "General electronics fixes, soldering, multimeter diagnostics, and power rail repair.",
                category = "Engineering",
                version = "1.4.0",
                size = "3.7 KB",
                iconColorHex = 0xFF43A047L,
                checksumSha256 = "a3341dc0d28cc136134757ded5d52138798765be15e3008cd9482d079142cb5d",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Soldering", "Circuit Boards", "Multimeter Tests", "Component Replacement", "Power Supplies"),
                downloadUrl = "$GITHUB_RELEASES_BASE/devices_repairs-1.4.0.zip"
            ),
            KnowledgePackManifest(
                packId = "learning",
                title = "Learning",
                description = "Science concepts, Newtonian mechanics, periodic table chemistry, and thermodynamics.",
                category = "Education",
                version = "1.1.0",
                size = "2.9 KB",
                iconColorHex = 0xFF7E57C2L,
                checksumSha256 = "3a02922e3e92fdd399369f04442003313ede02ffce756c3fc75bf53b311f95eb",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Physics Formulas", "Periodic Table", "Thermodynamics", "Mechanics", "Chemistry"),
                downloadUrl = "$GITHUB_RELEASES_BASE/learning-1.1.0.zip"
            ),
            KnowledgePackManifest(
                packId = "programming",
                title = "Programming",
                description = "Software architecture, Kotlin coroutines, Room persistence, and data structures.",
                category = "Software",
                version = "1.2.0",
                size = "2.5 KB",
                iconColorHex = 0xFF1E88E5L,
                checksumSha256 = "68d9388502236f10729841986ac95fcd127e3345f4000a3f7c125c5279f7442a",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Kotlin Coroutines", "Room Database", "Clean Architecture", "Data Structures"),
                downloadUrl = "$GITHUB_RELEASES_BASE/programming-1.2.0.zip"
            ),
            KnowledgePackManifest(
                packId = "languages",
                title = "Languages",
                description = "Linguistic frameworks, grammar, syntactic structures, and translation standards.",
                category = "Language",
                version = "1.1.0",
                size = "2.0 KB",
                iconColorHex = 0xFF00ACC1L,
                checksumSha256 = "a14bf444ae7b3c9d2ddcb8d3d65a0c41bc9a63bed319428c1310e8d7699bc1b2",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Typology", "Grammar", "Syntax", "Phonetics", "Translation"),
                downloadUrl = "$GITHUB_RELEASES_BASE/languages-1.1.0.zip"
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
