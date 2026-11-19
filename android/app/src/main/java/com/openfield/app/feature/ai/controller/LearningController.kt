package com.openfield.app.feature.ai.controller

import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.text.Markdown
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LessonSection
import com.openfield.app.feature.ai.mode.StructuredLesson
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import com.openfield.app.feature.ai.mode.ValidationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface LearningUiState {
    data object Idle : LearningUiState
    data class Generating(val message: String) : LearningUiState
    data class ActiveLesson(
        val lesson: StructuredLesson,
        val selectedQuizOptionIndex: Int? = null,
        val isAnswerSubmitted: Boolean = false,
        val showHint: Boolean = false,
        val followUps: List<ChatMessage> = emptyList()
    ) : LearningUiState {
        val isAnswering: Boolean get() = followUps.any { !it.isUser && !it.isComplete }
    }
    data class Error(val message: String, val canRetry: Boolean = true) : LearningUiState
}

class LearningController(
    private val orchestrator: AiOrchestrator,
    private val modeSessionDao: ModeSessionDao,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null
    private var followUpJob: Job? = null
    private var currentSessionId: String = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Idle)
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    private val _currentTopic = MutableStateFlow("")
    val currentTopic: StateFlow<String> = _currentTopic.asStateFlow()

    private val _currentLevel = MutableStateFlow(LearningLevel.BEGINNER)
    val currentLevel: StateFlow<LearningLevel> = _currentLevel.asStateFlow()

    val savedLessons: StateFlow<List<LearningSessionEntity>> = modeSessionDao.getLearningSessionsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun resumeLesson(session: LearningSessionEntity) {
        activeJob?.cancel()
        currentSessionId = session.id
        _currentTopic.value = session.topic
        val level = runCatching { LearningLevel.valueOf(session.level) }.getOrDefault(LearningLevel.BEGINNER)
        _currentLevel.value = level

        val lesson = StructuredOutputParser.deserializeLesson(session.lessonJson, session.topic, level)
            ?: StructuredLesson(
                topic = session.topic,
                level = level,
                title = session.lessonTitle,
                overview = session.lessonJson,
                sections = listOf(LessonSection("Overview", session.lessonJson)),
                practicalExample = "",
                quiz = null
            )

        _uiState.value = LearningUiState.ActiveLesson(
            lesson = lesson,
            selectedQuizOptionIndex = if (session.isQuizCompleted) lesson.quiz?.correctOptionIndex else null,
            isAnswerSubmitted = session.isQuizCompleted && lesson.quiz != null
        )
    }

    fun deleteLesson(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            modeSessionDao.deleteLearningSession(sessionId)
            if (currentSessionId == sessionId) {
                reset()
            }
        }
    }

    fun setLearningLevel(level: LearningLevel) {
        _currentLevel.value = level
    }

    fun generateLesson(topic: String, level: LearningLevel = _currentLevel.value) {
        val cleanTopic = topic.trim()
        if (cleanTopic.isBlank()) return
        _currentTopic.value = cleanTopic
        _currentLevel.value = level
        currentSessionId = UUID.randomUUID().toString()

        activeJob?.cancel()
        _uiState.value = LearningUiState.Generating("Building a ${level.displayName.lowercase()} lesson on “$cleanTopic”…")
        activeJob = scope.launch(Dispatchers.IO) {
            if (!orchestrator.awaitReady()) {
                _uiState.value = LearningUiState.Error(
                    "The on-device AI model isn't loaded. Open AI Model Management from the Home banner, then retry.",
                    canRetry = true
                )
                return@launch
            }

            when (val result = orchestrator.executeLessonGeneration(cleanTopic, level)) {
                is ValidationResult.Success -> {
                    _uiState.value = LearningUiState.ActiveLesson(result.data)
                    persistSession(result.data, isCompleted = false)
                }
                is ValidationResult.Failure -> {
                    _uiState.value = LearningUiState.Error(
                        "The lesson couldn't be built from the AI's answer (${result.reason.trimEnd('.')}). Tap retry to try again.",
                        canRetry = true
                    )
                }
            }
        }
    }

    fun retry() {
        generateLesson(_currentTopic.value, _currentLevel.value)
    }

    fun cancelGeneration() {
        orchestrator.cancelGeneration()
        activeJob?.cancel()
        _uiState.value = LearningUiState.Idle
    }

    fun selectQuizOption(optionIndex: Int) {
        _uiState.update { current ->
            if (current is LearningUiState.ActiveLesson && !current.isAnswerSubmitted) {
                current.copy(selectedQuizOptionIndex = optionIndex)
            } else current
        }
    }

    fun submitQuizAnswer() {
        val current = _uiState.value
        if (current is LearningUiState.ActiveLesson && current.selectedQuizOptionIndex != null) {
            _uiState.value = current.copy(isAnswerSubmitted = true, showHint = false)
            persistSession(current.lesson, isCompleted = true)
        }
    }

    fun retryQuiz() {
        _uiState.update { current ->
            if (current is LearningUiState.ActiveLesson) {
                current.copy(selectedQuizOptionIndex = null, isAnswerSubmitted = false)
            } else current
        }
    }

    fun toggleHint() {
        _uiState.update { current ->
            if (current is LearningUiState.ActiveLesson) current.copy(showHint = !current.showHint) else current
        }
    }

    fun askLessonFollowUp(question: String) {
        if (question.isBlank()) return
        runFollowUp(question.trim()) { lesson, context ->
            orchestrator.streamLessonFollowUp(lesson.topic, lesson.title, question, context)
        }
    }

    fun explainMoreSimply() = runFollowUp("Explain this more simply") { lesson, context ->
        orchestrator.streamExplainMoreSimply(lesson.topic, lesson.title, context)
    }

    fun showAnotherExample() = runFollowUp("Show me another example") { lesson, context ->
        orchestrator.streamShowAnotherExample(lesson.topic, lesson.title, context)
    }

    fun summarizeTopic() = runFollowUp("Summarize this lesson") { lesson, context ->
        orchestrator.streamSummarizeTopic(lesson.topic, lesson.title, context)
    }

    fun submitFreeResponseCheck(userAnswer: String) {
        if (userAnswer.isBlank()) return
        runFollowUp("My explanation: ${userAnswer.trim()}") { lesson, _ ->
            val question = "Explain the core idea of ${lesson.topic} in your own words."
            orchestrator.streamFreeResponseCheck(lesson.topic, question, userAnswer)
        }
    }

    private fun runFollowUp(
        userText: String,
        stream: (StructuredLesson, String) -> Flow<String>
    ) {
        val current = _uiState.value as? LearningUiState.ActiveLesson ?: return
        if (current.isAnswering) return

        val assistantMsgId = UUID.randomUUID().toString()
        _uiState.value = current.copy(
            followUps = current.followUps + listOf(
                ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = userText, timestamp = "Just now"),
                ChatMessage(id = assistantMsgId, isUser = false, text = "", timestamp = "Just now", isComplete = false)
            )
        )

        val lesson = current.lesson
        val context = buildString {
            append(Markdown.toPlainText(lesson.overview)).append('\n')
            lesson.sections.forEach { append(it.heading).append(": ").append(Markdown.toPlainText(it.content)).append('\n') }
        }

        followUpJob = scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                if (!orchestrator.awaitReady()) {
                    buffer.append("The on-device AI model isn't loaded yet. Open **AI Model Management** from the Home banner.")
                    return@launch
                }
                var lastEmit = 0L
                stream(lesson, context).collect { token ->
                    buffer.append(token)
                    val now = System.currentTimeMillis()
                    if (now - lastEmit >= 60) {
                        lastEmit = now
                        updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = false)
                    }
                }
            } catch (e: CancellationException) {
                // keep partial answer
            } catch (e: Exception) {
                if (buffer.isBlank()) buffer.append("Sorry, something went wrong: ${e.message}")
            } finally {
                updateFollowUpAssistant(assistantMsgId, buffer.toString().trim().ifEmpty { "_Stopped._" }, isComplete = true)
            }
        }
    }

    fun stopFollowUp() {
        if ((_uiState.value as? LearningUiState.ActiveLesson)?.isAnswering == true) {
            orchestrator.cancelGeneration()
        }
        followUpJob?.cancel()
    }

    private fun updateFollowUpAssistant(id: String, text: String, isComplete: Boolean) {
        _uiState.update { current ->
            if (current is LearningUiState.ActiveLesson) {
                current.copy(followUps = current.followUps.map {
                    if (it.id == id) it.copy(text = text, isComplete = isComplete) else it
                })
            } else current
        }
    }

    fun reset() {
        activeJob?.cancel()
        stopFollowUp()
        _uiState.value = LearningUiState.Idle
        _currentTopic.value = ""
        currentSessionId = UUID.randomUUID().toString()
    }

    private fun persistSession(lesson: StructuredLesson, isCompleted: Boolean) {
        val sessionId = currentSessionId
        scope.launch(Dispatchers.IO) {
            try {
                modeSessionDao.insertLearningSession(
                    LearningSessionEntity(
                        id = sessionId,
                        topic = lesson.topic,
                        level = lesson.level.name,
                        lessonTitle = lesson.title,
                        lessonJson = StructuredOutputParser.serializeLesson(lesson),
                        isQuizCompleted = isCompleted,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } catch (ignored: Exception) {
            }
        }
    }
}
