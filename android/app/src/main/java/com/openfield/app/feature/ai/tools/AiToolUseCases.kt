package com.openfield.app.feature.ai.tools

import com.openfield.app.feature.ai.engine.AiGenerationConfig
import com.openfield.app.feature.ai.engine.LocalAiEngine
import kotlinx.coroutines.flow.Flow

class SummarizeUseCase(private val aiEngine: LocalAiEngine) {
    fun execute(text: String): Flow<String> {
        val prompt = """
You are a clear and concise offline summarizer. Summarize the following content in:
1. A 2-sentence executive summary.
2. 3 to 5 key bullet points highlighting essential takeaways.

CONTENT:
$text
""".trimIndent()
        return aiEngine.generateStream(prompt, AiGenerationConfig(temperature = 0.4, topP = 0.9))
    }
}

class ExplainConceptUseCase(private val aiEngine: LocalAiEngine) {
    fun execute(concept: String): Flow<String> {
        val prompt = """
Explain the following concept in simple, clear, and intuitive terms suitable for everyday understanding:
CONCEPT: $concept

Provide:
1. Core Definition (1-2 sentences)
2. How it works (step-by-step in plain language)
3. Everyday analogy or example
4. In short summary points
""".trimIndent()
        return aiEngine.generateStream(prompt, AiGenerationConfig(temperature = 0.5, topP = 0.9))
    }
}

class TranslateUseCase(private val aiEngine: LocalAiEngine) {
    fun execute(text: String, targetLanguage: String = "Spanish"): Flow<String> {
        val prompt = """
Translate the following text accurately and naturally into $targetLanguage.
Provide the translation directly without conversational filler.

TEXT:
$text
""".trimIndent()
        return aiEngine.generateStream(prompt, AiGenerationConfig(temperature = 0.3, topP = 0.9))
    }
}

class ReframeUseCase(private val aiEngine: LocalAiEngine) {
    fun execute(text: String, style: String = "Professional & Clear"): Flow<String> {
        val prompt = """
Rewrite and polish the following text to make it $style.
Keep the original meaning intact while improving clarity and tone. Provide only the revised text.

ORIGINAL TEXT:
$text
""".trimIndent()
        return aiEngine.generateStream(prompt, AiGenerationConfig(temperature = 0.5, topP = 0.9))
    }
}

class WriteContentUseCase(private val aiEngine: LocalAiEngine) {
    fun execute(instruction: String): Flow<String> {
        val prompt = """
You are an offline writing assistant. Draft a well-structured, clear piece of content based on the following instruction:
$instruction
""".trimIndent()
        return aiEngine.generateStream(prompt, AiGenerationConfig(temperature = 0.7, topP = 0.9))
    }
}

