package com.openfield.app.feature.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.AnswerOptionRow
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
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
import com.openfield.app.core.theme.OpenFieldPrimaryBlueBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.core.theme.OpenFieldSuccessBg
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.controller.LearningController
import com.openfield.app.feature.ai.controller.LearningUiState
import com.openfield.app.feature.ai.mode.LearningLevel
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
    val followUpCount = active?.followUps?.size ?: 0
    val lastFollowUpLength = active?.followUps?.lastOrNull()?.text?.length ?: 0

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
                    isGenerating = active.isAnswering,
                    onStopGenerating = { controller.stopFollowUp() },
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
                        subtitle = "Explanation, examples and a quick quiz."
                    )
                    GeneratingCard(
                        message = state.message,
                        detail = "Lessons are written on your device and can take a minute or two.",
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
        subtitle = "Get a clear explanation, real examples and a quick quiz — all offline."
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
                            text = "$levelName • ${if (session.isQuizCompleted) "Quiz done" else "In progress"}",
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
        text = lesson.title,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, color = OpenFieldNavyText)
    )
    MarkdownContent(text = lesson.overview)

    if (lesson.topic.contains("battery", ignoreCase = true) || lesson.title.contains("battery", ignoreCase = true)) {
        Spacer(modifier = Modifier.height(10.dp))
        BatteryConceptDiagram()
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = CardShape,
            color = OpenFieldBackground,
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "In short:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText)
                )
                listOf(
                    "It has two ends: positive and negative.",
                    "A chemical reaction inside makes electrons move.",
                    "That motion creates electric current.",
                    "The current powers your device."
                ).forEach { pt ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(OpenFieldNavyText)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = pt,
                            style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldNavyText, fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }

    lesson.sections.forEachIndexed { index, section ->
        Surface(
            shape = CardShape,
            color = OpenFieldBackground,
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${index + 1}. ${section.heading}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OpenFieldPrimaryBlue)
                )
                MarkdownContent(text = section.content)
            }
        }
    }

    if (lesson.practicalExample.isNotBlank()) {
        Surface(
            shape = CardShape,
            color = OpenFieldPrimaryBlueLight,
            border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Everyday example", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldPrimaryBlue))
                    MarkdownContent(text = lesson.practicalExample)
                }
            }
        }
    }

    SectionTitle("Go deeper")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChoicePill("Explain simpler", selected = false, onClick = controller::explainMoreSimply, enabled = !state.isAnswering)
        ChoicePill("Another example", selected = false, onClick = controller::showAnotherExample, enabled = !state.isAnswering)
        ChoicePill("Summarize", selected = false, onClick = controller::summarizeTopic, enabled = !state.isAnswering)
    }

    lesson.quiz?.let { quiz ->
        SectionTitle("Quick check")
        Surface(
            shape = CardShape,
            color = OpenFieldBackground,
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InlineMarkdown(
                    text = quiz.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp, color = OpenFieldNavyText)
                )
                quiz.options.forEachIndexed { idx, option ->
                    val optionState = when {
                        state.isAnswerSubmitted && idx == quiz.correctOptionIndex -> OptionState.CORRECT
                        state.isAnswerSubmitted && idx == state.selectedQuizOptionIndex -> OptionState.WRONG
                        !state.isAnswerSubmitted && idx == state.selectedQuizOptionIndex -> OptionState.SELECTED
                        else -> OptionState.DEFAULT
                    }
                    AnswerOptionRow(
                        text = option,
                        state = optionState,
                        enabled = !state.isAnswerSubmitted,
                        onClick = { controller.selectQuizOption(idx) }
                    )
                }

                if (!state.isAnswerSubmitted && quiz.hint != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = controller::toggleHint)
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.showHint) "Hide hint" else "Need a hint?",
                            style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        )
                    }
                    if (state.showHint) {
                        MarkdownContent(
                            text = quiz.hint,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp, color = OpenFieldSecondaryText)
                        )
                    }
                }

                if (!state.isAnswerSubmitted) {
                    PrimaryButton(
                        text = "Check answer",
                        onClick = controller::submitQuizAnswer,
                        enabled = state.selectedQuizOptionIndex != null,
                        height = 48.dp
                    )
                } else {
                    val correct = state.selectedQuizOptionIndex == quiz.correctOptionIndex
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (correct) OpenFieldSuccessBg else OpenFieldErrorBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (correct) "Correct — nice work!" else "Not quite.",
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
                            onClick = controller::retryQuiz,
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

    ExplainBackCard(enabled = !state.isAnswering, onSubmit = controller::submitFreeResponseCheck)

    if (state.followUps.isNotEmpty()) {
        SectionTitle("Your questions")
        Column {
            state.followUps.forEach { msg ->
                ChatMessageBubble(
                    isUser = msg.isUser,
                    message = msg.text,
                    isComplete = msg.isComplete
                )
            }
        }
    }
}

@Composable
private fun ExplainBackCard(enabled: Boolean, onSubmit: (String) -> Unit) {
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
                "Write a short explanation and get feedback on what you got right.",
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

@Composable
fun BatteryConceptDiagram(modifier: Modifier = Modifier) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Labels
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Positive (+)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        text = "Negative (-)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Center Battery Graphic
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Positive Terminal Nub
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(Color(0xFF90A4AE))
                    )
                    // Battery Body Cylinder
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(105.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, OpenFieldNavyText, RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Layer: Reaction (Yellow/Orange)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFFFFB74D))
                            )
                            // Divider line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(OpenFieldNavyText.copy(alpha = 0.3f))
                            )
                            // Bottom Layer: Electron Flow (Blue)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFF64B5F6))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Right Description Labels
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text(
                        text = "Chemical\nreaction",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Text(
                        text = "(creates electrons)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = OpenFieldSecondaryText
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Electrical\nenergy output",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Text(
                        text = "(powers your device)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = OpenFieldSecondaryText
                        )
                    )
                }
            }
        }
    }
}
