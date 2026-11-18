package com.openfield.app.feature.ai.engine

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class LiteRtLmEngine : LocalAiEngine {

    private val _engineState = MutableStateFlow<AiEngineState>(AiEngineState.Uninitialized)
    override val engineState: StateFlow<AiEngineState> = _engineState.asStateFlow()

    private var currentCapabilities: AiCapabilities? = null
    override val capabilities: AiCapabilities?
        get() = currentCapabilities

    private var activeEngine: Engine? = null
    private val initMutex = Mutex()
    private val inferenceMutex = Mutex()

    // Cache of active conversations per session
    private val sessionConversations = ConcurrentHashMap<String, Conversation>()
    private var activeConversation: Conversation? = null

    override fun isReady(): Boolean = _engineState.value is AiEngineState.Ready

    override suspend fun initialize(
        modelFile: File,
        preferredBackend: AiBackend
    ): Result<Unit> = initMutex.withLock {
        withContext(Dispatchers.Default) {
            try {
                if (!modelFile.exists() || !modelFile.isFile) {
                    val error = AiEngineException("Model file does not exist at ${modelFile.absolutePath}")
                    _engineState.value = AiEngineState.Error(error.message ?: "File not found", error)
                    return@withContext Result.failure(error)
                }

                // Pre-flight size check — the native LiteRT loader will segfault on
                // truncated or placeholder files. Reject anything that looks obviously wrong
                // before touching JNI.
                val minReasonableSize = 50_000_000L
                if (modelFile.length() < minReasonableSize) {
                    val error = AiEngineException(
                        "Model file is too small to be a valid LiteRT artifact (${modelFile.length()} bytes). Re-download the model."
                    )
                    _engineState.value = AiEngineState.Error(error.message ?: "Invalid model", error)
                    return@withContext Result.failure(error)
                }

                if (!modelFile.canRead()) {
                    val error = AiEngineException("Cannot read model file at ${modelFile.absolutePath}")
                    _engineState.value = AiEngineState.Error(error.message ?: "No read permission", error)
                    return@withContext Result.failure(error)
                }

                _engineState.value = AiEngineState.Loading(modelFile.absolutePath)
                unloadInternal()

                // Select backend
                val targetBackend = when (preferredBackend) {
                    AiBackend.GPU -> Backend.GPU
                    AiBackend.CPU -> Backend.CPU
                    AiBackend.AUTO -> Backend.GPU // Try GPU first, will fall back to CPU if needed
                }

                var engineInstance: Engine? = null
                var usedBackend = "GPU"

                try {
                    val engineConfig = EngineConfig(
                        modelPath = modelFile.absolutePath,
                        backend = targetBackend
                    )
                    val candidate = Engine(engineConfig)
                    candidate.initialize()
                    engineInstance = candidate
                    usedBackend = if (targetBackend == Backend.GPU) "GPU" else "CPU"
                } catch (gpuEx: Throwable) {
                    if (targetBackend == Backend.GPU) {
                        // Fallback to CPU backend
                        try {
                            val cpuConfig = EngineConfig(
                                modelPath = modelFile.absolutePath,
                                backend = Backend.CPU
                            )
                            val cpuEngine = Engine(cpuConfig)
                            cpuEngine.initialize()
                            engineInstance = cpuEngine
                            usedBackend = "CPU"
                        } catch (cpuEx: Throwable) {
                            val ex = AiEngineException("Failed to initialize LiteRT-LM engine on CPU fallback", cpuEx)
                            _engineState.value = AiEngineState.Error(ex.message ?: "Init failed", ex)
                            return@withContext Result.failure(ex)
                        }
                    } else {
                        val ex = AiEngineException("Failed to initialize LiteRT-LM engine on $targetBackend", gpuEx)
                        _engineState.value = AiEngineState.Error(ex.message ?: "Init failed", ex)
                        return@withContext Result.failure(ex)
                    }
                }

                activeEngine = engineInstance

                val caps = AiCapabilities(
                    modelId = "gemma-4-e2b-it",
                    modelName = "Gemma 4 E2B IT",
                    supportsVision = true,
                    supportsStreaming = true,
                    maxContextLength = 4096,
                    activeBackend = usedBackend
                )
                currentCapabilities = caps
                _engineState.value = AiEngineState.Ready(caps)

                Result.success(Unit)
            } catch (e: OutOfMemoryError) {
                val ex = AiEngineException(
                    "Not enough memory to load the on-device model. Close other apps and try again.",
                    e
                )
                _engineState.value = AiEngineState.Error(ex.message ?: "Out of memory", ex)
                Result.failure(ex)
            } catch (e: UnsatisfiedLinkError) {
                val ex = AiEngineException(
                    "The LiteRT native runtime is not available on this device/ABI.",
                    e
                )
                _engineState.value = AiEngineState.Error(ex.message ?: "Native runtime missing", ex)
                Result.failure(ex)
            } catch (e: Throwable) {
                val ex = AiEngineException("Failed to load LiteRT model: ${e.message}", e)
                _engineState.value = AiEngineState.Error(ex.message ?: "Init error", ex)
                Result.failure(ex)
            }
        }
    }

    private fun getOrCreateConversation(
        sessionId: String?,
        config: AiGenerationConfig
    ): Conversation {
        val engine = activeEngine ?: throw AiEngineException("LiteRT-LM engine is not initialized")

        if (sessionId != null) {
            val existing = sessionConversations[sessionId]
            if (existing != null && existing.isAlive) {
                return existing
            }
        }

        val samplerConfig = SamplerConfig(
            topK = config.topK,
            topP = config.topP,
            temperature = config.temperature,
            seed = 0
        )

        val systemMsg = config.systemPrompt?.let { Message.of(it) }
        val convConfig = ConversationConfig(
            systemMessage = systemMsg,
            tools = emptyList(),
            samplerConfig = samplerConfig
        )

        val conversation = engine.createConversation(convConfig)
        if (sessionId != null) {
            sessionConversations[sessionId] = conversation
        }
        return conversation
    }

    override fun generateStream(
        prompt: String,
        config: AiGenerationConfig
    ): Flow<String> = flow {
        inferenceMutex.withLock {
            val conversation = getOrCreateConversation(null, config)
            activeConversation = conversation

            try {
                _engineState.value = AiEngineState.Generating(prompt)
                val message = Message.of(prompt)
                var accumulated = ""

                conversation.sendMessageAsync(message).collect { responseMsg ->
                    val chunkText = responseMsg.contents.filterIsInstance<Content.Text>()
                        .joinToString("") { it.text }

                    if (chunkText.isNotEmpty()) {
                        if (chunkText.startsWith(accumulated) && accumulated.isNotEmpty()) {
                            val delta = chunkText.substring(accumulated.length)
                            accumulated = chunkText
                            if (delta.isNotEmpty()) emit(delta)
                        } else {
                            accumulated += chunkText
                            emit(chunkText)
                        }
                    }
                }
            } catch (c: CancellationException) {
                conversation.cancelProcess()
                throw c
            } finally {
                activeConversation = null
                currentCapabilities?.let { _engineState.value = AiEngineState.Ready(it) }
            }
        }
    }.flowOn(Dispatchers.Default)

    override fun generateConversationStream(
        sessionId: String,
        prompt: String,
        config: AiGenerationConfig
    ): Flow<String> = flow {
        inferenceMutex.withLock {
            val conversation = getOrCreateConversation(sessionId, config)
            activeConversation = conversation

            try {
                _engineState.value = AiEngineState.Generating(prompt)
                val message = Message.of(prompt)
                var accumulated = ""

                conversation.sendMessageAsync(message).collect { responseMsg ->
                    val chunkText = responseMsg.contents.filterIsInstance<Content.Text>()
                        .joinToString("") { it.text }

                    if (chunkText.isNotEmpty()) {
                        if (chunkText.startsWith(accumulated) && accumulated.isNotEmpty()) {
                            val delta = chunkText.substring(accumulated.length)
                            accumulated = chunkText
                            if (delta.isNotEmpty()) emit(delta)
                        } else {
                            accumulated += chunkText
                            emit(chunkText)
                        }
                    }
                }
            } catch (c: CancellationException) {
                conversation.cancelProcess()
                throw c
            } finally {
                activeConversation = null
                currentCapabilities?.let { _engineState.value = AiEngineState.Ready(it) }
            }
        }
    }.flowOn(Dispatchers.Default)

    override fun generateWithImageStream(
        imagePath: String,
        prompt: String,
        config: AiGenerationConfig
    ): Flow<String> = flow {
        inferenceMutex.withLock {
            val caps = currentCapabilities ?: throw AiEngineException("Gemma 4 E2B model is not initialized")
            if (!caps.supportsVision) {
                throw AiEngineException("Vision capability is not available for the active configuration.")
            }

            val conversation = getOrCreateConversation(null, config)
            activeConversation = conversation

            try {
                _engineState.value = AiEngineState.Generating(prompt)
                val message = Message.of(
                    Content.ImageFile(imagePath),
                    Content.Text(prompt)
                )
                var accumulated = ""

                conversation.sendMessageAsync(message).collect { responseMsg ->
                    val chunkText = responseMsg.contents.filterIsInstance<Content.Text>()
                        .joinToString("") { it.text }

                    if (chunkText.isNotEmpty()) {
                        if (chunkText.startsWith(accumulated) && accumulated.isNotEmpty()) {
                            val delta = chunkText.substring(accumulated.length)
                            accumulated = chunkText
                            if (delta.isNotEmpty()) emit(delta)
                        } else {
                            accumulated += chunkText
                            emit(chunkText)
                        }
                    }
                }
            } catch (c: CancellationException) {
                conversation.cancelProcess()
                throw c
            } finally {
                activeConversation = null
                currentCapabilities?.let { _engineState.value = AiEngineState.Ready(it) }
            }
        }
    }.flowOn(Dispatchers.Default)

    override fun cancelGeneration() {
        activeConversation?.cancelProcess()
    }

    override suspend fun unload() = initMutex.withLock {
        withContext(Dispatchers.Default) {
            unloadInternal()
            _engineState.value = AiEngineState.Uninitialized
        }
    }

    private fun unloadInternal() {
        for ((_, conv) in sessionConversations) {
            try { conv.close() } catch (ignored: Exception) {}
        }
        sessionConversations.clear()
        activeConversation = null

        try {
            activeEngine?.close()
        } catch (ignored: Exception) {}
        activeEngine = null
        currentCapabilities = null
    }

    override fun close() {
        unloadInternal()
        _engineState.value = AiEngineState.Uninitialized
    }
}
