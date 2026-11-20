package com.openfield.app.feature.problem

import com.openfield.app.feature.ai.engine.AiGenerationConfig
import com.openfield.app.feature.ai.engine.LocalAiEngine
import kotlinx.coroutines.flow.Flow

class ProblemSolverUseCase(private val aiEngine: LocalAiEngine) {

    fun diagnoseStream(problem: String): Flow<String> {
        val prompt = """
You are an expert offline diagnostic technician. The user is troubleshooting:
"$problem"

Generate a clear, step-by-step diagnostic guide with:
1. Likely Root Causes (2-3 probable explanations)
2. Safety & Pre-check (immediate safety precautions)
3. Step-by-Step Diagnostic Actions (numbered, clear physical checks)
4. Recommended Resolution (how to permanently fix or test)

Keep instructions practical, safe, and easy to follow.
""".trimIndent()

        val config = AiGenerationConfig(
            temperature = 0.4,
            topP = 0.9,
            systemPrompt = "You are CRUX's offline troubleshooting and diagnostics assistant. Provide systematic, safe, and actionable repair steps."
        )

        return aiEngine.generateStream(prompt, config)
    }

    fun answerStepFollowUpStream(
        problem: String,
        currentStep: String,
        userObservation: String
    ): Flow<String> {
        val prompt = """
Troubleshooting Context: "$problem"
Current Step: "$currentStep"
User's Observation: "$userObservation"

Based on this observation, what is the immediate next diagnostic action or conclusion?
Provide a concise, direct answer.
""".trimIndent()

        return aiEngine.generateStream(prompt, AiGenerationConfig(temperature = 0.4))
    }
}

