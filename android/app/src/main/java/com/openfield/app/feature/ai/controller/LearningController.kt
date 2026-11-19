package com.openfield.app.feature.ai.controller

import com.openfield.app.core.model.ChatMessage
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LessonSection
import com.openfield.app.feature.ai.mode.StructuredLesson
import com.openfield.app.feature.ai.mode.ValidationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    ) : LearningUiState
    data class Error(val message: String, val canRetry: Boolean = true) : LearningUiState
}

class LearningController(
    private val orchestrator: AiOrchestrator,
    private val modeSessionDao: ModeSessionDao,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null
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
        currentSessionId = session.id
        _currentTopic.value = session.topic
        val level = try {
            LearningLevel.valueOf(session.level)
        } catch (e: Exception) {
            LearningLevel.BEGINNER
        }
        _currentLevel.value = level

        val lesson = StructuredLesson(
            topic = session.topic,
            level = level,
            title = session.lessonTitle,
            overview = session.lessonJson,
            sections = listOf(LessonSection("Overview & Principles", session.lessonJson)),
            practicalExample = "Refer to saved lesson concepts.",
            quiz = null
        )

        _uiState.value = LearningUiState.ActiveLesson(
            lesson = lesson,
            isAnswerSubmitted = session.isQuizCompleted
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
        if (topic.isBlank()) return
        _currentTopic.value = topic.trim()
        _currentLevel.value = level
        currentSessionId = UUID.randomUUID().toString()

        if (!orchestrator.isReady()) {
            _uiState.value = LearningUiState.Error(
                "Local AI model is not ready. Please load Gemma 4 E2B IT in Model Management.",
                canRetry = true
            )
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            _uiState.value = LearningUiState.Generating("Crafting ${level.displayName} lesson on '$topic'…")

            val result = orchestrator.executeLessonGeneration(topic, level)
            when (result) {
                is ValidationResult.Success -> {
                    val lesson = result.data
                    _uiState.value = LearningUiState.ActiveLesson(lesson)
                    persistSession(lesson, isCompleted = false)
                }
                is ValidationResult.Failure -> {
                    _uiState.value = LearningUiState.Error(result.reason, canRetry = true)
                }
            }
        }
    }

    fun selectQuizOption(optionIndex: Int) {
        val current = _uiState.value
        if (current is LearningUiState.ActiveLesson && !current.isAnswerSubmitted) {
            _uiState.value = current.copy(selectedQuizOptionIndex = optionIndex)
        }
    }

    fun submitQuizAnswer() {
        val current = _uiState.value
        if (current is LearningUiState.ActiveLesson && current.selectedQuizOptionIndex != null) {
            _uiState.value = current.copy(isAnswerSubmitted = true)
            persistSession(current.lesson, isCompleted = true)
        }
    }

    fun toggleHint() {
        val current = _uiState.value
        if (current is LearningUiState.ActiveLesson) {
            _uiState.value = current.copy(showHint = !current.showHint)
        }
    }

    fun askLessonFollowUp(question: String) {
        val current = _uiState.value
        if (current !is LearningUiState.ActiveLesson || question.isBlank()) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        val userMsg = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = question.trim(),
            timestamp = "Just now"
        )
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Explaining…",
            timestamp = "Just now",
            isComplete = false
        )

        val updatedFollowUps = current.followUps + listOf(userMsg, assistantMsg)
        _uiState.value = current.copy(followUps = updatedFollowUps)

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                orchestrator.streamLessonFollowUp(
                    topic = current.lesson.topic,
                    lessonTitle = current.lesson.title,
                    userQuestion = question
                ).collect { token ->
                    buffer.append(token)
                    updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = false)
                }
                updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = true)
            } catch (e: Exception) {
                updateFollowUpAssistant(assistantMsgId, "Error: ${e.message}", isComplete = true)
            }
        }
    }

    fun explainMoreSimply() {
        val current = _uiState.value
        if (current !is LearningUiState.ActiveLesson) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        val userMsg = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = "Explain this more simply",
            timestamp = "Just now"
        )
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Simplifying concept…",
            timestamp = "Just now",
            isComplete = false
        )

        _uiState.value = current.copy(followUps = current.followUps + listOf(userMsg, assistantMsg))

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                orchestrator.streamExplainMoreSimply(
                    topic = current.lesson.topic,
                    lessonTitle = current.lesson.title
                ).collect { token ->
                    buffer.append(token)
                    updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = false)
                }
                updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = true)
            } catch (e: Exception) {
                updateFollowUpAssistant(assistantMsgId, "Error: ${e.message}", isComplete = true)
            }
        }
    }

    fun showAnotherExample() {
        val current = _uiState.value
        if (current !is LearningUiState.ActiveLesson) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        val userMsg = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = "Show another example",
            timestamp = "Just now"
        )
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Finding another analogy…",
            timestamp = "Just now",
            isComplete = false
        )

        _uiState.value = current.copy(followUps = current.followUps + listOf(userMsg, assistantMsg))

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                orchestrator.streamShowAnotherExample(
                    topic = current.lesson.topic,
                    lessonTitle = current.lesson.title
                ).collect { token ->
                    buffer.append(token)
                    updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = false)
                }
                updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = true)
            } catch (e: Exception) {
                updateFollowUpAssistant(assistantMsgId, "Error: ${e.message}", isComplete = true)
            }
        }
    }

    fun summarizeTopic() {
        val current = _uiState.value
        if (current !is LearningUiState.ActiveLesson) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        val userMsg = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = "Summarize this topic",
            timestamp = "Just now"
        )
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Summarizing core points…",
            timestamp = "Just now",
            isComplete = false
        )

        _uiState.value = current.copy(followUps = current.followUps + listOf(userMsg, assistantMsg))

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                orchestrator.streamSummarizeTopic(
                    topic = current.lesson.topic,
                    lessonTitle = current.lesson.title
                ).collect { token ->
                    buffer.append(token)
                    updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = false)
                }
                updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = true)
            } catch (e: Exception) {
                updateFollowUpAssistant(assistantMsgId, "Error: ${e.message}", isComplete = true)
            }
        }
    }

    fun submitFreeResponseCheck(userAnswer: String) {
        val current = _uiState.value
        if (current !is LearningUiState.ActiveLesson || userAnswer.isBlank()) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        val userMsg = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = "My understanding: $userAnswer",
            timestamp = "Just now"
        )
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Evaluating understanding…",
            timestamp = "Just now",
            isComplete = false
        )

        _uiState.value = current.copy(followUps = current.followUps + listOf(userMsg, assistantMsg))

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                val question = current.lesson.quiz?.question ?: "Core principles of ${current.lesson.topic}"
                orchestrator.streamFreeResponseCheck(
                    topic = current.lesson.topic,
                    question = question,
                    userAnswer = userAnswer
                ).collect { token ->
                    buffer.append(token)
                    updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = false)
                }
                updateFollowUpAssistant(assistantMsgId, buffer.toString(), isComplete = true)
            } catch (e: Exception) {
                updateFollowUpAssistant(assistantMsgId, "Error: ${e.message}", isComplete = true)
            }
        }
    }

    private fun updateFollowUpAssistant(id: String, text: String, isComplete: Boolean) {
        val current = _uiState.value
        if (current is LearningUiState.ActiveLesson) {
            val updated = current.followUps.map {
                if (it.id == id) it.copy(text = text, isComplete = isComplete) else it
            }
            _uiState.value = current.copy(followUps = updated)
        }
    }

    fun reset() {
        activeJob?.cancel()
        _uiState.value = LearningUiState.Idle
        _currentTopic.value = ""
        currentSessionId = UUID.randomUUID().toString()
    }

    private fun persistSession(lesson: StructuredLesson, isCompleted: Boolean) {
        scope.launch(Dispatchers.IO) {
            try {
                val entity = LearningSessionEntity(
                    id = currentSessionId,
                    topic = lesson.topic,
                    level = lesson.level.name,
                    lessonTitle = lesson.title,
                    lessonJson = lesson.overview,
                    isQuizCompleted = isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
                modeSessionDao.insertLearningSession(entity)
            } catch (ignored: Exception) {}
        }
    }
}
