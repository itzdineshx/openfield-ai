package com.openfield.app.feature.ai.engine

enum class AiBackend {
    AUTO,
    GPU,
    CPU
}

data class AiGenerationConfig(
    val temperature: Double = 0.7,
    val topK: Int = 40,
    val topP: Double = 0.9,
    val maxTokens: Int = 1024,
    val systemPrompt: String? = null
)

data class AiCapabilities(
    val modelId: String,
    val modelName: String,
    val supportsVision: Boolean,
    val supportsStreaming: Boolean = true,
    val maxContextLength: Int = 2048,
    val activeBackend: String = "CPU"
)

sealed interface AiEngineState {
    data object Uninitialized : AiEngineState
    data class Loading(val modelPath: String) : AiEngineState
    data class Ready(val capabilities: AiCapabilities) : AiEngineState
    data class Generating(val prompt: String) : AiEngineState
    data class Error(val message: String, val cause: Throwable? = null) : AiEngineState
}

class AiEngineException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

