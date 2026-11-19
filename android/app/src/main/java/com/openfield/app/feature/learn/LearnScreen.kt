package com.openfield.app.feature.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.MarkdownText
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.controller.LearningUiState
import com.openfield.app.feature.ai.mode.LearningLevel

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
    var freeResponseInput by remember { mutableStateOf("") }

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
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "New topic"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState is LearningUiState.ActiveLesson) {
                ChatBottomComposer(
                    value = followUpPrompt,
                    onValueChange = { followUpPrompt = it },
                    onSend = { prompt ->
                        if (prompt.isNotBlank()) {
                            controller.askLessonFollowUp(prompt)
                            followUpPrompt = ""
                        }
                    },
                    onAttach = { },
                    onVoiceClick = { },
                    placeholder = "Ask a question about this lesson...",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            when (val state = uiState) {
                is LearningUiState.Idle -> {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldPrimaryBlueLight),
                        border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = OpenFieldPrimaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Interactive AI Tutor",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Master concepts through structured lessons, analogies, and quizzes offline.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = OpenFieldSecondaryText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "What concept would you like to learn?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = {
                            Text(
                                text = "e.g. Quantum Computing, Photosynthesis, Neural Networks...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = OpenFieldSecondaryText.copy(alpha = 0.6f)
                                )
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenFieldPrimaryBlue,
                            unfocusedBorderColor = OpenFieldCardBorder,
                            focusedContainerColor = OpenFieldBackground,
                            unfocusedContainerColor = OpenFieldBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Select explanation level:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Level Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LearningLevel.entries.forEach { level ->
                            val isSelected = currentLevel == level
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { controller.setLearningLevel(level) }
                            ) {
                                Text(
                                    text = level.displayName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Start interactive lesson",
                        onClick = { controller.generateLesson(topicInput, currentLevel) },
                        enabled = topicInput.isNotBlank(),
                        leadingIcon = Icons.Default.AutoAwesome
                    )

                    // Saved Lessons Section
                    if (savedSessions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "Recent Lessons",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        savedSessions.forEach { session ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { controller.resumeLesson(session) },
                                shape = CardShape,
                                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                                border = BorderStroke(1.dp, OpenFieldCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.lessonTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = OpenFieldNavyText
                                            ),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${session.topic} • ${session.level} Level • ${if (session.isQuizCompleted) "Quiz Done" else "In Progress"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = OpenFieldSecondaryText
                                            )
                                        )
                                    }
                                    IconButton(
                                        onClick = { controller.deleteLesson(session.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
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

                is LearningUiState.Generating -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = OpenFieldPrimaryBlue,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Synthesizing principles, analogies, and quizzes on-device…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }
                }

                is LearningUiState.ActiveLesson -> {
                    val lesson = state.lesson

                    // Lesson Header Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OpenFieldPrimaryBlueLight
                        ) {
                            Text(
                                text = "${lesson.level.displayName} Level",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = lesson.topic,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title & Core Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = OpenFieldNavyText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            MarkdownText(
                                text = lesson.overview,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = OpenFieldNavyText
                                )
                            )
                        }
                    }

                    // Concept Sections
                    lesson.sections.forEach { section ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                            border = BorderStroke(1.dp, OpenFieldCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = section.heading,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OpenFieldPrimaryBlue
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                MarkdownText(
                                    text = section.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            }
                        }
                    }

                    // Intuitive Analogy / Practical Example
                    if (lesson.practicalExample.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = OpenFieldPrimaryBlueLight.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Intuitive Analogy",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = OpenFieldPrimaryBlue
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = lesson.practicalExample,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            color = OpenFieldNavyText
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Deepen Understanding Actions
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Deepen Understanding",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OpenFieldSecondaryBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { controller.explainMoreSimply() }
                        ) {
                            Text(
                                text = "Explain Simpler",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = OpenFieldNavyText
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OpenFieldSecondaryBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { controller.showAnotherExample() }
                        ) {
                            Text(
                                text = "New Example",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = OpenFieldNavyText
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OpenFieldSecondaryBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { controller.summarizeTopic() }
                        ) {
                            Text(
                                text = "Summarize",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = OpenFieldNavyText
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Interactive Knowledge Check Quiz
                    val quiz = lesson.quiz
                    if (quiz != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Knowledge Check",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                            border = BorderStroke(1.5.dp, OpenFieldCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = quiz.question,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                quiz.options.forEachIndexed { idx, optionText ->
                                    val isSelected = state.selectedQuizOptionIndex == idx
                                    val isCorrect = idx == quiz.correctOptionIndex

                                    val optionBorderColor = when {
                                        state.isAnswerSubmitted && isCorrect -> Color(0xFF2E7D32)
                                        state.isAnswerSubmitted && isSelected && !isCorrect -> OpenFieldError
                                        isSelected -> OpenFieldPrimaryBlue
                                        else -> OpenFieldCardBorder
                                    }

                                    val optionBgColor = when {
                                        state.isAnswerSubmitted && isCorrect -> Color(0xFFE8F5E9)
                                        state.isAnswerSubmitted && isSelected && !isCorrect -> OpenFieldError.copy(alpha = 0.08f)
                                        isSelected -> OpenFieldPrimaryBlueLight
                                        else -> OpenFieldBackground
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = optionBgColor,
                                        border = BorderStroke(1.5.dp, optionBorderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable(enabled = !state.isAnswerSubmitted) {
                                                controller.selectQuizOption(idx)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val letter = ('A'.code + idx).toChar().toString()
                                            Text(
                                                text = "$letter.",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = OpenFieldNavyText
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = optionText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 13.sp,
                                                    color = OpenFieldNavyText
                                                )
                                            )
                                        }
                                    }
                                }

                                if (quiz.hint != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .clickable { controller.toggleHint() }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = null,
                                            tint = OpenFieldPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (state.showHint) "Hide hint" else "Need a hint?",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = OpenFieldPrimaryBlue,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }

                                    if (state.showHint) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = OpenFieldPrimaryBlueLight.copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = quiz.hint,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = OpenFieldNavyText,
                                                    fontSize = 12.sp
                                                ),
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (!state.isAnswerSubmitted) {
                                    PrimaryButton(
                                        text = "Check answer",
                                        onClick = { controller.submitQuizAnswer() },
                                        enabled = state.selectedQuizOptionIndex != null
                                    )
                                } else {
                                    // Feedback explanation
                                    val selectedIdx = state.selectedQuizOptionIndex ?: -1
                                    val gotItRight = selectedIdx == quiz.correctOptionIndex

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (gotItRight) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (gotItRight) "Correct! Well done." else "Incorrect.",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (gotItRight) Color(0xFF2E7D32) else OpenFieldError
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = quiz.explanation,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp,
                                                    color = OpenFieldNavyText
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Free-Response Concept Check
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Explain In Your Own Words",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "How would you explain the core mechanism of ${lesson.topic} to someone else?",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = freeResponseInput,
                                onValueChange = { freeResponseInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                placeholder = {
                                    Text(
                                        text = "Write your explanation here…",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OpenFieldSecondaryText.copy(alpha = 0.6f)
                                        )
                                    )
                                },
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OpenFieldPrimaryBlue,
                                    unfocusedBorderColor = OpenFieldCardBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            PrimaryButton(
                                text = "Evaluate my explanation",
                                onClick = {
                                    if (freeResponseInput.isNotBlank()) {
                                        controller.submitFreeResponseCheck(freeResponseInput)
                                        freeResponseInput = ""
                                    }
                                },
                                enabled = freeResponseInput.isNotBlank()
                            )
                        }
                    }

                    // Follow-up Q&A messages
                    if (state.followUps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Lesson Q&A",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        state.followUps.forEach { msg ->
                            ChatMessageBubble(
                                isUser = msg.isUser,
                                message = msg.text,
                                timestamp = msg.timestamp,
                                isComplete = msg.isComplete
                            )
                        }
                    }
                }

                is LearningUiState.Error -> {
                    Surface(
                        shape = CardShape,
                        color = OpenFieldError.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, OpenFieldError.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = OpenFieldError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lesson Error",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = OpenFieldError,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldError)
                            )
                            if (state.canRetry) {
                                Spacer(modifier = Modifier.height(12.dp))
                                PrimaryButton(
                                    text = "Retry lesson",
                                    onClick = { controller.generateLesson(topicInput, currentLevel) },
                                    leadingIcon = Icons.Default.Refresh
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
