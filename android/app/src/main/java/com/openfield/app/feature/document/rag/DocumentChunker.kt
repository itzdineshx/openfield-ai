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

        for (page in pages) {
            val text = page.text.trim()
            if (text.isBlank()) continue

            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (words.size <= targetChunkWords) {
                // Whole page fits in one chunk
                result.add(
                    DocumentChunkEntity(
                        documentId = documentId,
                        pageNumber = page.pageNumber,
                        chunkIndex = chunkGlobalIndex++,
                        text = text
                    )
                )
            } else {
                // Split with overlap
                var startIndex = 0
                while (startIndex < words.size) {
                    val endIndex = (startIndex + targetChunkWords).coerceAtMost(words.size)
                    val chunkText = words.subList(startIndex, endIndex).joinToString(" ")

                    result.add(
                        DocumentChunkEntity(
                            documentId = documentId,
                            pageNumber = page.pageNumber,
                            chunkIndex = chunkGlobalIndex++,
                            text = chunkText
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

