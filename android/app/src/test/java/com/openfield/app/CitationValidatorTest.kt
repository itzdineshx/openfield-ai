package com.openfield.app

import com.openfield.app.core.model.KnowledgeEvidence
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.feature.document.rag.CitationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CitationValidatorTest {

    private val sampleEvidence = listOf(
        KnowledgeEvidence(
            evidenceId = "ev_1",
            sourceId = "doc_1",
            sourceTitle = "AnyScanner_Manual.pdf",
            sourceType = KnowledgeSourceType.DOCUMENT,
            pageNumber = 1,
            text = "To scan a document, tap the camera icon on the main screen.",
            relevanceScore = 0.95f
        ),
        KnowledgeEvidence(
            evidenceId = "ev_2",
            sourceId = "doc_1",
            sourceTitle = "AnyScanner_Manual.pdf",
            sourceType = KnowledgeSourceType.DOCUMENT,
            pageNumber = 2,
            text = "Export documents by tapping Share and selecting PDF or JPG format.",
            relevanceScore = 0.90f
        ),
        KnowledgeEvidence(
            evidenceId = "ev_3",
            sourceId = "pack_1",
            sourceTitle = "Devices & Repairs",
            sourceType = KnowledgeSourceType.KNOWLEDGE_PACK,
            section = "Hardware",
            text = "Check the cable connection before replacing the scanner module.",
            relevanceScore = 0.85f
        )
    )

    @Test
    fun validate_validSourceAndPageCitation() {
        val response = "According to [Source: AnyScanner_Manual.pdf, Page 2], you can export as PDF."
        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(1, result.validCount)
        assertEquals(0, result.invalidCount)
        assertFalse(result.hasHallucinations)
        assertTrue(result.citations.first().isValid)
        assertEquals("AnyScanner_Manual.pdf", result.citations.first().sourceTitle)
        assertEquals(2, result.citations.first().pageNumber)
    }

    @Test
    fun validate_validParenthesizedCitation() {
        val response = "Export formats include PDF and JPG [AnyScanner_Manual.pdf (Page 2)]."
        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(1, result.validCount)
        assertEquals(0, result.invalidCount)
        assertFalse(result.hasHallucinations)
        assertTrue(result.citations.first().isValid)
    }

    @Test
    fun validate_validPageOnlyCitation() {
        val response = "To scan, use the camera icon (Page 1)."
        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(1, result.validCount)
        assertEquals(0, result.invalidCount)
        assertFalse(result.hasHallucinations)
        assertTrue(result.citations.first().isValid)
    }

    @Test
    fun validate_hallucinatedPageNumber_isFlaggedInvalid() {
        val response = "The advanced settings are explained in [Source: AnyScanner_Manual.pdf, Page 99]."
        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(0, result.validCount)
        assertEquals(1, result.invalidCount)
        assertTrue(result.hasHallucinations)
        assertFalse(result.citations.first().isValid)
        assertTrue(result.citations.first().reason!!.contains("Page 99 was not in retrieved evidence"))
    }

    @Test
    fun validate_hallucinatedSource_isFlaggedInvalid() {
        val response = "Per [Source: NonExistentHandbook.pdf, Page 1], always wear gloves."
        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(0, result.validCount)
        assertEquals(1, result.invalidCount)
        assertTrue(result.hasHallucinations)
        assertFalse(result.citations.first().isValid)
        assertTrue(result.citations.first().reason!!.contains("Unknown source"))
    }

    @Test
    fun validate_multipleCitationsWithMixedValidity() {
        val response = """
            You should first check the cable [Source: Devices & Repairs].
            Then export your scan [Source: AnyScanner_Manual.pdf, Page 2].
            Finally consult the warranty [Source: FakeDoc.pdf, Page 5].
        """.trimIndent()

        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(2, result.validCount)
        assertEquals(1, result.invalidCount)
        assertTrue(result.hasHallucinations)
    }

    @Test
    fun validate_noCitations_returnsEmptyWithoutHallucinationFlag() {
        val response = "This is a direct factual answer with no inline citations."
        val result = CitationValidator.validate(response, sampleEvidence)

        assertEquals(0, result.validCount)
        assertEquals(0, result.invalidCount)
        assertFalse(result.hasHallucinations)
        assertTrue(result.citations.isEmpty())
    }
}
