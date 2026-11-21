package com.openfield.app

import com.openfield.app.feature.ai.context.ContextWindowOptimizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextWindowOptimizerTest {

    @Test
    fun testEstimateTokensHandlesEmptyAndNonEmpty() {
        assertEquals(0, ContextWindowOptimizer.estimateTokens(""))
        val tokens = ContextWindowOptimizer.estimateTokens("Hello world! This is a test of token estimation.")
        assertTrue("Estimated tokens should be positive and reasonable", tokens in 8..18)
    }

    @Test
    fun testSmartTrimDoesNotTruncateShortText() {
        val short = "This is a short text."
        val trimmed = ContextWindowOptimizer.smartTrim(short, maxTokens = 100)
        assertEquals(short, trimmed)
    }

    @Test
    fun testSmartTrimTruncatesAtSentenceBoundary() {
        val text = "First sentence is clear. Second sentence has facts. Third sentence explains things. Fourth sentence concludes."
        val trimmed = ContextWindowOptimizer.smartTrim(text, maxTokens = 12)
        assertTrue("Should end with sentence period or ellipsis", trimmed.endsWith(".") || trimmed.endsWith("..."))
        assertFalse("Should not cut mid-word", trimmed.contains("sentenc..."))
    }

    @Test
    fun testMergeOverlappingChunksEliminatesRedundancy() {
        val passage1 = "The cooling compressor operates on a 12V DC power cycle with thermal regulation."
        val passage2 = "with thermal regulation. When temperature rises above 4C, the relay triggers cooling."

        val chunks = listOf(
            Pair(1, passage1),
            Pair(1, passage2)
        )

        val merged = ContextWindowOptimizer.mergeOverlappingChunks(chunks)
        assertEquals(1, merged.size)
        val combinedText = merged.first().second
        assertTrue(combinedText.contains("12V DC power cycle"))
        assertTrue(combinedText.contains("relay triggers cooling"))
        // Check that "with thermal regulation." is not duplicated consecutively
        val occurrences = Regex("with thermal regulation").findAll(combinedText).count()
        assertEquals(1, occurrences)
    }

    @Test
    fun testPackEvidenceChunksWithinBudget() {
        val chunks = listOf(
            "Short chunk A",
            "Medium chunk B with slightly more information and descriptive data.",
            "Long chunk C with extensive technical descriptions, specifications, numbers, and details.",
            "Chunk D overflow"
        )

        val packed = ContextWindowOptimizer.packEvidenceChunks(chunks, maxTokens = 25) { it }
        assertTrue("Should pack at least first 2 chunks", packed.size >= 2)
        assertTrue("Should not exceed budget", packed.size < chunks.size)
    }

    @Test
    fun testCompactConversationHistoryPreservesFirstTurn() {
        val turns = listOf(
            Pair(true, "How do I troubleshoot an overheating alternator?"),
            Pair(false, "First check the belt tension and ensure no slipping."),
            Pair(true, "Belt is tight. What next?"),
            Pair(false, "Inspect the diode rectifier using a multimeter on diode mode."),
            Pair(true, "Diodes check out fine. Voltage is 14.8V at idle.")
        )

        val compacted = ContextWindowOptimizer.compactConversationHistory(turns, maxHistoryTokens = 30)
        assertTrue("Compacted history should retain initial user query", compacted.first().second.contains("overheating alternator"))
        assertTrue("Compacted history should retain latest turn", compacted.last().second.contains("14.8V at idle"))
    }
}
