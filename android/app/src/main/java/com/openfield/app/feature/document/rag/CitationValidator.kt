package com.openfield.app.feature.document.rag

import com.openfield.app.core.model.KnowledgeEvidence

data class Citation(
    val rawText: String,
    val sourceTitle: String?,
    val pageNumber: Int?,
    val isValid: Boolean,
    val reason: String? = null
)

data class CitationValidationResult(
    val citations: List<Citation>,
    val validCount: Int,
    val invalidCount: Int,
    val hasHallucinations: Boolean
)

/**
 * Validates post-generation citations against actual evidence passages to prevent fabricated claims.
 */
object CitationValidator {

    private val CITATION_REGEX = Regex("\\[(?:Source:\\s*)?([^\\]\\,]+?)(?:,\\s*Page\\s*(\\d+)|\\s*\\(Page\\s*(\\d+)\\))?\\]|\\(Page\\s*(\\d+)\\)", RegexOption.IGNORE_CASE)

    fun validate(
        responseText: String,
        evidenceList: List<KnowledgeEvidence>
    ): CitationValidationResult {
        val citations = mutableListOf<Citation>()
        val matches = CITATION_REGEX.findAll(responseText)

        val knownSources = evidenceList.map { it.sourceTitle.lowercase() }.toSet()
        val knownPagesBySource = evidenceList.groupBy { it.sourceTitle.lowercase() }
            .mapValues { (_, list) -> list.mapNotNull { it.pageNumber }.toSet() }
        val allKnownPages = evidenceList.mapNotNull { it.pageNumber }.toSet()

        for (match in matches) {
            val raw = match.value
            val sourceName = match.groups[1]?.value?.trim()
            val pageStr = match.groups[2]?.value ?: match.groups[3]?.value ?: match.groups[4]?.value
            val pageNum = pageStr?.toIntOrNull()

            if (sourceName != null && pageNum != null) {
                // Source and page both specified
                val sourceMatch = knownSources.any { it.contains(sourceName.lowercase()) || sourceName.lowercase().contains(it) }
                if (!sourceMatch) {
                    citations.add(Citation(raw, sourceName, pageNum, false, "Unknown source: '$sourceName'"))
                } else {
                    val matchingSourceKey = knownSources.firstOrNull { it.contains(sourceName.lowercase()) || sourceName.lowercase().contains(it) }
                    val validPages = knownPagesBySource[matchingSourceKey] ?: emptySet()
                    if (validPages.isNotEmpty() && pageNum !in validPages) {
                        citations.add(Citation(raw, sourceName, pageNum, false, "Page $pageNum was not in retrieved evidence for '$sourceName'"))
                    } else {
                        citations.add(Citation(raw, sourceName, pageNum, true))
                    }
                }
            } else if (sourceName != null) {
                // Only source specified
                val sourceMatch = knownSources.any { it.contains(sourceName.lowercase()) || sourceName.lowercase().contains(it) }
                if (sourceMatch) {
                    citations.add(Citation(raw, sourceName, null, true))
                } else {
                    citations.add(Citation(raw, sourceName, null, false, "Unknown source: '$sourceName'"))
                }
            } else if (pageNum != null) {
                // Only page specified e.g. (Page 1)
                if (pageNum in allKnownPages) {
                    citations.add(Citation(raw, null, pageNum, true))
                } else {
                    citations.add(Citation(raw, null, pageNum, false, "Page $pageNum was not in retrieved evidence"))
                }
            }
        }

        val validCount = citations.count { it.isValid }
        val invalidCount = citations.count { !it.isValid }

        return CitationValidationResult(
            citations = citations,
            validCount = validCount,
            invalidCount = invalidCount,
            hasHallucinations = invalidCount > 0
        )
    }
}
