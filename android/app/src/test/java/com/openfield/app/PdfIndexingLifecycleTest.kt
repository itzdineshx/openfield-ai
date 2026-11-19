package com.openfield.app

import com.openfield.app.feature.ai.controller.DocumentIndexingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfIndexingLifecycleTest {

    @Test
    fun indexingState_calculatesProgressFractionCorrectly() {
        var state = DocumentIndexingState(
            isIndexing = true,
            documentName = "Manual.pdf",
            currentStep = "Extracting page 5 of 20",
            processedPages = 5,
            totalPages = 20,
            progressFraction = 5f / 20f
        )

        assertTrue(state.isIndexing)
        assertEquals(0.25f, state.progressFraction, 0.001f)
        assertEquals(5, state.processedPages)
        assertEquals(20, state.totalPages)
        assertNull(state.error)

        // Halfway
        state = state.copy(
            processedPages = 10,
            progressFraction = 10f / 20f
        )
        assertEquals(0.50f, state.progressFraction, 0.001f)

        // Complete
        state = DocumentIndexingState()
        assertFalse(state.isIndexing)
        assertEquals(0f, state.progressFraction, 0.001f)
    }

    @Test
    fun indexingState_errorState_retainsErrorMessage() {
        val errorState = DocumentIndexingState(
            isIndexing = false,
            error = "Password-protected PDF cannot be extracted"
        )

        assertFalse(errorState.isIndexing)
        assertEquals("Password-protected PDF cannot be extracted", errorState.error)
    }
}
