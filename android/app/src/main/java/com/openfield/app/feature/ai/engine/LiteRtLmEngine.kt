package com.openfield.app.feature.ai.engine
import com.google.ai.edge.litertlm.Contents
import android.util.Log
import com.openfield.app.core.util.PerformanceTracker
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

private const val TAG = "OpenField/LiteRtLm"

val Message.text: String
    get() = contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }

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

                // Pick the backend. CPU is the default because the Unisoc T616 / Mali-G57
                // class of SoC this app targets does not implement every LiteRT delegate op
                // that Gemma-family multimodal containers (text + audio + vision adapters)
                // request. When the GPU delegate fails to build a kernel it does so by
                // calling abort() inside libtensorflowlite_gpu — that is a native SIGABRT,
                // NOT a Java Throwable, so we cannot "catch and retry on CPU".
                // GPU is only used when the caller explicitly asks for it.
                val targetBackend = when (preferredBackend) {
                    AiBackend.GPU -> Backend.GPU()
                    AiBackend.CPU, AiBackend.AUTO -> Backend.CPU()
                }

                val visionBackend = when (preferredBackend) {
                    AiBackend.GPU -> Backend.GPU()
                    AiBackend.CPU, AiBackend.AUTO -> Backend.CPU()
                }

                val backendLabel = when (preferredBackend) {
                    AiBackend.GPU -> "GPU"
                    AiBackend.CPU, AiBackend.AUTO -> "CPU"
                }

                val engineConfig = EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = targetBackend,
                    visionBackend = visionBackend,
                    maxNumImages = 1
                )

                Log.i(
                    TAG,
                    "About to construct Engine: " +
                        "path=${modelFile.absolutePath} " +
                        "size=${modelFile.length()} " +
                        "backend=$backendLabel"
                )
                val candidate = Engine(engineConfig)
                Log.i(TAG, "Engine constructor returned. Calling initialize() on $backendLabel…")
                // If the LiteRT-LM runtime version does not match the .litertlm container
                // version, OR if a required sub-engine (audio/vision adapter) cannot be
                // built for this backend, the native layer aborts inside this call.
                // Neither OutOfMemoryError nor Throwable catches below will fire for that —
                // the whole process dies with SIGABRT. The user must see the Loading state
                // in the UI so that when a crash happens they know which action caused it.
                val initStart = System.currentTimeMillis()
                candidate.initialize()
                val initElapsed = System.currentTimeMillis() - initStart
                PerformanceTracker.recordColdInit(initElapsed, backendLabel)
                Log.i(TAG, "Engine.initialize() returned OK on $backendLabel in ${initElapsed}ms")

                activeEngine = candidate

                val caps = AiCapabilities(
                    modelId = "gemma-4-e2b-it",
                    modelName = "Gemma 4 E2B IT",
                    supportsVision = true,
                    supportsStreaming = true,
                    maxContextLength = 4096,
                    activeBackend = backendLabel
                )
                currentCapabilities = caps
                _engineState.value = AiEngineState.Ready(caps)

                Result.success(Unit)
                // The two catches below can only ever fire for Java-side failures.
                // A native CHECK/FATAL/abort() inside liblitertlm_jni.so bypasses all of
                // these and ends the process. Do not add messaging here that implies
                // these blocks "recover" from a native crash — they do not.
            } catch (e: OutOfMemoryError) {
                val ex = AiEngineException(
                    "Java heap is exhausted during model load. Close other apps and try again.",
                    e
                )
                _engineState.value = AiEngineState.Error(ex.message ?: "Out of memory", ex)
                Result.failure(ex)
            } catch (e: UnsatisfiedLinkError) {
                val ex = AiEngineException(
                    "The LiteRT-LM native library is not available for this ABI. Check that the APK " +
                        "ships the arm64-v8a .so and that the installed runtime version matches " +
                        "the .litertlm container version stamped in the model header.",
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

        val systemInstruction = config.systemPrompt?.let {
            Contents.of(it)
        }

        val convConfig = ConversationConfig(
            systemInstruction = systemInstruction,
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
            val startTime = System.currentTimeMillis()
            var isFirstToken = true
            var emittedChunks = 0

            try {
                _engineState.value = AiEngineState.Generating(prompt)
                val message = Message.user(Contents.of(prompt))
                var accumulated = ""

                conversation.sendMessageAsync(message).collect { responseMsg ->
                    val chunkText = responseMsg.text

                    if (chunkText.isNotEmpty()) {
                        if (isFirstToken) {
                            val ttft = System.currentTimeMillis() - startTime
                            PerformanceTracker.recordTimeToFirstToken(ttft)
                            isFirstToken = false
                        }
                        emittedChunks++

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
                val duration = System.currentTimeMillis() - startTime
                PerformanceTracker.recordGenerationStats(emittedChunks, duration)
                try { conversation.close() } catch (ignored: Exception) {}
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
            val startTime = System.currentTimeMillis()
            var isFirstToken = true
            var emittedChunks = 0

            try {
                _engineState.value = AiEngineState.Generating(prompt)
                val message = Message.user(Contents.of(prompt))
                var accumulated = ""

                conversation.sendMessageAsync(message).collect { responseMsg ->
                    val chunkText = responseMsg.text

                    if (chunkText.isNotEmpty()) {
                        if (isFirstToken) {
                            val ttft = System.currentTimeMillis() - startTime
                            PerformanceTracker.recordTimeToFirstToken(ttft)
                            isFirstToken = false
                        }
                        emittedChunks++

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
                val duration = System.currentTimeMillis() - startTime
                PerformanceTracker.recordGenerationStats(emittedChunks, duration)
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
            val startTime = System.currentTimeMillis()
            var isFirstToken = true
            var emittedChunks = 0

            try {
                _engineState.value = AiEngineState.Generating(prompt)
                val message = Message.user(
                    Contents.of(
                        Content.ImageFile(imagePath),
                        Content.Text(prompt)
                    )
                )
                var accumulated = ""

                conversation.sendMessageAsync(message).collect { responseMsg ->
                    val chunkText = responseMsg.text

                    if (chunkText.isNotEmpty()) {
                        if (isFirstToken) {
                            val ttft = System.currentTimeMillis() - startTime
                            PerformanceTracker.recordTimeToFirstToken(ttft)
                            isFirstToken = false
                        }
                        emittedChunks++

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
                val duration = System.currentTimeMillis() - startTime
                PerformanceTracker.recordGenerationStats(emittedChunks, duration)
                try { conversation.close() } catch (ignored: Exception) {}
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
