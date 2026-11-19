package com.openfield.app.feature.document.rag

/**
 * Benchmark data point representing a test query and its expected ground-truth keywords/section matches.
 */
data class BenchmarkQuery(
    val query: String,
    val expectedSectionKeywords: List<String>
)

/**
 * Result of a retrieval benchmark run across a fixed local test dataset.
 */
data class BenchmarkResult(
    val totalQueries: Int,
    val meanReciprocalRank: Double,
    val precisionAt1: Double,
    val precisionAt3: Double,
    val averageLatencyMs: Long
)

/**
 * Offline evaluation utility to benchmark retrieval quality using a fixed local test dataset.
 */
object RetrievalBenchmark {

    val FIXED_TEST_DATASET = listOf(
        ExtractedPage(
            pageNumber = 1,
            text = """
                # 1. Linux Kernel Memory Management
                The Linux kernel manages physical memory using page frames, typically 4KB in size.
                Virtual memory provides each process with its own address space.
                When physical memory pressure occurs, the kernel page reclamation subsystem invokes kswapd.
                Swapping moves anonymous memory pages to designated swap partitions or swap files on disk.
            """.trimIndent()
        ),
        ExtractedPage(
            pageNumber = 2,
            text = """
                # 2. Electrical Circuit Protection & Breaker Sizing
                Circuit breakers protect electrical wiring from thermal overload and short circuits.
                Standard residential circuits use 15-amp breakers with 14 AWG copper wire or 20-amp breakers with 12 AWG wire.
                Continuous loads must not exceed 80% of the circuit's rated capacity according to electrical safety standards.
                Ground Fault Circuit Interrupters (GFCI) detect current leakage to ground and trip within milliseconds.
            """.trimIndent()
        ),
        ExtractedPage(
            pageNumber = 3,
            text = """
                # 3. SQLite Storage Architecture & Write-Ahead Logging
                SQLite databases store data in a single cross-platform disk file structured into B-tree pages.
                In Write-Ahead Logging (WAL) mode, changes are appended to a separate -wal file instead of modifying the main database directly.
                This enables concurrent readers while a write transaction is occurring.
                Checkpointing periodically transfers committed WAL transactions back into the main database page cache.
            """.trimIndent()
        )
    )

    val BENCHMARK_QUERIES = listOf(
        BenchmarkQuery(
            query = "How does Linux handle memory pressure with swap?",
            expectedSectionKeywords = listOf("kswapd", "swap", "memory pressure", "kernel")
        ),
        BenchmarkQuery(
            query = "What size wire and breaker is required for 20 amp circuits?",
            expectedSectionKeywords = listOf("20-amp", "12 AWG", "circuit breaker", "wire")
        ),
        BenchmarkQuery(
            query = "How does SQLite WAL mode support concurrent reading and writing?",
            expectedSectionKeywords = listOf("WAL", "Write-Ahead", "concurrent", "checkpoint")
        )
    )

    /**
     * Executes the retrieval benchmark using DocumentChunker on the fixed dataset.
     */
    fun evaluate(chunker: DocumentChunker = DocumentChunker()): BenchmarkResult {
        val chunks = chunker.chunkDocument("benchmark_doc", FIXED_TEST_DATASET)
        var reciprocalRankSum = 0.0
        var pAt1Sum = 0
        var pAt3Sum = 0
        var totalLatency = 0L

        for (item in BENCHMARK_QUERIES) {
            val startTime = System.nanoTime()

            // Keyword relevance scoring based on query terms
            val terms = item.query.lowercase().split(Regex("\\s+")).filter { it.length > 3 }
            val scored = chunks.map { chunk ->
                val textLower = chunk.text.lowercase()
                val score = terms.count { textLower.contains(it) }
                chunk to score
            }.sortedByDescending { it.second }

            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
            totalLatency += elapsedMs

            // Evaluate relevance against expected keywords
            val topResults = scored.take(3).map { it.first }
            var firstRelevantRank = 0

            topResults.forEachIndexed { index, candidate ->
                val candLower = candidate.text.lowercase()
                val isRelevant = item.expectedSectionKeywords.any { candLower.contains(it.lowercase()) }
                if (isRelevant && firstRelevantRank == 0) {
                    firstRelevantRank = index + 1
                }
                if (isRelevant) {
                    if (index == 0) pAt1Sum++
                    pAt3Sum++
                }
            }

            if (firstRelevantRank > 0) {
                reciprocalRankSum += (1.0 / firstRelevantRank)
            }
        }

        val n = BENCHMARK_QUERIES.size
        return BenchmarkResult(
            totalQueries = n,
            meanReciprocalRank = if (n > 0) reciprocalRankSum / n else 0.0,
            precisionAt1 = if (n > 0) pAt1Sum.toDouble() / n else 0.0,
            precisionAt3 = if (n > 0) pAt3Sum.toDouble() / (n * 3) else 0.0,
            averageLatencyMs = if (n > 0) totalLatency / n else 0L
        )
    }
}
