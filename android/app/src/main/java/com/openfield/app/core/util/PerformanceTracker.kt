package com.openfield.app.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Snapshot of real-time on-device performance benchmarks.
 */
data class PerformanceMetrics(
    val coldInitTimeMs: Long = 0L,
    val lastTimeToFirstTokenMs: Long = 0L,
    val lastTokensPerSec: Double = 0.0,
    val lastGenerationDurationMs: Long = 0L,
    val lastImagePreprocessingMs: Long = 0L,
    val lastDocumentExtractionMs: Long = 0L,
    val lastRetrievalLatencyMs: Long = 0L,
    val currentMemoryUsedMb: Long = 0L,
    val maxAvailableMemoryMb: Long = 0L,
    val activeBackend: String = "CPU"
)

/**
 * Centralized, zero-telemetry in-memory performance instrumentation tracker.
 * Measures cold load time, TTFT, token generation speed, retrieval latency, and RAM usage on device.
 */
object PerformanceTracker {

    private val _metrics = MutableStateFlow(getInitialMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()

    private fun getInitialMetrics(): PerformanceMetrics {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val max = runtime.maxMemory() / (1024 * 1024)
        return PerformanceMetrics(
            currentMemoryUsedMb = used,
            maxAvailableMemoryMb = max
        )
    }

    fun recordColdInit(durationMs: Long, backend: String = "CPU") {
        updateMemory()
        _metrics.update {
            it.copy(
                coldInitTimeMs = durationMs,
                activeBackend = backend
            )
        }
    }

    fun recordTimeToFirstToken(ttftMs: Long) {
        _metrics.update {
            it.copy(lastTimeToFirstTokenMs = ttftMs)
        }
    }

    fun recordGenerationStats(totalTokens: Int, durationMs: Long) {
        updateMemory()
        val tokensPerSec = if (durationMs > 0) {
            (totalTokens.toDouble() / (durationMs.toDouble() / 1000.0))
        } else 0.0

        _metrics.update {
            it.copy(
                lastTokensPerSec = String.format(java.util.Locale.US, "%.1f", tokensPerSec).toDoubleOrNull() ?: tokensPerSec,
                lastGenerationDurationMs = durationMs
            )
        }
    }

    fun recordImagePreprocessing(durationMs: Long) {
        updateMemory()
        _metrics.update {
            it.copy(lastImagePreprocessingMs = durationMs)
        }
    }

    fun recordDocumentExtraction(durationMs: Long) {
        updateMemory()
        _metrics.update {
            it.copy(lastDocumentExtractionMs = durationMs)
        }
    }

    fun recordRetrievalLatency(latencyMs: Long) {
        _metrics.update {
            it.copy(lastRetrievalLatencyMs = latencyMs)
        }
    }

    fun updateMemory() {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val max = runtime.maxMemory() / (1024 * 1024)
        _metrics.update {
            it.copy(
                currentMemoryUsedMb = used,
                maxAvailableMemoryMb = max
            )
        }
    }
}
