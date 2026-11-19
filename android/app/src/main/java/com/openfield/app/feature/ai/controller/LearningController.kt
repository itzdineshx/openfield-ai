package com.openfield.app.feature.ai.controller

import com.openfield.app.core.text.Markdown
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LessonMarkupParser
import com.openfield.app.feature.ai.mode.LessonSection
import com.openfield.app.feature.ai.mode.OutputValidator
import com.openfield.app.feature.ai.mode.QuizProgress
import com.openfield.app.feature.ai.mode.StructuredLesson
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import com.openfield.app.feature.ai.mode.ValidationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** Lifecycle of the lesson body itself. A STOPPED lesson is visibly incomplete and is never saved. */
enum class LessonPhase { STREAMING, COMPLETE, STOPPED }

enum class PracticeStatus { IDLE, GENERATING, FAILED }

enum class TutorNoteKind(val label: String) {
    QUESTION("Your question"),
    SIMPLER("Simpler explanation"),
    EXAMPLE("Another example"),
    SUMMARY("Revision summary"),
    FEEDBACK("Feedback on your explanation")
}

enum class TutorNoteStatus { STREAMING, COMPLETE, STOPPED, FAILED }

/**
 * A tutor response attached to the lesson. [text] is only ever model output; [systemMessage] carries app-side
 * explanations (model not loaded, runtime error) so the UI can label them as system messages.
 */
data class TutorNote(
    val id: String,
    val kind: TutorNoteKind,
    val prompt: String?,
    val text: String,
    val status: TutorNoteStatus,
    val systemMessage: String? = null
)

sealed interface LearningUiState {
    data object Idle : LearningUiState

    /** Waiting for the model's first lesson block (model load / prompt prefill). */
    data class Generating(val message: String) : LearningUiState

    data class ActiveLesson(
        val lesson: StructuredLesson,
        val phase: LessonPhase = LessonPhase.COMPLETE,
        /** What the model is writing right now while [phase] is STREAMING. */
        val writingBlock: LessonMarkupParser.Block? = null,
        /** Knowledge checks the model has started but not finished (not interactive yet). */
        val pendingQuizCount: Int = 0,
        val stopReason: String? = null,
        val progress: Map<Int, QuizProgress> = emptyMap(),
        val hintsShown: Set<Int> = emptySet(),
        val practiceStatus: PracticeStatus = PracticeStatus.IDLE,
        val practiceMessage: String? = null,
        val notes: List<TutorNote> = emptyList()
    ) : LearningUiState {
        val isStreaming: Boolean get() = phase == LessonPhase.STREAMING
        val isAnswering: Boolean get() = notes.any { it.status == TutorNoteStatus.STREAMING }
        val isBusy: Boolean get() = isStreaming || isAnswering || practiceStatus == PracticeStatus.GENERATING
        val answeredCount: Int get() = lesson.quizzes.indices.count { progress[it]?.submitted == true }
        val correctCount: Int get() = lesson.quizzes.withIndex().count { (i, q) -> progress[i]?.isCorrect(q) == true }
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
    private var practiceJob: Job? = null
    private var currentSessionId: String = UUID.randomUUID().toString()

    /** Incremented whenever a lesson generation starts or is abandoned, so stale stream updates are dropped. */
    @Volatile
    private var generationToken = 0L

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Idle)
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    private val _currentTopic = MutableStateFlow("")
    val currentTopic: StateFlow<String> = _currentTopic.asStateFlow()

    private val _currentLevel = MutableStateFlow(LearningLevel.BEGINNER)
    val currentLevel: StateFlow<LearningLevel> = _currentLevel.asStateFlow()

    val savedLessons: StateFlow<List<LearningSessionEntity>> = modeSessionDao.getLearningSessionsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // ------------------------------------------------------------------
    // Lesson lifecycle
    // ------------------------------------------------------------------

    fun generateLesson(topic: String, level: LearningLevel = _currentLevel.value) {
        val cleanTopic = topic.trim()
        if (cleanTopic.isBlank()) return
        _currentTopic.value = cleanTopic
        _currentLevel.value = level

        cancelAllJobs()
        val token = ++generationToken
        currentSessionId = UUID.randomUUID().toString()
        _uiState.value = LearningUiState.Generating("Preparing a ${level.displayName.lowercase()} lesson on “$cleanTopic”…")

        activeJob = scope.launch(Dispatchers.IO) {
            if (!orchestrator.awaitReady()) {
                if (token == generationToken) {
                    _uiState.value = LearningUiState.Error(
                        "The on-device AI model isn't loaded. Open AI Model Management from the Home banner, then retry.",
                        canRetry = true
                    )
                }
                return@launch
            }

            val buffer = StringBuilder()
            var lastParse = 0L
            try {
                orchestrator.streamLessonGeneration(cleanTopic, level).collect { chunk ->
                    buffer.append(chunk)
                    val now = System.currentTimeMillis()
                    if (now - lastParse >= PARSE_INTERVAL_MS) {
                        lastParse = now
                        publishPartialLesson(token, buffer.toString(), cleanTopic, level)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                endLessonEarly(token, buffer.toString(), cleanTopic, level, "The on-device model stopped unexpectedly.")
                return@launch
            }
            ensureActive()
            finalizeLesson(token, buffer.toString(), cleanTopic, level)
        }
    }

    private fun publishPartialLesson(token: Long, raw: String, topic: String, level: LearningLevel) {
        val result = LessonMarkupParser.parse(raw, topic, level, isFinal = false)
        if (!result.hasMarkup) return
        val lesson = result.lesson
        if (!lesson.hasContent && lesson.title.isBlank()) return

        _uiState.update { current ->
            if (token != generationToken) return@update current
            val previous = current as? LearningUiState.ActiveLesson
            LearningUiState.ActiveLesson(
                lesson = lesson,
                phase = LessonPhase.STREAMING,
                writingBlock = result.activeBlock,
                pendingQuizCount = (result.quizzesStarted - lesson.quizzes.size).coerceAtLeast(0),
                progress = previous?.progress.orEmpty(),
                hintsShown = previous?.hintsShown.orEmpty()
            )
        }
    }

    private fun finalizeLesson(token: Long, raw: String, topic: String, level: LearningLevel) {
        if (token != generationToken) return
        val parsed = StructuredOutputParser.parseStructuredLesson(raw, topic, level)
        when (val validated = OutputValidator.validateStructuredLesson(parsed, raw)) {
            is ValidationResult.Success -> {
                val lesson = validated.data
                val previous = _uiState.value as? LearningUiState.ActiveLesson
                _uiState.value = LearningUiState.ActiveLesson(
                    lesson = lesson,
                    phase = LessonPhase.COMPLETE,
                    progress = previous?.progress.orEmpty().filterKeys { it in lesson.quizzes.indices },
                    hintsShown = previous?.hintsShown.orEmpty()
                )
                persistCurrent()
                // One repair attempt: if no knowledge check survived validation, ask for questions separately.
                if (lesson.quizzes.isEmpty()) startPractice(isRepair = true)
            }
            is ValidationResult.Failure -> endLessonEarly(
                token, raw, topic, level, "The lesson came out incomplete (${validated.reason.trimEnd('.')})."
            )
        }
    }

    /** Keeps whatever was generated visible but clearly marked incomplete; falls back to an error if nothing usable. */
    private fun endLessonEarly(token: Long, raw: String, topic: String, level: LearningLevel, reason: String) {
        if (token != generationToken) return
        val partial = LessonMarkupParser.parse(raw, topic, level, isFinal = true)
        val previous = _uiState.value as? LearningUiState.ActiveLesson
        _uiState.value = if (partial.hasMarkup && partial.lesson.hasContent) {
            LearningUiState.ActiveLesson(
                lesson = partial.lesson,
                phase = LessonPhase.STOPPED,
                stopReason = reason,
                progress = previous?.progress.orEmpty().filterKeys { it in partial.lesson.quizzes.indices },
                hintsShown = previous?.hintsShown.orEmpty()
            )
        } else {
            LearningUiState.Error("The lesson couldn't be built from the AI's answer. $reason Tap retry to try again.", canRetry = true)
        }
    }

    fun retry() {
        generateLesson(_currentTopic.value, _currentLevel.value)
    }

    fun cancelGeneration() {
        val wasStreaming = when (val s = _uiState.value) {
            is LearningUiState.Generating -> true
            is LearningUiState.ActiveLesson -> s.isStreaming
            else -> false
        }
        if (!wasStreaming) return
        generationToken++
        activeJob?.cancel()
        orchestrator.cancelGeneration()
        _uiState.update { s ->
            when (s) {
                is LearningUiState.Generating -> LearningUiState.Idle
                is LearningUiState.ActiveLesson -> s.copy(
                    phase = LessonPhase.STOPPED,
                    writingBlock = null,
                    pendingQuizCount = 0,
                    stopReason = "You stopped the lesson before it finished."
                )
                else -> s
            }
        }
    }

    fun resumeLesson(session: LearningSessionEntity) {
        cancelAllJobs()
        generationToken++
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
                practicalExample = ""
            )

        var progress = StructuredOutputParser.deserializeLessonProgress(session.lessonJson)
            .filterKeys { it in lesson.quizzes.indices }
        // Lessons saved before per-question progress existed only recorded "quiz completed".
        if (progress.isEmpty() && session.isQuizCompleted) {
            lesson.quiz?.let { progress = mapOf(0 to QuizProgress(it.correctOptionIndex, submitted = true, attempts = 1)) }
        }

        _uiState.value = LearningUiState.ActiveLesson(lesson = lesson, phase = LessonPhase.COMPLETE, progress = progress)
    }

    fun deleteLesson(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            modeSessionDao.deleteLearningSession(sessionId)
            if (currentSessionId == sessionId) reset()
        }
    }

    fun setLearningLevel(level: LearningLevel) {
        _currentLevel.value = level
    }

    fun reset() {
        cancelAllJobs()
        generationToken++
        _uiState.value = LearningUiState.Idle
        _currentTopic.value = ""
        currentSessionId = UUID.randomUUID().toString()
    }

    private fun cancelAllJobs() {
        val wasBusy = (_uiState.value as? LearningUiState.ActiveLesson)?.isBusy == true ||
            _uiState.value is LearningUiState.Generating
        activeJob?.cancel()
        followUpJob?.cancel()
        practiceJob?.cancel()
        if (wasBusy) orchestrator.cancelGeneration()
    }

    // ------------------------------------------------------------------
    // Knowledge checks
    // ------------------------------------------------------------------

    fun selectQuizOption(quizIndex: Int, optionIndex: Int) = updateQuiz(quizIndex) { p ->
        if (p.submitted) p else p.copy(selectedIndex = optionIndex)
    }

    fun submitQuizAnswer(quizIndex: Int) {
        updateQuiz(quizIndex) { p ->
            if (p.submitted || p.selectedIndex == null) p else p.copy(submitted = true, attempts = p.attempts + 1)
        }
        _uiState.update { s -> if (s is LearningUiState.ActiveLesson) s.copy(hintsShown = s.hintsShown - quizIndex) else s }
        persistCurrent()
    }

    fun retryQuiz(quizIndex: Int) = updateQuiz(quizIndex) { p -> p.copy(selectedIndex = null, submitted = false) }

    fun toggleHint(quizIndex: Int) {
        _uiState.update { s ->
            if (s is LearningUiState.ActiveLesson) {
                s.copy(hintsShown = if (quizIndex in s.hintsShown) s.hintsShown - quizIndex else s.hintsShown + quizIndex)
            } else s
        }
    }

    private fun updateQuiz(quizIndex: Int, transform: (QuizProgress) -> QuizProgress) {
        _uiState.update { s ->
            if (s is LearningUiState.ActiveLesson && quizIndex in s.lesson.quizzes.indices) {
                s.copy(progress = s.progress + (quizIndex to transform(s.progress[quizIndex] ?: QuizProgress())))
            } else s
        }
    }

    /** Generates new questions on the lesson, streaming each validated question in as soon as it is complete. */
    fun generatePractice() = startPractice(isRepair = false)

    private fun startPractice(isRepair: Boolean) {
        val current = _uiState.value as? LearningUiState.ActiveLesson ?: return
        if (current.phase != LessonPhase.COMPLETE || current.isBusy) return
        val lesson = current.lesson
        val sessionId = currentSessionId
        _uiState.value = current.copy(practiceStatus = PracticeStatus.GENERATING, practiceMessage = null)

        practiceJob = scope.launch(Dispatchers.IO) {
            val baseQuizzes = lesson.quizzes
            val existingQuestions = baseQuizzes.map { it.question }
            val buffer = StringBuilder()

            fun publish(isFinal: Boolean): Int {
                val fresh = LessonMarkupParser.parse(buffer.toString(), lesson.topic, lesson.level, isFinal).lesson.quizzes
                    .filter { q -> existingQuestions.none { it.equals(q.question, ignoreCase = true) } }
                    .filter { OutputValidator.validateQuiz(it) == null }
                if (fresh.isNotEmpty()) {
                    updateActive(sessionId) { it.copy(lesson = it.lesson.copy(quizzes = baseQuizzes + fresh)) }
                }
                return fresh.size
            }

            try {
                if (!orchestrator.awaitReady()) {
                    updateActive(sessionId) {
                        it.copy(practiceStatus = PracticeStatus.FAILED, practiceMessage = "The on-device AI model isn't loaded.")
                    }
                    return@launch
                }
                var lastParse = 0L
                orchestrator.streamLessonPractice(lesson.topic, lesson.title, lessonContext(lesson), existingQuestions)
                    .collect { chunk ->
                        buffer.append(chunk)
                        val now = System.currentTimeMillis()
                        if (now - lastParse >= PARSE_INTERVAL_MS) {
                            lastParse = now
                            publish(isFinal = false)
                        }
                    }
                val added = publish(isFinal = true)
                updateActive(sessionId) {
                    if (added > 0) it.copy(practiceStatus = PracticeStatus.IDLE, practiceMessage = null)
                    else it.copy(
                        practiceStatus = PracticeStatus.FAILED,
                        practiceMessage = if (isRepair) {
                            "No knowledge check could be validated from the model's answer, so none is shown."
                        } else {
                            "The model's questions couldn't be turned into a valid quiz."
                        }
                    )
                }
                if (added > 0) persistCurrent()
            } catch (e: CancellationException) {
                updateActive(sessionId) { it.copy(practiceStatus = PracticeStatus.IDLE) }
                throw e
            } catch (e: Exception) {
                updateActive(sessionId) {
                    it.copy(practiceStatus = PracticeStatus.FAILED, practiceMessage = "The on-device model stopped unexpectedly.")
                }
            }
        }
    }

    fun stopPractice() {
        val s = _uiState.value as? LearningUiState.ActiveLesson ?: return
        if (s.practiceStatus != PracticeStatus.GENERATING) return
        practiceJob?.cancel()
        orchestrator.cancelGeneration()
    }

    // ------------------------------------------------------------------
    // Tutor notes (follow-ups)
    // ------------------------------------------------------------------

    fun askLessonFollowUp(question: String) {
        val q = question.trim()
        if (q.isBlank()) return
        runFollowUp(TutorNoteKind.QUESTION, q) { lesson, context ->
            orchestrator.streamLessonFollowUp(lesson.topic, lesson.title, q, context)
        }
    }

    fun explainMoreSimply() = runFollowUp(TutorNoteKind.SIMPLER, null) { lesson, context ->
        orchestrator.streamExplainMoreSimply(lesson.topic, lesson.title, context)
    }

    fun showAnotherExample() = runFollowUp(TutorNoteKind.EXAMPLE, null) { lesson, context ->
        orchestrator.streamShowAnotherExample(lesson.topic, lesson.title, context)
    }

    fun summarizeTopic() = runFollowUp(TutorNoteKind.SUMMARY, null) { lesson, context ->
        orchestrator.streamSummarizeTopic(lesson.topic, lesson.title, context)
    }

    fun submitFreeResponseCheck(userAnswer: String) {
        val answer = userAnswer.trim()
        if (answer.isBlank()) return
        runFollowUp(TutorNoteKind.FEEDBACK, answer) { lesson, _ ->
            orchestrator.streamFreeResponseCheck(lesson.topic, freeResponseQuestion(lesson), answer)
        }
    }

    fun freeResponseQuestion(lesson: StructuredLesson): String =
        "Explain the core idea of ${lesson.topic} in your own words."

    private fun runFollowUp(
        kind: TutorNoteKind,
        prompt: String?,
        stream: (StructuredLesson, String) -> Flow<String>
    ) {
        val current = _uiState.value as? LearningUiState.ActiveLesson ?: return
        if (current.isBusy) return

        val noteId = UUID.randomUUID().toString()
        val sessionId = currentSessionId
        _uiState.value = current.copy(
            notes = current.notes + TutorNote(noteId, kind, prompt, text = "", status = TutorNoteStatus.STREAMING)
        )
        val lesson = current.lesson
        val context = lessonContext(lesson)

        followUpJob = scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            var status = TutorNoteStatus.COMPLETE
            var systemMessage: String? = null
            try {
                if (!orchestrator.awaitReady()) {
                    status = TutorNoteStatus.FAILED
                    systemMessage = "The on-device AI model isn't loaded yet. Open AI Model Management from the Home banner."
                    return@launch
                }
                var lastEmit = 0L
                stream(lesson, context).collect { token ->
                    buffer.append(token)
                    val now = System.currentTimeMillis()
                    if (now - lastEmit >= 60) {
                        lastEmit = now
                        updateNote(sessionId, noteId) { it.copy(text = buffer.toString()) }
                    }
                }
                if (buffer.isBlank()) {
                    status = TutorNoteStatus.FAILED
                    systemMessage = "The model returned an empty answer. Try again."
                }
            } catch (e: CancellationException) {
                status = TutorNoteStatus.STOPPED
            } catch (e: Exception) {
                status = TutorNoteStatus.FAILED
                systemMessage = "The on-device model stopped unexpectedly. Try again."
            } finally {
                val finalStatus = status
                val finalMessage = systemMessage
                updateNote(sessionId, noteId) {
                    it.copy(text = buffer.toString().trim(), status = finalStatus, systemMessage = finalMessage)
                }
            }
        }
    }

    fun stopFollowUp() {
        if ((_uiState.value as? LearningUiState.ActiveLesson)?.isAnswering == true) {
            orchestrator.cancelGeneration()
        }
        followUpJob?.cancel()
    }

    private fun updateNote(sessionId: String, id: String, transform: (TutorNote) -> TutorNote) =
        updateActive(sessionId) { s -> s.copy(notes = s.notes.map { if (it.id == id) transform(it) else it }) }

    private fun updateActive(sessionId: String, transform: (LearningUiState.ActiveLesson) -> LearningUiState.ActiveLesson) {
        _uiState.update { s ->
            if (s is LearningUiState.ActiveLesson && sessionId == currentSessionId) transform(s) else s
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    /** Saves the current lesson and per-question progress. Incomplete (streaming/stopped) lessons are never saved. */
    private fun persistCurrent() {
        val s = _uiState.value as? LearningUiState.ActiveLesson ?: return
        if (s.phase != LessonPhase.COMPLETE) return
        val sessionId = currentSessionId
        val lesson = s.lesson
        val progress = s.progress
        val allAnswered = lesson.quizzes.isNotEmpty() && lesson.quizzes.indices.all { progress[it]?.submitted == true }
        scope.launch(Dispatchers.IO) {
            try {
                modeSessionDao.insertLearningSession(
                    LearningSessionEntity(
                        id = sessionId,
                        topic = lesson.topic,
                        level = lesson.level.name,
                        lessonTitle = lesson.title.ifBlank { lesson.topic },
                        lessonJson = StructuredOutputParser.serializeLesson(lesson, progress),
                        isQuizCompleted = allAnswered,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } catch (ignored: Exception) {
            }
        }
    }

    private fun lessonContext(lesson: StructuredLesson): String = buildString {
        append(Markdown.toPlainText(lesson.overview)).append('\n')
        if (lesson.keyPoints.isNotEmpty()) append("Key ideas: ").append(lesson.keyPoints.joinToString("; ")).append('\n')
        if (lesson.flowSteps.isNotEmpty()) append("How it works: ").append(lesson.flowSteps.joinToString(" -> ")).append('\n')
        lesson.sections.forEach { append(it.heading).append(": ").append(Markdown.toPlainText(it.content)).append('\n') }
    }

    private companion object {
        /** How often the streaming buffer is re-parsed into lesson blocks. */
        const val PARSE_INTERVAL_MS = 120L
    }
}
