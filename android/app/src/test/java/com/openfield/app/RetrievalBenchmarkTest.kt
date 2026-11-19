package com.openfield.app

import com.openfield.app.feature.document.rag.DocumentChunker
import com.openfield.app.feature.document.rag.RetrievalBenchmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrievalBenchmarkTest {

    @Test
    fun benchmark_evaluatesFixedDataset_satisfiesQualityThresholds() {
        val chunker = DocumentChunker(targetChunkWords = 80, overlapWords = 15)
        val result = RetrievalBenchmark.evaluate(chunker)

        assertEquals(3, result.totalQueries)
        assertTrue("MRR should be >= 0.8 on fixed benchmark dataset", result.meanReciprocalRank >= 0.8)
        assertTrue("Precision@1 should be >= 0.6", result.precisionAt1 >= 0.6)
        assertTrue("Precision@3 should be >= 0.3", result.precisionAt3 >= 0.3)
    }

    @Test
    fun fixedDataset_chunksPreserveSectionHeadings() {
        val chunker = DocumentChunker(targetChunkWords = 100, overlapWords = 20)
        val chunks = chunker.chunkDocument("benchmark_doc", RetrievalBenchmark.FIXED_TEST_DATASET)

        assertTrue(chunks.isNotEmpty())
        val chunk1 = chunks.find { it.pageNumber == 1 }
        val chunk2 = chunks.find { it.pageNumber == 2 }
        val chunk3 = chunks.find { it.pageNumber == 3 }

        assertTrue(chunk1?.text?.contains("[Section: 1. Linux Kernel Memory Management]") == true)
        assertTrue(chunk2?.text?.contains("[Section: 2. Electrical Circuit Protection & Breaker Sizing]") == true)
        assertTrue(chunk3?.text?.contains("[Section: 3. SQLite Storage Architecture & Write-Ahead Logging]") == true)
    }
}
