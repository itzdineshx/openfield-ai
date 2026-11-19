package com.openfield.app.feature.ai.mode

import com.openfield.app.feature.ai.engine.AiEngineException
import com.openfield.app.feature.ai.engine.AiEngineState
import com.openfield.app.feature.ai.engine.AiGenerationConfig
import com.openfield.app.feature.ai.engine.LocalAiEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

class AiOrchestrator(
    val aiEngine: LocalAiEngine
) {

    fun isReady(): Boolean = aiEngine.isReady()

    /** Waits for an in-progress model load (e.g. auto-load at app start) instead of failing immediately. */
    suspend fun awaitReady(timeoutMs: Long = 120_000): Boolean {
        if (aiEngine.isReady()) return true
        if (aiEngine.engineState.value !is AiEngineState.Loading) return false
        val settled = withTimeoutOrNull(timeoutMs) {
            aiEngine.engineState.first { it !is AiEngineState.Loading }
        }
        return settled != null && aiEngine.isReady()
    }

    fun cancelGeneration() {
        aiEngine.cancelGeneration()
    }

    // -------------------------------------------------------------
    // Mode A: Ask AI (Normal Conversational Chat)
    // -------------------------------------------------------------
    fun streamAskAi(
        sessionId: String,
        userPrompt: String,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ASK_AI).defaultGenConfig
        val prompt = PromptBuilder.buildAskAiPrompt(userPrompt)
        return aiEngine.generateConversationStream(sessionId, prompt, config)
    }

    // -------------------------------------------------------------
    // Mode B: Analyze Image
    // -------------------------------------------------------------
    fun streamImageAnalysis(
        imagePath: String,
        userQuestion: String?,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ANALYZE_IMAGE).defaultGenConfig
        val prompt = PromptBuilder.buildImageAnalysisPrompt(userQuestion)
        return aiEngine.generateWithImageStream(imagePath, prompt, config)
    }

    fun streamImageFollowUp(
        imagePath: String,
        userQuestion: String,
        priorSummary: String?,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ANALYZE_IMAGE).defaultGenConfig
        val prompt = PromptBuilder.buildImageFollowUpPrompt(userQuestion, priorSummary)
        return aiEngine.generateWithImageStream(imagePath, prompt, config)
    }

    // -------------------------------------------------------------
    // Mode C: Ask Document (Grounded RAG)
    // -------------------------------------------------------------
    fun streamDocumentQa(
        documentName: String,
        excerptsWithPages: List<Pair<Int, String>>,
        question: String,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ASK_DOCUMENT).defaultGenConfig
        val prompt = PromptBuilder.buildDocumentQaPrompt(documentName, excerptsWithPages, question)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamDocumentComparison(
        docAName: String,
        docBName: String,
        excerptsA: List<Pair<Int, String>>,
        excerptsB: List<Pair<Int, String>>,
        focusQuery: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ASK_DOCUMENT).defaultGenConfig
        val prompt = PromptBuilder.buildDocumentComparisonPrompt(docAName, docBName, excerptsA, excerptsB, focusQuery)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamDocumentSummary(
        documentName: String,
        excerptsWithPages: List<Pair<Int, String>>,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ASK_DOCUMENT).defaultGenConfig
        val prompt = PromptBuilder.buildDocumentSummaryPrompt(documentName, excerptsWithPages)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamCrossSourceQa(
        prompt: String,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.ASK_DOCUMENT).defaultGenConfig
        return aiEngine.generateStream(prompt, config)
    }

    // -------------------------------------------------------------
    // Mode D: Solve a Problem (Interactive Diagnostics)
    // -------------------------------------------------------------
    fun streamDiagnosticAssessment(
        problemDescription: String,
        history: List<DiagnosticStepHistory> = emptyList(),
        imagePath: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.SOLVE_PROBLEM).defaultGenConfig
        val prompt = if (history.isEmpty()) {
            PromptBuilder.buildTroubleshootIntakePrompt(problemDescription, hasImage = imagePath != null)
        } else {
            PromptBuilder.buildTroubleshootNextStepPrompt(problemDescription, history)
        }
        return if (imagePath != null && history.isEmpty()) {
            aiEngine.generateWithImageStream(imagePath, prompt, config)
        } else {
            aiEngine.generateStream(prompt, config)
        }
    }

    suspend fun executeDiagnosticAssessment(
        problemDescription: String,
        history: List<DiagnosticStepHistory> = emptyList(),
        imagePath: String? = null
    ): ValidationResult<DiagnosticAssessment> {
        val rawBuffer = StringBuilder()
        try {
            streamDiagnosticAssessment(problemDescription, history, imagePath).collect { token ->
                rawBuffer.append(token)
            }
            val raw = rawBuffer.toString()
            val parsed = StructuredOutputParser.parseDiagnosticAssessment(raw)?.let(::repairDiagnostic)
            return OutputValidator.validateDiagnosticAssessment(parsed, raw, problemDescription)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ValidationResult.Failure("Diagnostic generation failed: ${e.message}", rawBuffer.toString())
        }
    }

    /** Small models often get the shape almost right; fix recoverable gaps instead of failing the step. */
    private fun repairDiagnostic(a: DiagnosticAssessment): DiagnosticAssessment {
        var fixed = a
        if (fixed.isResolutionReached && fixed.recommendedSolution.isNullOrBlank()) {
            fixed = if (!fixed.likelyCause.isNullOrBlank() && fixed.options.size < 2) {
                fixed.copy(recommendedSolution = "Address the likely cause described above. If you are unsure how, consult a qualified technician.")
            } else {
                fixed.copy(isResolutionReached = false)
            }
        }
        if (!fixed.isResolutionReached && fixed.options.size < 2 && fixed.question.isNotBlank()) {
            fixed = fixed.copy(
                options = listOf(
                    DiagnosticOption("A", "Yes"),
                    DiagnosticOption("B", "No"),
                    DiagnosticOption("C", "I'm not sure")
                )
            )
        }
        return fixed
    }

    // -------------------------------------------------------------
    // Mode E: Learn & Explain (Interactive Tutoring)
    // -------------------------------------------------------------
    fun streamLessonGeneration(
        topic: String,
        level: LearningLevel,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildLearningLessonPrompt(topic, level)
        return aiEngine.generateStream(prompt, config)
    }

    /** Additional knowledge checks for an existing lesson (also used once to repair a lesson with no valid quiz). */
    fun streamLessonPractice(
        topic: String,
        lessonTitle: String,
        lessonContext: String?,
        existingQuestions: List<String>,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildLessonPracticePrompt(topic, lessonTitle, lessonContext, existingQuestions)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamLessonFollowUp(
        topic: String,
        lessonTitle: String,
        userQuestion: String,
        lessonContext: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildLearningFollowUpPrompt(topic, lessonTitle, userQuestion, lessonContext)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamExplainMoreSimply(
        topic: String,
        lessonTitle: String,
        lessonContext: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildExplainMoreSimplyPrompt(topic, lessonTitle, lessonContext)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamShowAnotherExample(
        topic: String,
        lessonTitle: String,
        lessonContext: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildShowAnotherExamplePrompt(topic, lessonTitle, lessonContext)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamSummarizeTopic(
        topic: String,
        lessonTitle: String,
        lessonContext: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildSummarizeTopicPrompt(topic, lessonTitle, lessonContext)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamFreeResponseCheck(
        topic: String,
        question: String,
        userAnswer: String,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN).defaultGenConfig
        val prompt = PromptBuilder.buildFreeResponseCheckPrompt(topic, question, userAnswer)
        return aiEngine.generateStream(prompt, config)
    }

    // -------------------------------------------------------------
    // Mode F: Create & Write (Document Workspace)
    // -------------------------------------------------------------
    fun streamWritingDraft(
        intent: WritingIntent,
        tone: WritingTone,
        instruction: String,
        sourceText: String? = null,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.CREATE_WRITE).defaultGenConfig
        val prompt = PromptBuilder.buildWritingPrompt(intent, tone, instruction, sourceText)
        return aiEngine.generateStream(prompt, config)
    }

    fun streamWritingRefine(
        refinementType: String,
        currentContent: String,
        customConfig: AiGenerationConfig? = null
    ): Flow<String> {
        val config = customConfig ?: ModeConfigRegistry.get(OpenFieldMode.CREATE_WRITE).defaultGenConfig
        val prompt = PromptBuilder.buildWritingRefinePrompt(refinementType, currentContent)
        return aiEngine.generateStream(prompt, config)
    }
}
