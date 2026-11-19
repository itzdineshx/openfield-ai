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
}
