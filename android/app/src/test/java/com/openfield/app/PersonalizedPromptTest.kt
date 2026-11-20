package com.openfield.app

import com.openfield.app.feature.ai.mode.PromptBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizedPromptTest {

    @Test
    fun buildAskAiPrompt_withExplanationLevel_injectsCorrectDirective() {
        val beginnerPrompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "Explain quantum superposition",
            explanationLevel = "Beginner"
        )
        assertTrue(beginnerPrompt.contains("beginner without heavy technical jargon"))

        val advancedPrompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "Explain quantum superposition",
            explanationLevel = "Advanced"
        )
        assertTrue(advancedPrompt.contains("advanced, technically rigorous"))
    }

    @Test
    fun buildAskAiPrompt_withMeasurementUnits_injectsCorrectDirective() {
        val metricPrompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "How high is Mt Everest?",
            measurementUnits = "Metric"
        )
        assertTrue(metricPrompt.contains("Use Metric measurement units"))

        val imperialPrompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "How high is Mt Everest?",
            measurementUnits = "Imperial"
        )
        assertTrue(imperialPrompt.contains("Use Imperial measurement units"))
    }

    @Test
    fun buildAskAiPrompt_withWritingToneAndFormatting_injectsDirectives() {
        val prompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "Summarize project milestones",
            writingTone = "Technical",
            formattingStyle = "Bullet Points"
        )
        assertTrue(prompt.contains("Tone: Technical."))
        assertTrue(prompt.contains("Format with Bullet Points."))
    }

    @Test
    fun boundContext_shortText_remainsUnchanged() {
        val short = "Short context snippet."
        assertEquals(short, PromptBuilder.boundContext(short, maxChars = 100))
    }

    @Test
    fun boundContext_longText_truncatedWithEllipsis() {
        val longText = "A".repeat(500)
        val bounded = PromptBuilder.boundContext(longText, maxChars = 50)
        assertEquals(50, bounded.length)
        assertTrue(bounded.endsWith("..."))
    }
}
