package com.openfield.app

import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.PromptBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptEfficiencyTest {

    @Test
    fun askAiPrompt_leanAndBounded() {
        val prompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "What is an alternator?",
            language = "English",
            responseLength = "concise",
            customMemory = null
        )
        // Verify no redundant language directive when English
        assertFalse(prompt.contains("Respond in English"))
        assertTrue(prompt.contains("Keep response brief"))
        assertTrue(prompt.contains("What is an alternator?"))
        // Bounded size
        assertTrue(prompt.length < 150)
    }

    @Test
    fun documentQaPrompt_preservesStrictRulesAndPageLabels() {
        val excerpts = listOf(
            1 to "The primary pump delivers 50 PSI pressure.",
            2 to "Emergency shutoff valve is located on the secondary line."
        )
        val prompt = PromptBuilder.buildDocumentQaPrompt("Pump_Manual.pdf", excerpts, "What is the pump pressure?")

        assertTrue(prompt.contains("Pump_Manual.pdf"))
        assertTrue(prompt.contains("[Page 1]:"))
        assertTrue(prompt.contains("[Page 2]:"))
        assertTrue(prompt.contains("50 PSI"))
        assertTrue(prompt.contains("RULES:"))
        assertTrue(prompt.contains("Never invent page numbers"))
    }

    @Test
    fun troubleshootIntakePrompt_hasCompactJsonTemplate() {
        val prompt = PromptBuilder.buildTroubleshootIntakePrompt("Engine won't start, clicking sound")

        assertTrue(prompt.contains("category"))
        assertTrue(prompt.contains("safetyNotice"))
        assertTrue(prompt.contains("currentStepNumber"))
        assertTrue(prompt.contains("options"))
        assertTrue(prompt.contains("Engine won't start, clicking sound"))
        // Token efficiency: template length must be compact (< 1600 characters)
        assertTrue(prompt.length < 1600)
    }

    @Test
    fun learningLessonPrompt_hasCompleteSchemaWithinBudget() {
        val prompt = PromptBuilder.buildLearningLessonPrompt("Photosynthesis", LearningLevel.BEGINNER)

        assertTrue(prompt.contains("Photosynthesis"))
        assertTrue(prompt.contains("Beginner"))
        assertTrue(prompt.contains("sections"))
        assertTrue(prompt.contains("practicalExample"))
        assertTrue(prompt.contains("quiz"))
        assertTrue(prompt.contains("correctOptionIndex"))
        assertTrue(prompt.length < 1500)
    }
}
