package com.openfield.app.feature.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.AnswerOptionRow
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChoicePill
import com.openfield.app.core.components.FeatureHeaderCard
import com.openfield.app.core.components.GeneratingCard
import com.openfield.app.core.components.InlineErrorCard
import com.openfield.app.core.components.InlineMarkdown
import com.openfield.app.core.components.MarkdownContent
import com.openfield.app.core.components.OptionState
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.SectionTitle
import com.openfield.app.core.components.SuggestionChips
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldErrorBg
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.core.theme.OpenFieldSuccessBg
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.controller.LearningController
import com.openfield.app.feature.ai.controller.LearningUiState
import com.openfield.app.feature.ai.controller.LessonPhase
import com.openfield.app.feature.ai.controller.PracticeStatus
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LessonQuiz
import com.openfield.app.feature.ai.mode.QuizProgress
import com.openfield.app.feature.voice.rememberVoiceInput

private val topicSuggestions = listOf(
    "How a battery works",
    "Photosynthesis",
    "How Wi-Fi works",
    "Compound interest",
    "Why the sky is blue"
)

@Composable
fun LearnScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val controller = repository.learningController
    val uiState by controller.uiState.collectAsState()
    val currentLevel by controller.currentLevel.collectAsState()
    val savedSessions by controller.savedLessons.collectAsState()

    var topicInput by remember { mutableStateOf("") }
    var followUpPrompt by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val voice = rememberVoiceInput { recognized ->
        followUpPrompt = if (followUpPrompt.isBlank()) recognized else "$followUpPrompt $recognized"
    }

    val active = uiState as? LearningUiState.ActiveLesson
    val followUpCount = active?.notes?.size ?: 0
    val lastFollowUpLength = active?.notes?.lastOrNull()?.text?.length ?: 0

    LaunchedEffect(uiState::class) { scrollState.scrollTo(0) }
    LaunchedEffect(followUpCount) {
        if (followUpCount > 0) scrollState.animateScrollTo(scrollState.maxValue)
    }
    LaunchedEffect(lastFollowUpLength) {
        if (followUpCount > 0 && scrollState.maxValue - scrollState.value < 600) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Learn & Explain",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (uiState !is LearningUiState.Idle) {
                        IconButton(onClick = {
                            controller.reset()
                            topicInput = ""
                        }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "New topic")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (active != null) {
                ChatBottomComposer(
                    value = followUpPrompt,
                    onValueChange = { followUpPrompt = it },
                    onSend = { prompt ->
                        controller.askLessonFollowUp(prompt)
                        followUpPrompt = ""
                    },
                    onVoiceClick = voice.toggle,
                    isListening = voice.isListening,
                    onStopListening = voice.stop,
                    onCancelListening = voice.cancel,
                    isGenerating = active.isBusy,
                    onStopGenerating = {
                        when {
                            active.isStreaming -> controller.cancelGeneration()
                            active.practiceStatus == PracticeStatus.GENERATING -> controller.stopPractice()
                            else -> controller.stopFollowUp()
                        }
                    },
                    placeholder = "Ask a follow-up…",
                    modifier = Modifier.imePadding()
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (val state = uiState) {
                is LearningUiState.Idle -> IdleContent(
                    topicInput = topicInput,
                    onTopicChange = { topicInput = it },
                    level = currentLevel,
                    onLevelChange = controller::setLearningLevel,
                    onStart = { controller.generateLesson(topicInput, currentLevel) },
                    savedSessions = savedSessions,
                    onResume = controller::resumeLesson,
                    onDelete = controller::deleteLesson
                )

                is LearningUiState.Generating -> {
                    FeatureHeaderCard(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Preparing your lesson",
                        subtitle = "Key ideas, diagrams, examples and knowledge checks."
                    )
                    GeneratingCard(
                        message = state.message,
                        detail = "The on-device model is reading your topic. The lesson appears here block by block as it is written.",
                        onStop = { controller.cancelGeneration() }
                    )
                }

                is LearningUiState.ActiveLesson -> LessonContent(state, controller)

                is LearningUiState.Error -> InlineErrorCard(
                    title = "Couldn't create the lesson",
                    message = state.message,
                    retryLabel = "Retry lesson",
                    onRetry = if (state.canRetry) controller::retry else null
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun IdleContent(
    topicInput: String,
    onTopicChange: (String) -> Unit,
    level: LearningLevel,
    onLevelChange: (LearningLevel) -> Unit,
    onStart: () -> Unit,
    savedSessions: List<LearningSessionEntity>,
    onResume: (LearningSessionEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    FeatureHeaderCard(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Learn anything",
        subtitle = "Lessons are written live by the on-device model — key ideas, diagrams, examples and quizzes, fully offline."
    )

    SectionTitle("What would you like to understand?")
    OutlinedTextField(
        value = topicInput,
        onValueChange = onTopicChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        placeholder = {
            Text(
                "e.g. How does a battery work?",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = OpenFieldSecondaryText.copy(alpha = 0.7f))
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OpenFieldPrimaryBlue,
            unfocusedBorderColor = OpenFieldCardBorder,
            focusedContainerColor = OpenFieldBackground,
            unfocusedContainerColor = OpenFieldBackground
        )
    )
    SuggestionChips(suggestions = topicSuggestions, onClick = onTopicChange)

    SectionTitle("Explanation level")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        LearningLevel.entries.forEach { item ->
            ChoicePill(
                label = item.displayName,
                selected = level == item,
                onClick = { onLevelChange(item) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    PrimaryButton(
        text = "Start lesson",
        onClick = onStart,
        enabled = topicInput.isNotBlank(),
        leadingIcon = Icons.Default.AutoAwesome
    )

    if (savedSessions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        SectionTitle("Recent lessons")
        savedSessions.take(8).forEach { session ->
            Surface(
                shape = CardShape,
                color = OpenFieldBackground,
                border = BorderStroke(1.dp, OpenFieldCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .clickable { onResume(session) }
            ) {
                Row(modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.lessonTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                        )
                        val levelName = runCatching { LearningLevel.valueOf(session.level).displayName }.getOrDefault(session.level)
                        Text(
                            text = "$levelName • ${if (session.isQuizCompleted) "Checks done" else "In progress"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = OpenFieldSecondaryText)
                        )
                    }
                    IconButton(onClick = { onDelete(session.id) }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete lesson",
                            tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonContent(state: LearningUiState.ActiveLesson, controller: LearningController) {
    val lesson = state.lesson
    val isComplete = state.phase == LessonPhase.COMPLETE

    LessonStatusBanner(
        isStreaming = state.isStreaming,
        stopReason = state.stopReason,
        onStop = controller::cancelGeneration,
        onRegenerate = controller::retry
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = OpenFieldPrimaryBlueLight) {
            Text(
                text = lesson.level.displayName,
                style = MaterialTheme.typography.labelMedium.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = lesson.topic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText)
        )
    }

    Text(
        text = lesson.title.ifBlank { lesson.topic },
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, color = OpenFieldNavyText)
    )
    if (lesson.overview.isNotBlank()) MarkdownContent(text = lesson.overview)

    // Generative UI: each block appears only if the model produced that kind of content for this topic.
    if (lesson.keyPoints.isNotEmpty()) KeyPointsBlock(lesson.keyPoints)
    if (lesson.flowSteps.size >= 2) FlowDiagramBlock(lesson.flowSteps)
    if (lesson.parts.isNotEmpty()) PartsDiagramBlock(lesson.topic, lesson.parts)
    lesson.sections.forEachIndexed { index, section -> ConceptBlock(index, section) }
    if (lesson.practicalExample.isNotBlank()) ExampleBlock(lesson.practicalExample)
    if (lesson.terms.isNotEmpty()) TermsBlock(lesson.terms)

    if (isComplete) {
        SectionTitle("Go deeper")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoicePill("Explain simpler", selected = false, onClick = controller::explainMoreSimply, enabled = !state.isBusy)
            ChoicePill("Another example", selected = false, onClick = controller::showAnotherExample, enabled = !state.isBusy)
            ChoicePill("Summarize", selected = false, onClick = controller::summarizeTopic, enabled = !state.isBusy)
        }
    }

    KnowledgeChecks(state, controller)

    if (state.isStreaming && state.pendingQuizCount == 0) {
        LiveWritingIndicator(label = (state.writingBlock?.writingLabel ?: "Writing") + "…")
    }

    if (isComplete) {
        ExplainBackCard(
            question = controller.freeResponseQuestion(lesson),
            enabled = !state.isBusy,
            onSubmit = controller::submitFreeResponseCheck
        )
    }

    if (state.notes.isNotEmpty()) {
        SectionTitle("Tutor notes")
        state.notes.forEach { note -> TutorNoteBlock(note) }
    }
}

@Composable
private fun KnowledgeChecks(state: LearningUiState.ActiveLesson, controller: LearningController) {
    val quizzes = state.lesson.quizzes
    val isComplete = state.phase == LessonPhase.COMPLETE
    val generatingPractice = state.practiceStatus == PracticeStatus.GENERATING
    if (quizzes.isEmpty() && state.pendingQuizCount == 0 && !isComplete) return

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Knowledge checks", modifier = Modifier.weight(1f))
        if (quizzes.isNotEmpty()) {
            Text(
                text = "${state.correctCount} of ${quizzes.size} correct",
                style = MaterialTheme.typography.labelMedium.copy(color = OpenFieldSecondaryText, fontWeight = FontWeight.Medium)
            )
        }
    }
    if (quizzes.isNotEmpty()) {
        LinearProgressIndicator(
            progress = { state.answeredCount.toFloat() / quizzes.size },
            color = OpenFieldPrimaryBlue,
            trackColor = OpenFieldPrimaryBlueLight,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }

    quizzes.forEachIndexed { index, quiz ->
        QuizCard(
            index = index,
            total = quizzes.size,
            quiz = quiz,
            progress = state.progress[index] ?: QuizProgress(),
            hintShown = index in state.hintsShown,
            controller = controller
        )
    }

    if (state.isStreaming && state.pendingQuizCount > 0) {
        LiveWritingIndicator(label = "Writing knowledge check ${quizzes.size + 1}…")
    }

    if (isComplete) {
        if (generatingPractice) {
            LiveWritingIndicator(label = "Writing new practice questions…")
        } else {
            if (state.practiceStatus == PracticeStatus.FAILED && state.practiceMessage != null) {
                Text(
                    text = state.practiceMessage,
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.5.sp)
                )
            }
            OutlinedButton(
                onClick = controller::generatePractice,
                enabled = !state.isBusy,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OpenFieldCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (quizzes.isEmpty()) "Create practice questions" else "More practice",
                    color = OpenFieldNavyText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuizCard(
    index: Int,
    total: Int,
    quiz: LessonQuiz,
    progress: QuizProgress,
    hintShown: Boolean,
    controller: LearningController
) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Check ${index + 1} of $total",
                style = MaterialTheme.typography.labelMedium.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold)
            )
            InlineMarkdown(
                text = quiz.question,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp, color = OpenFieldNavyText)
            )
            quiz.options.forEachIndexed { optionIndex, option ->
                val optionState = when {
                    progress.submitted && optionIndex == quiz.correctOptionIndex -> OptionState.CORRECT
                    progress.submitted && optionIndex == progress.selectedIndex -> OptionState.WRONG
                    !progress.submitted && optionIndex == progress.selectedIndex -> OptionState.SELECTED
                    else -> OptionState.DEFAULT
                }
                AnswerOptionRow(
                    text = option,
                    state = optionState,
                    enabled = !progress.submitted,
                    onClick = { controller.selectQuizOption(index, optionIndex) }
                )
            }

            if (!progress.submitted && quiz.hint != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { controller.toggleHint(index) }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hintShown) "Hide hint" else "Need a hint?",
                        style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    )
                }
                if (hintShown) {
                    MarkdownContent(
                        text = quiz.hint,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp, color = OpenFieldSecondaryText)
                    )
                }
            }

            if (!progress.submitted) {
                PrimaryButton(
                    text = "Check answer",
                    onClick = { controller.submitQuizAnswer(index) },
                    enabled = progress.selectedIndex != null,
                    height = 48.dp
                )
            } else {
                val correct = progress.isCorrect(quiz)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (correct) OpenFieldSuccessBg else OpenFieldErrorBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = when {
                                correct && progress.attempts <= 1 -> "Correct — nice work!"
                                correct -> "Correct on attempt ${progress.attempts}."
                                else -> "Not quite."
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (correct) OpenFieldSuccess else OpenFieldError
                            )
                        )
                        if (quiz.explanation.isNotBlank()) {
                            MarkdownContent(
                                text = quiz.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp, lineHeight = 20.sp, color = OpenFieldNavyText)
                            )
                        }
                    }
                }
                if (!correct) {
                    OutlinedButton(
                        onClick = { controller.retryQuiz(index) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = OpenFieldNavyText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Try again", color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplainBackCard(question: String, enabled: Boolean, onSubmit: (String) -> Unit) {
    var answer by remember { mutableStateOf("") }
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Explain it in your own words",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
            )
            Text(
                "$question The tutor will point out what you got right and what to fix.",
                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.5.sp)
            )
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5,
                placeholder = { Text("Your explanation…", color = OpenFieldSecondaryText.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OpenFieldPrimaryBlue,
                    unfocusedBorderColor = OpenFieldCardBorder
                )
            )
            PrimaryButton(
                text = "Get feedback",
                onClick = {
                    onSubmit(answer)
                    answer = ""
                },
                enabled = enabled && answer.isNotBlank(),
                height = 48.dp
            )
        }
    }
}
