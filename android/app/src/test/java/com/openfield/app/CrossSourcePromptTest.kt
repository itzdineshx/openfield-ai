package com.openfield.app

import com.openfield.app.core.model.KnowledgeEvidence
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.feature.ai.mode.PromptBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossSourcePromptTest {

    @Test
    fun buildCrossSourceQaPrompt_formatsAttributionTagsCorrectly() {
        val evidence = listOf(
            KnowledgeEvidence(
                evidenceId = "e1",
                sourceId = "d1",
                sourceTitle = "Manual.pdf",
                sourceType = KnowledgeSourceType.DOCUMENT,
                pageNumber = 3,
                text = "Press power button for 3 seconds."
            ),
            KnowledgeEvidence(
                evidenceId = "e2",
                sourceId = "p1",
                sourceTitle = "FixPack",
                sourceType = KnowledgeSourceType.KNOWLEDGE_PACK,
                section = "Power",
                text = "Ensure battery is charged before power on."
            )
        )

        val prompt = PromptBuilder.buildCrossSourceQaPrompt(
            query = "How do I turn on the device?",
            evidenceList = evidence,
            scopeName = "Device Guides"
        )

        assertTrue(prompt.contains("[Source: Manual.pdf (Page 3)]:"))
        assertTrue(prompt.contains("Press power button for 3 seconds."))
        assertTrue(prompt.contains("[Source: FixPack (Power)]:"))
        assertTrue(prompt.contains("Ensure battery is charged before power on."))
        assertTrue(prompt.contains("Device Guides"))
        assertTrue(prompt.contains("cite the source using: [Source: <Title> (Page <P>)]"))
    }

    @Test
    fun buildCrossSourceQaPrompt_handlesEmptyEvidenceGracefully() {
        val prompt = PromptBuilder.buildCrossSourceQaPrompt(
            query = "Compare X and Y",
            evidenceList = emptyList(),
            scopeName = "Empty Scope"
        )

        assertTrue(prompt.contains("The selected sources do not contain sufficient information"))
        assertTrue(prompt.contains("Compare X and Y"))
    }
}
