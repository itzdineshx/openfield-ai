package com.openfield.app

import com.openfield.app.feature.ai.mode.DiagnosticStepHistory
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.PromptBuilder
import com.openfield.app.feature.ai.mode.WritingIntent
import com.openfield.app.feature.ai.mode.WritingTone
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun buildAskAiPrompt_trimsWhitespace() {
        val prompt = PromptBuilder.buildAskAiPrompt("   Hello Gemma   ")
        assertTrue(prompt == "Hello Gemma")
    }

    @Test
    fun buildImageAnalysisPrompt_includesExpectedSections() {
        val prompt = PromptBuilder.buildImageAnalysisPrompt(null)
        assertTrue(prompt.contains("What I See"))
        assertTrue(prompt.contains("Important Details"))
        assertTrue(prompt.contains("Explanation"))
        assertTrue(prompt.contains("Possible Identification"))
        assertTrue(prompt.contains("Suggested Next Steps"))
        assertTrue(prompt.contains("Uncertainty & Limitations"))
    }

    @Test
    fun buildImageAnalysisPrompt_withCustomQuestion_includesQuery() {
        val prompt = PromptBuilder.buildImageAnalysisPrompt("Is this mushroom edible?")
        assertTrue(prompt.contains("Is this mushroom edible?"))
    }

    @Test
    fun buildImageFollowUpPrompt_incorporatesSummary() {
        val prompt = PromptBuilder.buildImageFollowUpPrompt(
            userQuestion = "What genus does this belong to?",
            priorSummary = "Subject: Amanita muscaria"
        )
        assertTrue(prompt.contains("Amanita muscaria"))
        assertTrue(prompt.contains("What genus does this belong to?"))
    }

    @Test
    fun buildDocumentQaPrompt_includesExcerptsAndCitations() {
        val excerpts = listOf(
            1 to "The safety switch is located on the rear panel.",
            4 to "Fuse rating must not exceed 10 amperes."
        )
        val prompt = PromptBuilder.buildDocumentQaPrompt(
            documentName = "Device_Manual.pdf",
            excerptsWithPages = excerpts,
            question = "Where is the safety switch?"
        )
        assertTrue(prompt.contains("Device_Manual.pdf"))
        assertTrue(prompt.contains("[Page 1]"))
        assertTrue(prompt.contains("[Page 4]"))
        assertTrue(prompt.contains("Where is the safety switch?"))
        assertTrue(prompt.contains("Ground your answer strictly on the provided excerpts"))
    }

    @Test
    fun buildTroubleshootIntakePrompt_requestsJsonSchema() {
        val prompt = PromptBuilder.buildTroubleshootIntakePrompt("Car clicking but engine won't turn over")
        assertTrue(prompt.contains("Car clicking but engine won't turn over"))
        assertTrue(prompt.contains("\"category\""))
        assertTrue(prompt.contains("\"options\""))
        assertTrue(prompt.contains("\"safetyNotice\""))
    }

    @Test
    fun buildTroubleshootNextStepPrompt_formatsHistory() {
        val history = listOf(
            DiagnosticStepHistory(1, "What happens when you turn key?", "Rapid clicking"),
            DiagnosticStepHistory(2, "Do the dashboard lights dim?", "Yes, completely dark")
        )
        val prompt = PromptBuilder.buildTroubleshootNextStepPrompt(
            problemDescription = "Car won't start",
            history = history
        )
        assertTrue(prompt.contains("Step 1: \"What happens when you turn key?\" -> User answered: \"Rapid clicking\""))
        assertTrue(prompt.contains("Step 2: \"Do the dashboard lights dim?\" -> User answered: \"Yes, completely dark\""))
    }

    @Test
    fun buildLearningLessonPrompt_reflectsLevelAndTopic() {
        val beginnerPrompt = PromptBuilder.buildLearningLessonPrompt("Quantum Entanglement", LearningLevel.BEGINNER)
        assertTrue(beginnerPrompt.contains("Quantum Entanglement"))
        assertTrue(beginnerPrompt.contains("Beginner"))
        assertTrue(beginnerPrompt.contains("\"quiz\""))
        assertTrue(beginnerPrompt.contains("\"practicalExample\""))

        val advPrompt = PromptBuilder.buildLearningLessonPrompt("Quantum Entanglement", LearningLevel.ADVANCED)
        assertTrue(advPrompt.contains("Advanced"))
    }

    @Test
    fun buildWritingPrompt_incorporatesIntentAndTone() {
        val prompt = PromptBuilder.buildWritingPrompt(
            intent = WritingIntent.EMAIL,
            tone = WritingTone.PROFESSIONAL,
            instruction = "Request an extension on the project deadline.",
            sourceText = null
        )
        assertTrue(prompt.contains("Format: Email"))
        assertTrue(prompt.contains("Tone: Professional"))
        assertTrue(prompt.contains("Request an extension on the project deadline."))
    }

    @Test
    fun buildWritingRefinePrompt_handlesRefinementModes() {
        val shorterPrompt = PromptBuilder.buildWritingRefinePrompt("shorter", "Long original text")
        assertTrue(shorterPrompt.contains("Condense and shorten"))

        val grammarPrompt = PromptBuilder.buildWritingRefinePrompt("grammar", "Bad grammar text")
        assertTrue(grammarPrompt.contains("grammar"))
    }

    @Test
    fun buildExplainMoreSimplyPrompt_includesDirective() {
        val prompt = PromptBuilder.buildExplainMoreSimplyPrompt("Photosynthesis", "Plant Energy Cycle")
        assertTrue(prompt.contains("Photosynthesis"))
        assertTrue(prompt.contains("Plant Energy Cycle"))
        assertTrue(prompt.contains("Explain this concept even more simply"))
    }

    @Test
    fun buildShowAnotherExamplePrompt_includesAnalogyDirective() {
        val prompt = PromptBuilder.buildShowAnotherExamplePrompt("Neural Networks", "Deep Learning Foundations")
        assertTrue(prompt.contains("Neural Networks"))
        assertTrue(prompt.contains("Provide a fresh, completely different real-world analogy"))
    }

    @Test
    fun buildSummarizeTopicPrompt_includesRevisionBulletDirective() {
        val prompt = PromptBuilder.buildSummarizeTopicPrompt("Relativity", "Time Dilation")
        assertTrue(prompt.contains("Relativity"))
        assertTrue(prompt.contains("3-to-4 bullet-point revision summary"))
    }

    @Test
    fun buildFreeResponseCheckPrompt_evaluatesUnderstanding() {
        val prompt = PromptBuilder.buildFreeResponseCheckPrompt(
            topic = "Thermodynamics",
            question = "What happens to entropy in a closed system?",
            userAnswer = "It always increases or stays constant."
        )
        assertTrue(prompt.contains("Thermodynamics"))
        assertTrue(prompt.contains("It always increases or stays constant."))
        assertTrue(prompt.contains("Evaluate the user's answer"))
    }

    @Test
    fun buildTroubleshootIntakePrompt_withImage_includesVisionGuidance() {
        val prompt = PromptBuilder.buildTroubleshootIntakePrompt("Pump squealing", hasImage = true)
        assertTrue(prompt.contains("photo of the physical equipment"))
        assertTrue(prompt.contains("\"isEscalationNeeded\""))
    }

    @Test
    fun buildTroubleshootNextStepPrompt_withFailedBranch_guidesAlternativeHypothesis() {
        val history = listOf(
            DiagnosticStepHistory(1, "Check breaker", "No, previous solution did not resolve the problem")
        )
        val prompt = PromptBuilder.buildTroubleshootNextStepPrompt("AC dead", history)
        assertTrue(prompt.contains("earlier recommended fix did NOT resolve"))
        assertTrue(prompt.contains("alternative diagnostic branch"))
    }

    @Test
    fun buildDocumentComparisonPrompt_structuresComparison() {
        val excerptsA = listOf(1 to "Solar panels convert sunlight to DC electricity.")
        val excerptsB = listOf(2 to "Wind turbines convert kinetic air movement into AC electricity.")
        val prompt = PromptBuilder.buildDocumentComparisonPrompt(
            docAName = "Solar_Guide.pdf",
            docBName = "Wind_Guide.pdf",
            excerptsA = excerptsA,
            excerptsB = excerptsB,
            focusQuery = "Efficiency and weather dependence"
        )
        assertTrue(prompt.contains("Solar_Guide.pdf"))
        assertTrue(prompt.contains("Wind_Guide.pdf"))
        assertTrue(prompt.contains("Key Similarities"))
        assertTrue(prompt.contains("Key Differences"))
        assertTrue(prompt.contains("Efficiency and weather dependence"))
    }

    @Test
    fun buildDocumentSummaryPrompt_requestsExecutiveSummary() {
        val excerpts = listOf(1 to "Overview of renewable microgrids in rural settings.")
        val prompt = PromptBuilder.buildDocumentSummaryPrompt("Microgrid_Report.pdf", excerpts)
        assertTrue(prompt.contains("Microgrid_Report.pdf"))
        assertTrue(prompt.contains("EXECUTIVE SUMMARY"))
        assertTrue(prompt.contains("High-level overview"))
    }

    @Test
    fun buildAskAiPrompt_withPersonalization_injectsDirectives() {
        val prompt = PromptBuilder.buildAskAiPrompt(
            userMessage = "Explain inverter grounding",
            language = "Spanish",
            responseLength = "concise",
            customMemory = "User is an off-grid solar installer"
        )
        assertTrue(prompt.contains("Respond in Spanish."))
        assertTrue(prompt.contains("concise"))
        assertTrue(prompt.contains("User is an off-grid solar installer"))
        assertTrue(prompt.contains("Explain inverter grounding"))
    }
}
