package com.openfield.app

import com.openfield.app.feature.document.rag.DocumentChunker
import com.openfield.app.feature.document.rag.ExtractedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentChunkerTest {

    private val chunker = DocumentChunker(targetChunkWords = 50, overlapWords = 10)

    @Test
    fun chunkDocument_singleSmallPage_returnsOneChunk() {
        val pages = listOf(
            ExtractedPage(pageNumber = 1, text = "This is a brief device manual covering basic setup and safety guidelines.")
        )
        val chunks = chunker.chunkDocument("doc_1", pages)

        assertEquals(1, chunks.size)
        assertEquals("doc_1", chunks[0].documentId)
        assertEquals(1, chunks[0].pageNumber)
        assertEquals(0, chunks[0].chunkIndex)
        assertTrue(chunks[0].text.contains("device manual"))
    }

    @Test
    fun chunkDocument_blankPage_isSkipped() {
        val pages = listOf(
            ExtractedPage(pageNumber = 1, text = "   \n\t  "),
            ExtractedPage(pageNumber = 2, text = "Page two has valid text content.")
        )
        val chunks = chunker.chunkDocument("doc_2", pages)

        assertEquals(1, chunks.size)
        assertEquals(2, chunks[0].pageNumber)
    }

    @Test
    fun chunkDocument_largePage_splitsWithOverlap() {
        // Generate a 120-word page (target is 50 words, overlap 10)
        val words = (1..120).map { "word$it" }.joinToString(" ")
        val pages = listOf(ExtractedPage(pageNumber = 5, text = words))

        val chunks = chunker.chunkDocument("doc_3", pages)

        assertTrue(chunks.size > 1)
        assertEquals(5, chunks[0].pageNumber)
        assertEquals(5, chunks[1].pageNumber)
        assertEquals(0, chunks[0].chunkIndex)
        assertEquals(1, chunks[1].chunkIndex)
    }

    @Test
    fun chunkDocument_multiplePages_tagsCorrectPageNumbers() {
        val pages = listOf(
            ExtractedPage(pageNumber = 1, text = "Chapter 1: Getting Started and Specifications."),
            ExtractedPage(pageNumber = 42, text = "Chapter 5: System Recovery and Factory Reset Options.")
        )
        val chunks = chunker.chunkDocument("doc_4", pages)

        assertEquals(2, chunks.size)
        assertEquals(1, chunks[0].pageNumber)
        assertEquals(42, chunks[1].pageNumber)
    }
}
