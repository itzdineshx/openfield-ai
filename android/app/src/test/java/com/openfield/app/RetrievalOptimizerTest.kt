package com.openfield.app

import com.openfield.app.core.model.KnowledgeEvidence
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.feature.document.rag.RetrievalOptimizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrievalOptimizerTest {

    @Test
    fun expandQueryTerms_expandsCommonAcronymsAndTerms() {
        val expanded = RetrievalOptimizer.expandQueryTerms(listOf("ai", "db", "carb"))

        assertTrue(expanded.contains("ai"))
        assertTrue(expanded.contains("artificial intelligence"))
        assertTrue(expanded.contains("db"))
        assertTrue(expanded.contains("database"))
        assertTrue(expanded.contains("carb"))
        assertTrue(expanded.contains("carburetor"))
    }

    @Test
    fun expandQueryTerms_unknownTerm_returnsOnlyOriginalTerm() {
        val expanded = RetrievalOptimizer.expandQueryTerms(listOf("photosynthesis"))
        assertEquals(listOf("photosynthesis"), expanded)
    }

    @Test
    fun scoreChunk_withHeadingContainingTerm_appliesHeadingBoost() {
        val textWithHeading = """
            ## Database Architecture
            SQLite is a relational engine.
        """.trimIndent()

        val textWithoutHeading = "SQLite is a relational engine."

        val scoreHeading = RetrievalOptimizer.scoreChunk(textWithHeading, listOf("database"))
        val scorePlain = RetrievalOptimizer.scoreChunk(textWithoutHeading, listOf("database"))

        assertTrue(scoreHeading > scorePlain)
        assertEquals(0f, scorePlain, 0.001f) // "database" not in textWithoutHeading
    }

    @Test
    fun fairShareInterleave_roundRobinDistributesEvidenceAcrossSources() {
        val doc1Chunks = listOf(
            createEvidence("doc_1", "Doc 1 Chunk 1"),
            createEvidence("doc_1", "Doc 1 Chunk 2"),
            createEvidence("doc_1", "Doc 1 Chunk 3")
        )
        val doc2Chunks = listOf(
            createEvidence("doc_2", "Doc 2 Chunk 1"),
            createEvidence("doc_2", "Doc 2 Chunk 2")
        )
        val packChunks = listOf(
            createEvidence("pack_1", "Pack 1 Overview")
        )

        val map = mapOf(
            "doc_1" to doc1Chunks,
            "doc_2" to doc2Chunks,
            "pack_1" to packChunks
        )

        val interleaved = RetrievalOptimizer.fairShareInterleave(map, maxTotal = 4)

        assertEquals(4, interleaved.size)
        // First 3 items should come from each of the 3 sources
        assertEquals("doc_1", interleaved[0].sourceId)
        assertEquals("doc_2", interleaved[1].sourceId)
        assertEquals("pack_1", interleaved[2].sourceId)
        // 4th item cycles back to doc_1
        assertEquals("doc_1", interleaved[3].sourceId)
    }

    private fun createEvidence(sourceId: String, text: String) = KnowledgeEvidence(
        evidenceId = "ev_${System.nanoTime()}",
        sourceId = sourceId,
        sourceTitle = "Title",
        sourceType = KnowledgeSourceType.DOCUMENT,
        text = text
    )
}
