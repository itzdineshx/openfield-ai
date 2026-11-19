package com.openfield.app

import com.openfield.app.core.util.PerformanceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceTrackerTest {

    @Test
    fun recordColdInit_updatesColdInitTimeAndBackend() {
        PerformanceTracker.recordColdInit(durationMs = 1250L, backend = "CPU")
        val metrics = PerformanceTracker.metrics.value
        assertEquals(1250L, metrics.coldInitTimeMs)
        assertEquals("CPU", metrics.activeBackend)
    }

    @Test
    fun recordTimeToFirstToken_updatesTtft() {
        PerformanceTracker.recordTimeToFirstToken(340L)
        val metrics = PerformanceTracker.metrics.value
        assertEquals(340L, metrics.lastTimeToFirstTokenMs)
    }

    @Test
    fun recordGenerationStats_computesTokensPerSecondCorrectly() {
        // 50 tokens generated over 2500 ms (2.5 seconds) -> 20.0 tokens/second
        PerformanceTracker.recordGenerationStats(totalTokens = 50, durationMs = 2500L)
        val metrics = PerformanceTracker.metrics.value
        assertEquals(2500L, metrics.lastGenerationDurationMs)
        assertEquals(20.0, metrics.lastTokensPerSec, 0.1)
    }

    @Test
    fun recordLatencies_updatesIndividualPipelineStages() {
        PerformanceTracker.recordImagePreprocessing(45L)
        PerformanceTracker.recordDocumentExtraction(120L)
        PerformanceTracker.recordRetrievalLatency(18L)

        val metrics = PerformanceTracker.metrics.value
        assertEquals(45L, metrics.lastImagePreprocessingMs)
        assertEquals(120L, metrics.lastDocumentExtractionMs)
        assertEquals(18L, metrics.lastRetrievalLatencyMs)
    }

    @Test
    fun updateMemory_computesValidHeapValues() {
        PerformanceTracker.updateMemory()
        val metrics = PerformanceTracker.metrics.value
        assertTrue(metrics.maxAvailableMemoryMb > 0)
        assertTrue(metrics.currentMemoryUsedMb >= 0)
    }
}
