package com.openfield.app

import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.feature.library.KnowledgePackManager
import com.openfield.app.feature.library.KnowledgePackManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KnowledgePackManagerTest {

    private class FakeKnowledgePackDao : KnowledgePackDao {
        val storage = mutableListOf<KnowledgePackEntity>()
        val articles = mutableListOf<com.openfield.app.data.local.entity.KnowledgePackArticleEntity>()

        override fun getKnowledgePacksFlow(): Flow<List<KnowledgePackEntity>> = flowOf(storage)
        override suspend fun getKnowledgePacks(): List<KnowledgePackEntity> = storage.toList()
        override suspend fun getPackById(id: String): KnowledgePackEntity? = storage.find { it.id == id }

        override suspend fun insertAll(packs: List<KnowledgePackEntity>) {
            storage.removeAll { existing -> packs.any { it.id == existing.id } }
            storage.addAll(packs)
        }

        override suspend fun updateInstallation(id: String, isInstalled: Boolean) {
            val idx = storage.indexOfFirst { it.id == id }
            if (idx != -1) {
                storage[idx] = storage[idx].copy(isInstalled = isInstalled)
            }
        }

        override suspend fun deletePack(id: String) {
            storage.removeAll { it.id == id }
        }

        override fun getArticlesForPackFlow(packId: String): Flow<List<com.openfield.app.data.local.entity.KnowledgePackArticleEntity>> =
            flowOf(articles.filter { it.packId == packId })

        override suspend fun getArticlesForPack(packId: String): List<com.openfield.app.data.local.entity.KnowledgePackArticleEntity> =
            articles.filter { it.packId == packId }

        override suspend fun getArticleById(id: String): com.openfield.app.data.local.entity.KnowledgePackArticleEntity? =
            articles.find { it.id == id }

        override suspend fun insertArticles(articles: List<com.openfield.app.data.local.entity.KnowledgePackArticleEntity>) {
            this.articles.removeAll { a -> articles.any { it.id == a.id } }
            this.articles.addAll(articles)
        }

        override suspend fun deleteArticlesForPack(packId: String) {
            articles.removeAll { it.packId == packId }
        }

        override suspend fun searchArticles(query: String): List<com.openfield.app.data.local.entity.KnowledgePackArticleEntity> =
            articles.filter { it.title.contains(query, true) || it.content.contains(query, true) }

        override suspend fun getAllInstalledArticles(): List<com.openfield.app.data.local.entity.KnowledgePackArticleEntity> =
            articles.toList()
    }

    private lateinit var dao: FakeKnowledgePackDao
    private lateinit var manager: KnowledgePackManager

    @Before
    fun setUp() {
        dao = FakeKnowledgePackDao()
        manager = KnowledgePackManager(dao)
    }

    @Test
    fun curatedCatalog_allManifestsAreValid() {
        for (manifest in KnowledgePackManager.CURATED_CATALOG) {
            val result = manager.validateManifest(manifest)
            assertTrue("Manifest ${manifest.packId} should be valid", result.isSuccess)
        }
    }

    @Test
    fun validateManifest_rejectsInvalidVersionFormat() {
        val badManifest = KnowledgePackManifest(
            packId = "test_bad",
            title = "Test",
            description = "Desc",
            category = "Cat",
            version = "v1.2", // Missing patch digit
            size = "1 MB",
            iconColorHex = 0L,
            checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            provenance = "Test",
            topics = listOf("A")
        )
        val result = manager.validateManifest(badManifest)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("semantic version") == true)
    }

    @Test
    fun validateManifest_rejectsInvalidChecksumLength() {
        val badManifest = KnowledgePackManifest(
            packId = "test_bad",
            title = "Test",
            description = "Desc",
            category = "Cat",
            version = "1.0.0",
            size = "1 MB",
            iconColorHex = 0L,
            checksumSha256 = "abc123", // Short hash
            provenance = "Test",
            topics = listOf("A")
        )
        val result = manager.validateManifest(badManifest)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("SHA-256") == true)
    }

    @Test
    fun seedDefaultPacksIfEmpty_populatesCuratedPacks() = runBlocking {
        assertEquals(0, dao.storage.size)
        manager.seedDefaultPacksIfEmpty()
        assertEquals(KnowledgePackManager.CURATED_CATALOG.size, dao.storage.size)

        // Calling again does not duplicate
        manager.seedDefaultPacksIfEmpty()
        assertEquals(KnowledgePackManager.CURATED_CATALOG.size, dao.storage.size)
    }

    @Test
    fun togglePackInstallation_updatesStatusAtomically() = runBlocking {
        manager.seedDefaultPacksIfEmpty()
        val packId = "comp_tech"

        assertFalse(dao.getPackById(packId)!!.isInstalled)

        val installResult = manager.togglePackInstallation(packId)
        assertTrue(installResult.isSuccess)
        assertTrue(installResult.getOrNull() == true)
        assertTrue(dao.getPackById(packId)!!.isInstalled)

        val uninstallResult = manager.togglePackInstallation(packId)
        assertTrue(uninstallResult.isSuccess)
        assertFalse(uninstallResult.getOrNull() == true)
        assertFalse(dao.getPackById(packId)!!.isInstalled)
    }

    @Test
    fun searchPacks_matchesKeywordAcrossTopics() = runBlocking {
        manager.seedDefaultPacksIfEmpty()

        val results = manager.searchPacks("Linux")
        assertTrue(results.isNotEmpty())
        assertEquals("comp_tech", results.first().id)

        val electronicsResults = manager.searchPacks("Multimeter")
        assertTrue(electronicsResults.isNotEmpty())
        assertEquals("devices_repairs", electronicsResults.first().id)

        val emptyResults = manager.searchPacks("NonExistentTermXYZ")
        assertTrue(emptyResults.isEmpty())
    }

    @Test
    fun updatePackToLatest_preservesInstalledState() = runBlocking {
        manager.seedDefaultPacksIfEmpty()
        manager.togglePackInstallation("comp_tech")
        assertTrue(dao.getPackById("comp_tech")!!.isInstalled)

        val updateResult = manager.updatePackToLatest("comp_tech")
        assertTrue(updateResult.isSuccess)
        assertTrue(dao.getPackById("comp_tech")!!.isInstalled)
    }

    @Test
    fun togglePackInstallation_installsRealArticlesAndCleansOnUninstall() = runBlocking {
        manager.seedDefaultPacksIfEmpty()
        val packId = "comp_tech"

        // 1. Install pack
        val installResult = manager.togglePackInstallation(packId)
        assertTrue(installResult.isSuccess)
        assertTrue(installResult.getOrThrow())

        // Verify real domain articles are populated
        val installedArticles = dao.getArticlesForPack(packId)
        assertTrue("Installed pack should contain real articles", installedArticles.isNotEmpty())
        assertTrue("Articles should contain Linux guide", installedArticles.any { it.title.contains("Linux") })
        assertTrue("Articles should contain Network guide", installedArticles.any { it.title.contains("Network") })

        // 2. Uninstall pack per PRD LIB-006
        val uninstallResult = manager.togglePackInstallation(packId)
        assertTrue(uninstallResult.isSuccess)
        assertFalse(uninstallResult.getOrThrow())

        val remainingArticles = dao.getArticlesForPack(packId)
        assertTrue("Uninstalling pack must clean derived index entries and articles", remainingArticles.isEmpty())
    }

    @Test
    fun curatedArticles_containsSubstantialRealContentAcrossDomains() {
        val articles = com.openfield.app.feature.library.KnowledgePackContent.CURATED_ARTICLES
        assertTrue("Should contain multiple curated articles", articles.size >= 10)

        val packIds = articles.map { it.packId }.distinct()
        assertTrue("Should cover computer & tech", packIds.contains("comp_tech"))
        assertTrue("Should cover devices & repairs", packIds.contains("devices_repairs"))
        assertTrue("Should cover learning & science", packIds.contains("learning"))
        assertTrue("Should cover programming", packIds.contains("programming"))
        assertTrue("Should cover languages", packIds.contains("languages"))
        assertTrue("Should cover first aid", packIds.contains("first_aid"))
        assertTrue("Should cover field electronics", packIds.contains("field_electronics"))
        assertTrue("Should cover wilderness survival", packIds.contains("wilderness_survival"))
        assertTrue("Should cover dev cheatsheets", packIds.contains("dev_cheatsheets"))

        for (art in articles) {
            assertTrue("Article ${art.id} must have title", art.title.isNotBlank())
            assertTrue("Article ${art.id} must have section", art.section.isNotBlank())
            assertTrue("Article ${art.id} must have substantial real content (>200 chars)", art.content.length > 200)
        }
    }

    @Test
    fun curatedCatalog_containsAllExpandedDomainPacks() {
        val packIds = KnowledgePackManager.CURATED_CATALOG.map { it.packId }
        assertTrue("Catalog must contain first_aid", packIds.contains("first_aid"))
        assertTrue("Catalog must contain field_electronics", packIds.contains("field_electronics"))
        assertTrue("Catalog must contain wilderness_survival", packIds.contains("wilderness_survival"))
        assertTrue("Catalog must contain dev_cheatsheets", packIds.contains("dev_cheatsheets"))
        assertEquals(9, KnowledgePackManager.CURATED_CATALOG.size)
    }

    @Test
    fun realTimeDomainSearch_retrievesRelevantKnowledge() = runBlocking {
        manager.seedDefaultPacksIfEmpty()

        // Install new domain packs
        assertTrue(manager.togglePackInstallation("first_aid").getOrThrow())
        assertTrue(manager.togglePackInstallation("field_electronics").getOrThrow())
        assertTrue(manager.togglePackInstallation("wilderness_survival").getOrThrow())
        assertTrue(manager.togglePackInstallation("dev_cheatsheets").getOrThrow())

        // 1. First Aid searches
        val cprResults = dao.searchArticles("CPR")
        assertTrue("CPR query should return results", cprResults.isNotEmpty())
        assertTrue(cprResults.any { it.title.contains("CPR") || it.content.contains("DRSABCD") })

        val tourniquetResults = dao.searchArticles("Tourniquet")
        assertTrue("Tourniquet query should return results", tourniquetResults.isNotEmpty())
        assertTrue(tourniquetResults.any { it.content.contains("Combat Application Tourniquet") })

        // 2. Field Electronics searches
        val multimeterResults = dao.searchArticles("Multimeter")
        assertTrue("Multimeter query should return results", multimeterResults.isNotEmpty())
        assertTrue(multimeterResults.any { it.content.contains("Diode Mode") })

        val batteryResults = dao.searchArticles("LiFePO4")
        assertTrue("Battery chemistry query should return results", batteryResults.isNotEmpty())

        // 3. Wilderness Survival searches
        val navigationResults = dao.searchArticles("Crux")
        assertTrue("Crux celestial navigation query should return results", navigationResults.isNotEmpty())
        assertTrue(navigationResults.any { it.title.contains("Crux") || it.content.contains("Southern Cross") })

        val waterResults = dao.searchArticles("Purification")
        assertTrue("Water purification query should return results", waterResults.isNotEmpty())

        // 4. Developer Cheatsheet searches
        val reflogResults = dao.searchArticles("reflog")
        assertTrue("Git reflog query should return results", reflogResults.isNotEmpty())

        val coroutinesResults = dao.searchArticles("Dispatchers")
        assertTrue("Kotlin Dispatchers query should return results", coroutinesResults.isNotEmpty())
    }
}
