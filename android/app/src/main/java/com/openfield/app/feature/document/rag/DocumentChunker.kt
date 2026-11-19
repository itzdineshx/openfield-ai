package com.openfield.app.feature.document.rag

import com.openfield.app.data.local.entity.DocumentChunkEntity

class DocumentChunker(
    private val targetChunkWords: Int = 300,
    private val overlapWords: Int = 50
) {

    fun chunkDocument(
        documentId: String,
        pages: List<ExtractedPage>
    ): List<DocumentChunkEntity> {
        val result = mutableListOf<DocumentChunkEntity>()
        var chunkGlobalIndex = 0
        var currentHeading: String? = null

        for (page in pages) {
            val text = page.text.trim()
            if (text.isBlank()) continue

            // Detect heading on the page if present
            val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
            val detectedHeading = lines.find { line ->
                line.startsWith("#") ||
                line.matches(Regex("^(?:Chapter|Section|Part|Article|Module)\\s+[0-9IVXLCDM]+.*", RegexOption.IGNORE_CASE)) ||
                (line.length in 4..60 && line.endsWith(":") && !line.contains(".")) ||
                (line.length in 4..50 && line == line.uppercase() && line.any { it.isLetter() })
            }
            if (detectedHeading != null) {
                currentHeading = detectedHeading.replace(Regex("^#+\\s*"), "").trim()
            }

            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (words.size <= targetChunkWords) {
                // Whole page fits in one chunk
                val chunkContent = if (currentHeading != null && !text.startsWith("[Section:")) {
                    "[Section: $currentHeading]\n$text"
                } else {
                    text
                }

                result.add(
                    DocumentChunkEntity(
                        documentId = documentId,
                        pageNumber = page.pageNumber,
                        chunkIndex = chunkGlobalIndex++,
                        text = chunkContent
                    )
                )
            } else {
                // Split with overlap
                var startIndex = 0
                while (startIndex < words.size) {
                    val endIndex = (startIndex + targetChunkWords).coerceAtMost(words.size)
                    val rawChunkText = words.subList(startIndex, endIndex).joinToString(" ")
                    val chunkContent = if (currentHeading != null && !rawChunkText.startsWith("[Section:")) {
                        "[Section: $currentHeading]\n$rawChunkText"
                    } else {
                        rawChunkText
                    }

                    result.add(
                        DocumentChunkEntity(
                            documentId = documentId,
                            pageNumber = page.pageNumber,
                            chunkIndex = chunkGlobalIndex++,
                            text = chunkContent
                        )
                    )

                    if (endIndex == words.size) break
                    startIndex += (targetChunkWords - overlapWords).coerceAtLeast(1)
                }
            }
        }

        return result
    }
}
