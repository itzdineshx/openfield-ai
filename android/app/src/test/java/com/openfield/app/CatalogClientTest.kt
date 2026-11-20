package com.openfield.app

import com.openfield.app.data.remote.CatalogResult
import com.openfield.app.feature.library.KnowledgePackManager
import com.openfield.app.feature.library.KnowledgePackManifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogClientTest {

    @Test
    fun catalogManifestValidation_validPack_succeeds() {
        val manifest = KnowledgePackManager.CURATED_CATALOG.first()
        val result = KnowledgePackManager(FakeKnowledgePackDao()).validateManifest(manifest)
        assertTrue(result.isSuccess)
    }

    @Test
    fun catalogManifestValidation_invalidChecksum_fails() {
        val manifest = KnowledgePackManifest(
            packId = "invalid_pack",
            title = "Corrupt Pack",
            description = "Test description",
            category = "Technology",
            version = "1.0.0",
            size = "50 MB",
            iconColorHex = 0xFF43A047,
            checksumSha256 = "invalid_short_hash",
            provenance = "Test",
            topics = listOf("Testing")
        )
        val result = KnowledgePackManager(FakeKnowledgePackDao()).validateManifest(manifest)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("SHA-256") == true)
    }

    @Test
    fun catalogResult_errorClassification_offlineHandledGracefully() {
        val error = CatalogResult.Error("Connection refused", isOffline = true)
        assertTrue(error.isOffline)
        assertEquals("Connection refused", error.message)
    }

    private class FakeKnowledgePackDao : com.openfield.app.data.local.dao.KnowledgePackDao {
        private val list = mutableListOf<com.openfield.app.data.local.entity.KnowledgePackEntity>()
        private val articles = mutableListOf<com.openfield.app.data.local.entity.KnowledgePackArticleEntity>()

        override suspend fun getKnowledgePacks(): List<com.openfield.app.data.local.entity.KnowledgePackEntity> = list
        override fun getKnowledgePacksFlow() = kotlinx.coroutines.flow.flowOf(list)
        override suspend fun getPackById(id: String) = list.find { it.id == id }
        override suspend fun insertAll(packs: List<com.openfield.app.data.local.entity.KnowledgePackEntity>) {
            list.removeAll { p -> packs.any { it.id == p.id } }
            list.addAll(packs)
        }
        override suspend fun updateInstallation(id: String, isInstalled: Boolean) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) {
                val existing = list[idx]
                list[idx] = existing.copy(isInstalled = isInstalled)
            }
        }
        override suspend fun deletePack(id: String) {
            list.removeAll { it.id == id }
        }

        override fun getArticlesForPackFlow(packId: String) = kotlinx.coroutines.flow.flowOf(articles.filter { it.packId == packId })
        override suspend fun getArticlesForPack(packId: String) = articles.filter { it.packId == packId }
        override suspend fun getArticleById(id: String) = articles.find { it.id == id }
        override suspend fun insertArticles(articles: List<com.openfield.app.data.local.entity.KnowledgePackArticleEntity>) {
            this.articles.removeAll { a -> articles.any { it.id == a.id } }
            this.articles.addAll(articles)
        }
        override suspend fun deleteArticlesForPack(packId: String) {
            articles.removeAll { it.packId == packId }
        }
        override suspend fun searchArticles(query: String) = articles.filter { it.title.contains(query, true) || it.content.contains(query, true) }
        override suspend fun getAllInstalledArticles() = articles.toList()
    }
}
