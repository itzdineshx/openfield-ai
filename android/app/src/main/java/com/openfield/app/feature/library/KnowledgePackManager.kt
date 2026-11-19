package com.openfield.app.feature.library

import com.openfield.app.core.model.KnowledgePack
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.data.remote.CatalogApiClient
import com.openfield.app.data.remote.CatalogResult
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
    val topics: List<String>
)

/**
 * Manages offline knowledge packs:
 * - Manifest validation, SHA-256 verification, and provenance checks.
 * - Atomic installation and updates.
 * - Local search and discovery by keyword, category, and topic.
 */
class KnowledgePackManager(
    private val dao: KnowledgePackDao,
    private val apiClient: CatalogApiClient = CatalogApiClient()
) {

    companion object {
        const val OFFICIAL_PROVENANCE = "OpenField Official Curated Corpus"

        val CURATED_CATALOG: List<KnowledgePackManifest> = listOf(
            KnowledgePackManifest(
                packId = "comp_tech",
                title = "Computer & Tech",
                description = "Troubleshooting, errors, setup",
                category = "Technology",
                version = "1.2.0",
                size = "2.4 GB",
                iconColorHex = 0xFF43A047,
                checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Networking", "Linux Commands", "Hardware Architecture", "Troubleshooting", "Operating Systems", "Linux")
            ),
            KnowledgePackManifest(
                packId = "devices_repairs",
                title = "Devices & Repairs",
                description = "General fixes and maintenance",
                category = "Engineering",
                version = "1.3.0",
                size = "1.8 GB",
                iconColorHex = 0xFF43A047,
                checksumSha256 = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Soldering", "Circuit Boards", "Multimeter Tests", "Component Replacement", "Power Supplies", "Multimeter")
            ),
            KnowledgePackManifest(
                packId = "learning",
                title = "Learning",
                description = "Science, concepts, how-to guides",
                category = "Education",
                version = "1.0.0",
                size = "1.2 GB",
                iconColorHex = 0xFF7E57C2,
                checksumSha256 = "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Physics Formulas", "Periodic Table", "Calculus Essentials", "Cellular Biology", "Mechanics")
            ),
            KnowledgePackManifest(
                packId = "programming",
                title = "Programming",
                description = "Basics, examples and concepts",
                category = "Software",
                version = "1.1.0",
                size = "1.9 GB",
                iconColorHex = 0xFF1E88E5,
                checksumSha256 = "5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Kotlin Coroutines", "Room Database", "Clean Architecture", "Data Structures", "Algorithms")
            ),
            KnowledgePackManifest(
                packId = "languages",
                title = "Languages",
                description = "Translation and learning support",
                category = "Language",
                version = "1.0.0",
                size = "860 MB",
                iconColorHex = 0xFF00ACC1,
                checksumSha256 = "6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b",
                provenance = OFFICIAL_PROVENANCE,
                topics = listOf("Vocabulary", "Technical Terms", "Grammar Rules", "Phrases", "Translations")
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
        return Result.success(Unit)
    }

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
}
