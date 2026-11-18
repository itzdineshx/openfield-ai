package com.openfield.app.feature.ai.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface LocalAiEngine {
    val engineState: StateFlow<AiEngineState>
    val capabilities: AiCapabilities?

    suspend fun initialize(
        modelFile: File,
        preferredBackend: AiBackend = AiBackend.AUTO
    ): Result<Unit>

    suspend fun unload()

    fun generateStream(
        prompt: String,
        config: AiGenerationConfig = AiGenerationConfig()
    ): Flow<String>

    fun generateConversationStream(
        sessionId: String,
        prompt: String,
        config: AiGenerationConfig = AiGenerationConfig()
    ): Flow<String>

    fun generateWithImageStream(
        imagePath: String,
        prompt: String,
        config: AiGenerationConfig = AiGenerationConfig()
    ): Flow<String>

    fun cancelGeneration()

    fun isReady(): Boolean

    fun close()
}

