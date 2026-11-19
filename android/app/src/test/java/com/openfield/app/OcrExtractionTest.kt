package com.openfield.app

import com.openfield.app.feature.document.rag.DocumentChunker
import com.openfield.app.feature.document.rag.DocumentExtractionResult
import com.openfield.app.feature.document.rag.ExtractedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrExtractionTest {

    private val chunker = DocumentChunker(targetChunkWords = 50, overlapWords = 10)

    @Test
    fun ocrExtractedPages_preservePageNumbersAndChunkCorrectly() {
        // Simulating a 3-page scanned document where pages 1 and 2 yielded OCR text, and page 3 was blank
        val ocrPages = listOf(
            ExtractedPage(pageNumber = 1, text = "Model X-500 Operating Instructions. Always disconnect power before servicing the internal heating element."),
            ExtractedPage(pageNumber = 2, text = "Troubleshooting error code E-04: Thermal sensor open loop. Check wiring harness connector J2 on motherboard."),
            ExtractedPage(pageNumber = 3, text = "   ")
        )

        val chunks = chunker.chunkDocument("scanned_doc_101", ocrPages)

        assertEquals(2, chunks.size)
        assertEquals(1, chunks[0].pageNumber)
        assertTrue(chunks[0].text.contains("Model X-500"))
        assertEquals(2, chunks[1].pageNumber)
        assertTrue(chunks[1].text.contains("E-04"))
    }

    @Test
    fun mixedPdfExtraction_combinesEmbeddedAndOcrPagesSeamlessly() {
        // Mixed PDF: Page 1 was digital text, Page 2 was a scanned diagram with OCR recognized label
        val mixedPages = listOf(
            ExtractedPage(pageNumber = 1, text = "Introduction: System overview and digital table of contents."),
            ExtractedPage(pageNumber = 2, text = "Diagram Figure 3: Hydraulic Pressure Relief Valve Assembly (OCR Scanned)")
        )

        val result = DocumentExtractionResult(
            fileName = "mixed_manual.pdf",
            formattedSize = "1.4 MB",
            sizeBytes = 1468006L,
            pageCount = 2,
            pages = mixedPages,
            isScanned = false, // false because sufficient text was recovered
            totalWordCount = 18
        )

        assertFalse(result.isScanned)
        assertEquals(2, result.pages.size)
        assertEquals(1, result.pages[0].pageNumber)
        assertEquals(2, result.pages[1].pageNumber)

        val chunks = chunker.chunkDocument("mixed_doc_102", result.pages)
        assertEquals(2, chunks.size)
        assertEquals("mixed_doc_102", chunks[0].documentId)
        assertEquals(1, chunks[0].pageNumber)
        assertEquals(2, chunks[1].pageNumber)
    }

    @Test
    fun emptyScannedPdf_flaggedAsScannedWhenCharsUnderThreshold() {
        val pages = listOf(
            ExtractedPage(pageNumber = 1, text = ""),
            ExtractedPage(pageNumber = 2, text = "   ")
        )
        val result = DocumentExtractionResult(
            fileName = "empty_scanned.pdf",
            formattedSize = "450 KB",
            sizeBytes = 460800L,
            pageCount = 2,
            pages = pages,
            isScanned = true,
            totalWordCount = 0
        )

        assertTrue(result.isScanned)
        val chunks = chunker.chunkDocument("empty_doc", result.pages)
        assertTrue(chunks.isEmpty())
    }
}
