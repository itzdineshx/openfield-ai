package com.openfield.app

import com.openfield.app.feature.ai.model.ModelCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelCatalogTest {

    @Test
    fun catalog_containsOnlyGemma4E2B() {
        assertEquals(1, ModelCatalog.ALL_MODELS.size)
        val model = ModelCatalog.ALL_MODELS.first()
        assertEquals("gemma-4-e2b-it", model.id)
        assertEquals("Gemma 4 E2B IT", model.displayName)
    }

    @Test
    fun modelArtifact_hasCorrectExtensionAndSize() {
        val model = ModelCatalog.GEMMA_4_E2B
        assertTrue(model.fileName.endsWith(".litertlm"))
        assertEquals("gemma-4-E2B-it.litertlm", model.fileName)
        assertTrue(model.sizeBytes > 2_000_000_000L)
    }

    @Test
    fun modelRepository_pointsToOfficialCommunityRepo() {
        val model = ModelCatalog.GEMMA_4_E2B
        assertEquals("litert-community/gemma-4-E2B-it-litert-lm", model.repository)
        assertTrue(model.downloadUrl.contains("huggingface.co/litert-community"))
        assertTrue(model.downloadUrl.endsWith("gemma-4-E2B-it.litertlm"))
    }

    @Test
    fun modelCapabilities_supportVisionAndContext() {
        val model = ModelCatalog.GEMMA_4_E2B
        assertTrue(model.supportsVision)
        assertEquals(4096, model.contextLength)
    }

    @Test
    fun findByFileName_matchesValidGemmaArtifact() {
        assertNotNull(ModelCatalog.findByFileName("gemma-4-E2B-it.litertlm"))
        assertNotNull(ModelCatalog.findByFileName("GEMMA-4-E2B-it.litertlm"))
    }
}
