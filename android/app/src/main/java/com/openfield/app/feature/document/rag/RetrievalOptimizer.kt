package com.openfield.app.feature.document.rag

import com.openfield.app.core.model.KnowledgeEvidence

/**
 * Optimizes local retrieval relevance, heading awareness, and fair-share cross-source interleaving (Phase 3 Gate 7).
 * Zero cloud dependency.
 */
object RetrievalOptimizer {

    private val SYNONYM_MAP = mapOf(
        "spec" to listOf("specification", "specs"),
        "doc" to listOf("document", "documentation"),
        "ai" to listOf("artificial intelligence", "model"),
        "ml" to listOf("machine learning"),
        "db" to listOf("database", "sqlite"),
        "hw" to listOf("hardware"),
        "sw" to listOf("software"),
        "os" to listOf("operating system"),
        "exam" to listOf("test", "examination"),
        "eval" to listOf("evaluation", "benchmark"),
        "calc" to listOf("calculation", "calculator"),
        "carb" to listOf("carburetor"),
        "fix" to listOf("repair", "troubleshoot", "solution"),
        "err" to listOf("error", "fault", "failure"),
        "setup" to listOf("install", "installation", "configure")
    )

    /**
     * Expands query terms using offline dictionary of technical, academic, and document synonyms.
     */
    fun expandQueryTerms(terms: List<String>): List<String> {
        val expanded = mutableSetOf<String>()
        for (term in terms) {
            val lower = term.lowercase()
            expanded.add(lower)
            SYNONYM_MAP[lower]?.let { expanded.addAll(it) }
        }
        return expanded.toList()
    }

    /**
     * Scores a text chunk's relevance with heading awareness and density weighting.
     */
    fun scoreChunk(text: String, terms: List<String>): Float {
        if (text.isBlank() || terms.isEmpty()) return 0f
        val lower = text.lowercase()
        var matchCount = 0
        var headingBoost = 0f

        val lines = text.lines()
        for (term in terms) {
            if (lower.contains(term)) {
                matchCount++
            }
            // Check if any heading contains the query term
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("#") && trimmed.lowercase().contains(term)) {
                    headingBoost += 0.5f
                    break
                }
            }
        }

        val baseScore = matchCount.toFloat() / terms.size.toFloat()
        return baseScore + headingBoost
    }

    /**
     * Interleaves evidence passages across disparate sources in round-robin order to ensure fair representation
     * and prevent a single dense document from monopolizing the prompt context budget.
     */
    fun fairShareInterleave(
        passagesBySource: Map<String, List<KnowledgeEvidence>>,
        maxTotal: Int
    ): List<KnowledgeEvidence> {
        val result = mutableListOf<KnowledgeEvidence>()
        val sources = passagesBySource.keys.toList()
        if (sources.isEmpty()) return emptyList()

        val indices = sources.associateWith { 0 }.toMutableMap()
        var addedInCycle = true

        while (result.size < maxTotal && addedInCycle) {
            addedInCycle = false
            for (sourceId in sources) {
                if (result.size >= maxTotal) break
                val list = passagesBySource[sourceId] ?: continue
                val idx = indices[sourceId] ?: 0
                if (idx < list.size) {
                    result.add(list[idx])
                    indices[sourceId] = idx + 1
                    addedInCycle = true
                }
            }
        }

        return result
    }
}
