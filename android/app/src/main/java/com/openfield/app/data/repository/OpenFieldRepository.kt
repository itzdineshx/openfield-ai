package com.openfield.app.data.repository

import android.content.Context
import android.net.Uri
import com.openfield.app.core.components.StepItem
import com.openfield.app.core.model.AiToolItem
import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.model.ChatSession
import com.openfield.app.core.model.DocumentItem
import com.openfield.app.core.model.ImageAnalysisResult
import com.openfield.app.core.model.KnowledgeCollection
import com.openfield.app.core.model.KnowledgePack
import com.openfield.app.core.model.KnowledgeSource
import com.openfield.app.core.model.LearningTopic
import com.openfield.app.core.model.ToolFilterCategory
import com.openfield.app.core.model.TroubleshootStep
import com.openfield.app.core.text.Markdown
import com.openfield.app.data.local.OpenFieldDatabase
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.data.local.entity.ChatSessionEntity
import com.openfield.app.data.local.entity.CollectionSourceCrossRefEntity
import com.openfield.app.data.local.entity.DocumentEntity
import com.openfield.app.data.local.entity.KnowledgeCollectionEntity
import com.openfield.app.feature.ai.controller.ChatController
import com.openfield.app.feature.ai.controller.DocumentQaController
import com.openfield.app.feature.ai.controller.ImageAnalysisController
import com.openfield.app.feature.ai.controller.LearningController
import com.openfield.app.feature.ai.controller.TroubleshootingController
import com.openfield.app.feature.ai.controller.WritingController
import com.openfield.app.feature.ai.mode.LenientJson
import com.openfield.app.feature.ai.engine.AiEngineState
import com.openfield.app.feature.ai.engine.LocalAiEngine
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.model.ModelRepository
import com.openfield.app.feature.ai.tools.ExplainConceptUseCase
import com.openfield.app.feature.ai.tools.ReframeUseCase
import com.openfield.app.feature.ai.tools.SummarizeUseCase
import com.openfield.app.feature.ai.tools.TranslateUseCase
import com.openfield.app.feature.ai.tools.WriteContentUseCase
import com.openfield.app.feature.document.rag.CrossSourceRetriever
import com.openfield.app.feature.document.rag.DocumentChunker
import com.openfield.app.feature.document.rag.DocumentQaUseCase
import com.openfield.app.feature.document.rag.DocumentRetriever
import com.openfield.app.feature.document.rag.PdfTextExtractor
import com.openfield.app.feature.document.rag.UnifiedSearchEngine
import com.openfield.app.feature.library.KnowledgePackManager
import com.openfield.app.feature.problem.ProblemSolverUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AppSettings(
    val offlineMode: Boolean = true,
    val selectedModel: String = "Gemma 4 E2B IT (Local)",
    val availableModels: List<String> = listOf("Gemma 4 E2B IT"),
    val language: String = "English",
    val themeMode: String = "Light (System)",
    val telemetryEnabled: Boolean = false,
    val preferredExplanationLevel: String = "Beginner",
    val preferredResponseLength: String = "Balanced",
    val preferredWritingTone: String = "Professional",
    val preferredMeasurementUnits: String = "Metric",
    val preferredFormattingStyle: String = "Standard",
    val userCustomMemory: String = ""
)

class OpenFieldRepository(
    private val context: Context,
    val database: OpenFieldDatabase,
    val modelRepository: ModelRepository,
    val aiEngine: LocalAiEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    val pdfExtractor = PdfTextExtractor(context)
    val documentChunker = DocumentChunker()
    val documentRetriever = DocumentRetriever(database.documentDao())
    val documentQaUseCase = DocumentQaUseCase(database.documentDao(), documentRetriever, aiEngine)

    val summarizeUseCase = SummarizeUseCase(aiEngine)
    val explainUseCase = ExplainConceptUseCase(aiEngine)
    val translateUseCase = TranslateUseCase(aiEngine)
    val reframeUseCase = ReframeUseCase(aiEngine)
    val writeContentUseCase = WriteContentUseCase(aiEngine)
    val problemSolverUseCase = ProblemSolverUseCase(aiEngine)

    // Shared AI Orchestration & Mode Controllers
    val orchestrator = AiOrchestrator(aiEngine)
    val chatController = ChatController(database.chatDao(), orchestrator, scope)
    val imageAnalysisController = ImageAnalysisController(orchestrator, scope)
    val documentQaController = DocumentQaController(
        database.documentDao(),
        documentRetriever,
        pdfExtractor,
        documentChunker,
        orchestrator,
        scope
    )
    val troubleshootingController = TroubleshootingController(orchestrator, database.modeSessionDao(), scope)
    val learningController = LearningController(orchestrator, database.modeSessionDao(), scope)
    val writingController = WritingController(orchestrator, database.modeSessionDao(), scope)
    val knowledgePackManager = KnowledgePackManager(database.knowledgePackDao())

    // Phase 3 Unified Personal Knowledge System
    val collectionDao = database.collectionDao()
    val knowledgeSourceRegistry = KnowledgeSourceRegistry(
        database.documentDao(),
        database.chatDao(),
        database.modeSessionDao(),
        database.knowledgePackDao(),
        database.collectionDao()
    )
    val unifiedSearchEngine = UnifiedSearchEngine(
        database.documentDao(),
        database.chatDao(),
        database.modeSessionDao(),
        database.knowledgePackDao()
    )
    val crossSourceRetriever = CrossSourceRetriever(
        database.documentDao(),
        database.knowledgePackDao()
    )
    val crossSourceQaController = com.openfield.app.feature.ai.controller.CrossSourceQaController(
        crossSourceRetriever,
        orchestrator,
        scope
    )
    val localMemoryManager = com.openfield.app.feature.ai.memory.LocalMemoryManager(
        database.settingsDao(),
        scope
    )
    val sharedContextManager = com.openfield.app.feature.ai.context.SharedContextManager(
        database.taskSessionDao(),
        scope
    )
    val backupManager = com.openfield.app.data.local.BackupManager(database)

    // Reactive Collections Flow
    val collections: StateFlow<List<KnowledgeCollection>> = database.collectionDao().getCollectionsFlow()
        .map { entities ->
            entities.map { entity ->
                val count = database.collectionDao().getSourceCountForCollection(entity.id)
                KnowledgeCollection(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    colorHex = entity.colorHex,
                    sourceCount = count,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    suspend fun createCollection(name: String, description: String, colorHex: Long): String {
        val id = "col_${System.currentTimeMillis()}"
        val entity = KnowledgeCollectionEntity(
            id = id,
            name = name,
            description = description,
            colorHex = colorHex
        )
        database.collectionDao().insertCollection(entity)
        return id
    }

    suspend fun deleteCollection(id: String) {
        database.collectionDao().deleteCollection(id)
    }

    val tasks: StateFlow<List<com.openfield.app.data.local.entity.TaskItemEntity>> = database.taskDao().getAllTasksFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    suspend fun createTask(
        title: String,
        description: String,
        priority: String = "MEDIUM",
        subtaskTitles: List<String> = emptyList(),
        dueDate: Long? = null
    ): String {
        val taskId = "task_${System.currentTimeMillis()}"
        val subtasks = subtaskTitles.mapIndexed { idx, stTitle ->
            mapOf("id" to "st_${taskId}_$idx", "title" to stTitle, "isCompleted" to false)
        }
        val subtasksJson = LenientJson.write(subtasks)
        val entity = com.openfield.app.data.local.entity.TaskItemEntity(
            id = taskId,
            title = title,
            description = description,
            status = "PENDING",
            priority = priority,
            dueDate = dueDate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            subtasksJson = subtasksJson
        )
        database.taskDao().insertTask(entity)
        return taskId
    }

    suspend fun toggleSubtask(taskId: String, subtaskId: String) {
        val task = database.taskDao().getTaskById(taskId) ?: return
        val rawList = (LenientJson.parse(task.subtasksJson) as? List<*>) ?: return
        val updated = rawList.map { item ->
            val map = (item as? Map<*, *>)?.toMutableMap() ?: return@map item
            if (map["id"] == subtaskId) {
                val current = map["isCompleted"] as? Boolean ?: false
                map["isCompleted"] = !current
            }
            map
        }
        val allDone = updated.all { (it as? Map<*, *>)?.get("isCompleted") == true }
        val newStatus = if (allDone && updated.isNotEmpty()) "COMPLETED" else "IN_PROGRESS"
        database.taskDao().updateTask(
            task.copy(
                subtasksJson = LenientJson.write(updated),
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteTask(taskId: String) {
        database.taskDao().deleteTask(taskId)
    }

    suspend fun addSourceToCollection(collectionId: String, sourceId: String, sourceType: String) {
        val ref = CollectionSourceCrossRefEntity(
            collectionId = collectionId,
            sourceId = sourceId,
            sourceType = sourceType
        )
        database.collectionDao().addSourceToCollection(ref)
    }

    suspend fun removeSourceFromCollection(collectionId: String, sourceId: String) {
        database.collectionDao().removeSourceFromCollection(collectionId, sourceId)
    }

    // Chat sessions & messages — backed by Room, no seed data.
    val sessions: StateFlow<List<ChatSession>> = database.chatDao().getSessionsFlow()
        .map { entities ->
            entities.map { entity ->
                ChatSession(
                    id = entity.id,
                    title = entity.title,
                    preview = entity.preview,
                    lastUpdated = formatTimestamp(entity.lastUpdated),
                    messageCount = entity.messageCount,
                    isPinned = entity.isPinned
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _messagesBySession = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messagesBySession: StateFlow<Map<String, List<ChatMessage>>> = _messagesBySession.asStateFlow()

    private val _currentImageAnalysis = MutableStateFlow<ImageAnalysisResult?>(null)
    val currentImageAnalysis: StateFlow<ImageAnalysisResult?> = _currentImageAnalysis.asStateFlow()

    private val _isAnalyzingImage = MutableStateFlow(false)
    val isAnalyzingImage: StateFlow<Boolean> = _isAnalyzingImage.asStateFlow()

    private val _savedImageAnalyses = MutableStateFlow<List<ImageAnalysisResult>>(emptyList())
    val savedImageAnalyses: StateFlow<List<ImageAnalysisResult>> = _savedImageAnalyses.asStateFlow()

    val documents: StateFlow<List<DocumentItem>> = database.documentDao().getDocumentsFlow()
        .map { entities ->
            entities.map { entity ->
                DocumentItem(
                    id = entity.id,
                    name = entity.name,
                    size = entity.formattedSize,
                    pageCount = entity.pageCount,
                    summary = entity.summary,
                    keyPoints = listOf(
                        "Pages: ${entity.pageCount}",
                        if (entity.isScanned) "Scanned document (OCR required)" else "Full text indexed locally"
                    ),
                    sampleQuestions = emptyList(),
                    isCustomImport = true
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _troubleshootingSteps = MutableStateFlow<List<TroubleshootStep>>(emptyList())
    val troubleshootingSteps: StateFlow<List<TroubleshootStep>> = _troubleshootingSteps.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _selectedOptions = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedOptions: StateFlow<Map<Int, String>> = _selectedOptions.asStateFlow()

    private val _currentTopic = MutableStateFlow<LearningTopic?>(null)
    val currentTopic: StateFlow<LearningTopic?> = _currentTopic.asStateFlow()

    private val _tools = MutableStateFlow<List<AiToolItem>>(emptyList())
    val tools: StateFlow<List<AiToolItem>> = _tools.asStateFlow()

    val knowledgePacks: StateFlow<List<KnowledgePack>> = database.knowledgePackDao().getKnowledgePacksFlow()
        .map { entities ->
            entities.map { entity ->
                val manifest = KnowledgePackManager.CURATED_CATALOG.find { it.packId == entity.id }
                KnowledgePack(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    size = entity.size,
                    isInstalled = entity.isInstalled,
                    category = entity.category,
                    iconColorHex = entity.iconColorHex,
                    topics = parseTopics(entity.topicsJson),
                    version = manifest?.version ?: "1.0.0"
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        initTools()
        monitorActiveChatSession()
        autoLoadModelIfInstalled()
        scope.launch(Dispatchers.IO) {
            knowledgePackManager.seedDefaultPacksIfEmpty()
        }
    }

    private fun autoLoadModelIfInstalled() {
        scope.launch {
            val file = modelRepository.getActiveModelFile() ?: return@launch
            if (!aiEngine.isReady()) {
                aiEngine.initialize(file)
            }
        }
    }

    private fun monitorActiveChatSession() {
        scope.launch {
            sessions.collect { sessionList ->
                for (session in sessionList) {
                    launch {
                        database.chatDao().getMessagesFlow(session.id).collect { msgEntities ->
                            val chatMsgs = msgEntities.map { entity ->
                                ChatMessage(
                                    id = entity.id,
                                    isUser = entity.isUser,
                                    text = entity.text,
                                    timestamp = formatTimestamp(entity.timestamp),
                                    steps = parseSteps(entity.stepsJson),
                                    imageUri = entity.imageUri,
                                    sourcePage = entity.sourcePage,
                                    isComplete = entity.isComplete
                                )
                            }
                            _messagesBySession.update { it + (session.id to chatMsgs) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun ensureEngineReady(): String? {
        val state = aiEngine.engineState.value
        if (state is AiEngineState.Ready || state is AiEngineState.Generating) return null

        return when (state) {
            is AiEngineState.Loading -> {
                val finalState = aiEngine.engineState.first {
                    it is AiEngineState.Ready || it is AiEngineState.Error
                }
                if (finalState is AiEngineState.Ready) null
                else "AI model failed to load: ${(finalState as? AiEngineState.Error)?.message ?: "unknown error"}"
            }
            is AiEngineState.Error -> "AI model error: ${state.message}. Please restart the app."
            else -> {
                if (modelRepository.getActiveModelFile() == null) {
                    "No on-device AI model is installed. Open AI Model Management to download or import the model."
                } else {
                    "AI model not ready. Please restart the app."
                }
            }
        }
    }

    /**
     * User-triggered engine initialization. Call from a visible UI action (the "Load model"
     * button in AI Model Management), never from the chat-send path, so that a native abort
     * is attributable to a specific user action.
     */
    suspend fun loadActiveModel(): Result<Unit> {
        val file = modelRepository.getActiveModelFile()
            ?: return Result.failure(IllegalStateException("No model installed"))
        return aiEngine.initialize(file)
    }

    private fun initTools() {
        _tools.value = listOf(
            AiToolItem(
                id = "image_analysis",
                title = "Image Analysis",
                description = "Identify, describe and get details from images",
                category = ToolFilterCategory.IMAGE,
                iconName = "image",
                inputPlaceholder = "Describe what you would like to analyze...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "doc_qa",
                title = "Document Q&A",
                description = "Ask questions about PDFs, manuals and notes",
                category = ToolFilterCategory.DOCUMENT,
                iconName = "document",
                inputPlaceholder = "Type a question regarding your document...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "summarize",
                title = "Summarize",
                description = "Get a short, clear summary of long content",
                category = ToolFilterCategory.TEXT,
                iconName = "summarize",
                inputPlaceholder = "Paste long text, articles, or notes to summarize...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "explain",
                title = "Explain",
                description = "Learn any topic in a simple way",
                category = ToolFilterCategory.TEXT,
                iconName = "explain",
                inputPlaceholder = "Enter any concept (e.g. Gravity, Wi-Fi, Electric motor, Quantum computing)...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "write_create",
                title = "Write & Create",
                description = "Help with writing, ideas and drafts",
                category = ToolFilterCategory.TEXT,
                iconName = "write",
                inputPlaceholder = "Describe what you want to write or outline...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "translate",
                title = "Translate",
                description = "Translate text between languages offline",
                category = ToolFilterCategory.TEXT,
                iconName = "translate",
                inputPlaceholder = "Enter text to translate offline...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "reframe",
                title = "Reframe / Improve",
                description = "Make text clearer, shorter or better",
                category = ToolFilterCategory.TEXT,
                iconName = "reframe",
                inputPlaceholder = "Paste text to improve...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "calculator",
                title = "Offline Calculator & Units",
                description = "Deterministic math expressions and offline unit conversions",
                category = ToolFilterCategory.TEXT,
                iconName = "calculate",
                inputPlaceholder = "Enter calculation or conversion...",
                sampleInput = "",
                sampleOutput = ""
            ),
            AiToolItem(
                id = "tasks",
                title = "Workflows & Checklists",
                description = "Automated checklists and action-oriented task planning",
                category = ToolFilterCategory.TEXT,
                iconName = "tasks",
                inputPlaceholder = "Describe task or workflow to plan...",
                sampleInput = "",
                sampleOutput = ""
            )
        )
    }

    // -------------------------------------------------------------
    // CHAT
    // -------------------------------------------------------------

    fun getSessionMessages(sessionId: String): Flow<List<ChatMessage>> {
        return database.chatDao().getMessagesFlow(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    isUser = entity.isUser,
                    text = entity.text,
                    timestamp = formatTimestamp(entity.timestamp),
                    steps = parseSteps(entity.stepsJson),
                    imageUri = entity.imageUri,
                    sourcePage = entity.sourcePage,
                    isComplete = entity.isComplete
                )
            }
        }
    }

    fun sendMessage(sessionId: String, userPrompt: String) {
        val s = _settings.value
        val memoryContext = localMemoryManager.buildContextForPrompt()
        val combined = listOf(s.userCustomMemory, memoryContext).filter { it.isNotBlank() }.joinToString("\n").ifBlank { null }
        chatController.sendMessage(
            sessionId = sessionId,
            userPrompt = userPrompt,
            language = s.language,
            responseLength = s.preferredResponseLength,
            explanationLevel = s.preferredExplanationLevel,
            measurementUnits = s.preferredMeasurementUnits,
            customMemory = combined
        )
    }

    fun createNewSession(title: String): String {
        val newId = UUID.randomUUID().toString()
        val entity = ChatSessionEntity(
            id = newId,
            title = title.ifBlank { "New chat" }.take(60),
            preview = "New conversation",
            lastUpdated = System.currentTimeMillis(),
            messageCount = 0
        )
        // Dispatch the insert to IO but wait for it so the row exists before the caller navigates.
        kotlinx.coroutines.runBlocking(Dispatchers.IO) {
            database.chatDao().insertSession(entity)
        }
        return newId
    }

    /**
     * Creates a new chat session synchronously (blocking Room insert) so callers can navigate
     * immediately without racing the writer coroutine.
     */
    suspend fun createNewSessionNow(title: String): String {
        val newId = UUID.randomUUID().toString()
        val entity = ChatSessionEntity(
            id = newId,
            title = title.ifBlank { "New chat" }.take(60),
            preview = "New conversation",
            lastUpdated = System.currentTimeMillis(),
            messageCount = 0
        )
        database.chatDao().insertSession(entity)
        return newId
    }

    suspend fun searchConversations(query: String): List<ChatSession> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext sessions.value
        val matches = database.chatDao().searchSessionsWithMessages(query)
        matches.map { entity ->
            ChatSession(
                id = entity.id,
                title = entity.title,
                preview = entity.preview,
                lastUpdated = formatTimestamp(entity.lastUpdated),
                messageCount = entity.messageCount,
                isPinned = entity.isPinned
            )
        }
    }

    fun deleteSession(sessionId: String) {
        scope.launch {
            database.chatDao().deleteSession(sessionId)
            database.chatDao().deleteMessagesForSession(sessionId)
        }
    }

    fun clearSessionMessages(sessionId: String) {
        scope.launch {
            database.chatDao().deleteMessagesForSession(sessionId)
            val session = database.chatDao().getSession(sessionId)
            if (session != null) {
                database.chatDao().updateSession(session.copy(preview = "No messages", messageCount = 0))
            }
        }
    }

    // -------------------------------------------------------------
    // IMAGE ANALYSIS
    // -------------------------------------------------------------

    fun analyzeSelectedImage(uri: Uri, context: Context, prompt: String? = null) {
        imageAnalysisController.setImage(context, uri)
        imageAnalysisController.analyzeImage(prompt)
    }

    fun saveCurrentAnalysis() {
        val sections = imageAnalysisController.analysisSections.value ?: return
        val result = ImageAnalysisResult(
            id = UUID.randomUUID().toString(),
            title = sections.subject,
            commonName = sections.possibleIdentification?.let(Markdown::stripInline) ?: sections.subject,
            scientificName = "",
            family = "",
            keyFeatures = sections.importantDetails.map(Markdown::stripInline),
            safetyNotice = sections.uncertaintyAndLimitations?.let(Markdown::toPlainText).orEmpty(),
            imageUri = imageAnalysisController.selectedImageUri.value,
            rawText = imageAnalysisController.rawAnalysisText.value
        )
        _currentImageAnalysis.value = result
        _savedImageAnalyses.update { listOf(result) + it }
    }

    // -------------------------------------------------------------
    // DOCUMENTS (real PDFBox extraction + chunking)
    // -------------------------------------------------------------

    fun importRealDocument(context: Context, uri: Uri) {
        documentQaController.importPdf(context, uri)
    }

    fun deleteDocument(docId: String) {
        scope.launch {
            database.documentDao().deleteDocument(docId)
            database.documentDao().deleteChunksForDocument(docId)
        }
    }

    // -------------------------------------------------------------
    // TROUBLESHOOTING — real on-device diagnostic generation
    // -------------------------------------------------------------

    fun startTroubleshootForProblem(problem: String) {
        scope.launch {
            val error = ensureEngineReady()
            if (error != null) {
                _troubleshootingSteps.value = listOf(
                    TroubleshootStep(
                        stepIndex = 1,
                        totalSteps = 1,
                        categoryTitle = "Guided Troubleshooting",
                        subtitle = error,
                        question = "AI model required",
                        options = emptyList()
                    )
                )
                _currentStepIndex.value = 0
                _selectedOptions.value = emptyMap()
                return@launch
            }

            val diagnosis = StringBuilder()
            try {
                problemSolverUseCase.diagnoseStream(problem).collect { token -> diagnosis.append(token) }
            } catch (e: Exception) {
                diagnosis.append("Failed to generate diagnostic: ${e.message}")
            }

            _troubleshootingSteps.value = listOf(
                TroubleshootStep(
                    stepIndex = 1,
                    totalSteps = 1,
                    categoryTitle = "Guided Troubleshooting",
                    subtitle = problem.take(120),
                    question = "Diagnostic result",
                    instruction = diagnosis.toString(),
                    options = emptyList(),
                    solutionRecommendation = null
                )
            )
            _currentStepIndex.value = 0
            _selectedOptions.value = emptyMap()
        }
    }

    fun getCurrentTroubleshootStep(): TroubleshootStep? {
        val steps = _troubleshootingSteps.value
        val index = _currentStepIndex.value
        return if (index in steps.indices) steps[index] else null
    }

    fun selectTroubleshootOption(stepIndex: Int, optionId: String) {
        _selectedOptions.update { it + (stepIndex to optionId) }
    }

    fun nextTroubleshootStep() {
        if (_currentStepIndex.value < _troubleshootingSteps.value.size - 1) {
            _currentStepIndex.value += 1
        }
    }

    fun prevTroubleshootStep() {
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value -= 1
        }
    }

    fun resetTroubleshootWorkflow() {
        _troubleshootingSteps.value = emptyList()
        _currentStepIndex.value = 0
        _selectedOptions.value = emptyMap()
    }

    // -------------------------------------------------------------
    // LEARNING — real on-device explanation generation only
    // -------------------------------------------------------------

    fun setTopic(topicPrompt: String) {
        scope.launch {
            val error = ensureEngineReady()
            if (error != null) {
                _currentTopic.value = LearningTopic(
                    id = UUID.randomUUID().toString(),
                    prompt = topicPrompt,
                    definition = error,
                    inShortPoints = emptyList(),
                    diagramTitle = "",
                    suggestions = emptyList()
                )
                return@launch
            }

            val generated = StringBuilder()
            try {
                explainUseCase.execute(topicPrompt).collect { token -> generated.append(token) }
            } catch (e: Exception) {
                _currentTopic.value = LearningTopic(
                    id = UUID.randomUUID().toString(),
                    prompt = topicPrompt,
                    definition = "Generation failed: ${e.message}",
                    inShortPoints = emptyList(),
                    diagramTitle = "",
                    suggestions = emptyList()
                )
                return@launch
            }

            val fullText = generated.toString()
            val lines = fullText.lines().filter { it.isNotBlank() }
            val definition = lines.firstOrNull() ?: ""
            val points = lines
                .filter { it.startsWith("-") || it.startsWith("•") || it.matches(Regex("^\\d+\\..*")) }
                .map { it.trimStart('-', '•', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ' ') }
                .take(6)

            _currentTopic.value = LearningTopic(
                id = UUID.randomUUID().toString(),
                prompt = topicPrompt,
                definition = definition,
                inShortPoints = points,
                diagramTitle = topicPrompt,
                suggestions = emptyList()
            )
        }
    }

    // -------------------------------------------------------------
    // AI TOOLS — streaming inference only, no mock output
    // -------------------------------------------------------------

    fun executeToolActionStream(toolId: String, input: String): Flow<String> {
        return when (toolId) {
            "summarize" -> summarizeUseCase.execute(input)
            "explain" -> explainUseCase.execute(input)
            "translate" -> translateUseCase.execute(input)
            "reframe" -> reframeUseCase.execute(input)
            "write_create" -> writeContentUseCase.execute(input)
            else -> aiEngine.generateStream(input)
        }
    }

    // -------------------------------------------------------------
    // KNOWLEDGE PACKS (installed state persisted in Room)
    // -------------------------------------------------------------

    fun toggleKnowledgePack(packId: String) {
        scope.launch(Dispatchers.IO) {
            knowledgePackManager.togglePackInstallation(packId)
        }
    }

    fun updateKnowledgePack(packId: String) {
        scope.launch(Dispatchers.IO) {
            knowledgePackManager.updatePackToLatest(packId)
        }
    }

    // -------------------------------------------------------------
    // SETTINGS
    // -------------------------------------------------------------

    fun updateOfflineMode(enabled: Boolean) {
        _settings.update { it.copy(offlineMode = enabled) }
    }

    fun updateModel(model: String) {
        _settings.update { it.copy(selectedModel = model) }
    }

    fun updateLanguage(lang: String) {
        _settings.update { it.copy(language = lang) }
    }

    fun updatePreferredExplanationLevel(level: String) {
        _settings.update { it.copy(preferredExplanationLevel = level) }
    }

    fun updatePreferredResponseLength(length: String) {
        _settings.update { it.copy(preferredResponseLength = length) }
    }

    fun updatePreferredWritingTone(tone: String) {
        _settings.update { it.copy(preferredWritingTone = tone) }
    }

    fun updatePreferredMeasurementUnits(units: String) {
        _settings.update { it.copy(preferredMeasurementUnits = units) }
    }

    fun updatePreferredFormattingStyle(style: String) {
        _settings.update { it.copy(preferredFormattingStyle = style) }
    }

    fun updateUserCustomMemory(memory: String) {
        _settings.update { it.copy(userCustomMemory = memory.trim()) }
    }

    fun clearUserCustomMemory() {
        _settings.update { it.copy(userCustomMemory = "") }
    }

    fun clearAppCache(context: Context): Long {
        var bytesDeleted = 0L
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                bytesDeleted += file.length()
                file.deleteRecursively()
            }
        } catch (ignored: Exception) {}
        return bytesDeleted
    }

    private fun formatTimestamp(millis: Long): String {
        return try {
            val now = System.currentTimeMillis()
            val diff = now - millis
            if (diff < 60_000) "Just now"
            else if (diff < 24 * 3600 * 1000) timeFormat.format(Date(millis))
            else dateFormat.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseTopics(json: String): List<String> {
        return try {
            json.trim('[', ']')
                .split(",")
                .map { it.trim('"', ' ') }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseSteps(stepsJson: String?): List<StepItem> {
        if (stepsJson.isNullOrBlank()) return emptyList()
        return try {
            stepsJson.lines().mapIndexed { idx, line ->
                StepItem(idx + 1, line.trimStart('-', '•', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ' '))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: OpenFieldRepository? = null

        fun initialize(
            context: Context,
            database: OpenFieldDatabase,
            modelRepository: ModelRepository,
            aiEngine: LocalAiEngine
        ): OpenFieldRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OpenFieldRepository(
                    context = context.applicationContext,
                    database = database,
                    modelRepository = modelRepository,
                    aiEngine = aiEngine
                ).also { INSTANCE = it }
            }
        }

        fun getInstance(): OpenFieldRepository {
            return INSTANCE ?: throw IllegalStateException(
                "OpenFieldRepository must be initialized in Application.onCreate() before calling getInstance()"
            )
        }
    }
}
