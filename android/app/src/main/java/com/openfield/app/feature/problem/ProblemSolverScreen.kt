package com.openfield.app.feature.problem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.AnswerOptionRow
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
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.core.theme.OpenFieldSuccessBg
import com.openfield.app.core.theme.OpenFieldWarning
import com.openfield.app.core.theme.OpenFieldWarningBg
import com.openfield.app.data.local.entity.TroubleshootSessionEntity
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.controller.TroubleshootingUiState
import com.openfield.app.feature.ai.mode.DiagnosticAssessment
import com.openfield.app.feature.ai.mode.DiagnosticStepHistory

private val exampleProblems = listOf(
    "Phone won't charge",
    "Wi-Fi keeps disconnecting",
    "Laptop is overheating",
    "Car engine clicks but won't start"
)

@Composable
fun ProblemSolverScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val controller = repository.troubleshootingController
    val uiState by controller.uiState.collectAsState()
    val history by controller.history.collectAsState()
    val selectedImageUri by controller.selectedImageUri.collectAsState()
    val savedSessions by controller.savedSessions.collectAsState()
    val problem by controller.problemDescription.collectAsState()

    var problemInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) controller.setImage(context, uri)
    }

    LaunchedEffect(uiState::class) { scrollState.animateScrollTo(0) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Solve a Problem",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (uiState !is TroubleshootingUiState.Idle) {
                        IconButton(onClick = {
                            controller.reset()
                            problemInput = ""
                        }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Start over")
                        }
                    }
                }
            )
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
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FeatureHeaderCard(
                icon = Icons.Default.Build,
                title = "Guided Troubleshooting",
                subtitle = "Let me help you solve this step by step."
            )

            if (uiState !is TroubleshootingUiState.Idle && problem.isNotBlank()) {
                ProblemSummary(problem)
            }

            when (val state = uiState) {
                is TroubleshootingUiState.Idle -> IdleContent(
                    problemInput = problemInput,
                    onProblemChange = { problemInput = it },
                    hasPhoto = selectedImageUri != null,
                    onAttachPhoto = { imagePickerLauncher.launch("image/*") },
                    onRemovePhoto = { controller.removeImage() },
                    onStart = { controller.startTroubleshooting(problemInput) },
                    savedSessions = savedSessions,
                    onResume = { controller.resumeSession(it) },
                    onDelete = { controller.deleteSession(it) }
                )

                is TroubleshootingUiState.Generating -> GeneratingCard(
                    message = state.stepMessage,
                    detail = "Working out the most useful next check — this can take up to a minute on device.",
                    onStop = { controller.cancelGeneration() }
                )

                is TroubleshootingUiState.DiagnosticStep -> StepContent(
                    assessment = state.assessment,
                    selectedOptionId = state.selectedOptionId,
                    history = history,
                    onSelect = { controller.selectOption(it) },
                    onNext = { controller.submitAnswerAndProceed() },
                    onCustomAnswer = { controller.submitCustomAnswer(it) }
                )

                is TroubleshootingUiState.Resolution -> ResolutionContent(
                    assessment = state.assessment,
                    didFixWork = state.didFixWork,
                    onVerify = { controller.verifySolution(it) },
                    onExploreAlternatives = { controller.exploreAlternativeCauses() },
                    onNewProblem = {
                        controller.reset()
                        problemInput = ""
                    }
                )

                is TroubleshootingUiState.Error -> InlineErrorCard(
                    title = "This step didn't work",
                    message = state.message,
                    retryLabel = "Retry step",
                    onRetry = if (state.canRetry) ({ controller.retryLastStep() }) else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProblemSummary(problem: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = OpenFieldPrimaryBlueLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = "Your problem",
                style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = problem,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldNavyText, fontSize = 14.sp)
            )
        }
    }
}

@Composable
private fun IdleContent(
    problemInput: String,
    onProblemChange: (String) -> Unit,
    hasPhoto: Boolean,
    onAttachPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onStart: () -> Unit,
    savedSessions: List<TroubleshootSessionEntity>,
    onResume: (TroubleshootSessionEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("What is the problem you're facing?")
        OutlinedTextField(
            value = problemInput,
            onValueChange = onProblemChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    text = "Describe what's happening, e.g. \"My laptop shows a blue screen after an update\"",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = OpenFieldSecondaryText.copy(alpha = 0.7f))
                )
            },
            colors = fieldColors()
        )
        SuggestionChips(suggestions = exampleProblems, onClick = onProblemChange)

        if (hasPhoto) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OpenFieldPrimaryBlueLight,
                border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Photo attached", color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = OpenFieldError,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable(onClick = onRemovePhoto)
                    )
                }
            }
        } else {
            OutlinedButton(
                onClick = onAttachPhoto,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add a photo (optional)", color = OpenFieldNavyText, fontSize = 13.sp)
            }
        }

        PrimaryButton(
            text = "Start troubleshooting",
            onClick = onStart,
            leadingIcon = Icons.Default.PlayArrow,
            enabled = problemInput.isNotBlank()
        )

        if (savedSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle("Recent sessions")
            savedSessions.take(5).forEach { session ->
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
                        Icon(
                            imageVector = if (session.isResolved) Icons.Default.CheckCircle else Icons.Default.Build,
                            contentDescription = null,
                            tint = if (session.isResolved) OpenFieldSuccess else OpenFieldPrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.problem,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                            )
                            Text(
                                text = "${session.category} • ${if (session.isResolved) "Resolved" else "Step ${session.currentStepNumber} — tap to continue"}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = OpenFieldSecondaryText)
                            )
                        }
                        IconButton(onClick = { onDelete(session.id) }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete session",
                                tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepContent(
    assessment: DiagnosticAssessment,
    selectedOptionId: String?,
    history: List<DiagnosticStepHistory>,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onCustomAnswer: (String) -> Unit
) {
    var otherSelected by remember(assessment) { mutableStateOf(false) }
    var otherText by remember(assessment) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StepProgress(assessment.currentStepNumber, assessment.totalEstimatedSteps, assessment.category)

        if (assessment.isEscalationNeeded) {
            Notice(
                title = "Get professional help",
                text = assessment.escalationReason
                    ?: "This involves high-risk components. Switch off power or stop using the device and contact a qualified technician."
            )
        } else if (!assessment.safetyNotice.isNullOrBlank()) {
            Notice(title = "Safety first", text = assessment.safetyNotice)
        }

        InlineMarkdown(
            text = assessment.question,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                color = OpenFieldNavyText
            )
        )
        if (!assessment.explanation.isNullOrBlank()) {
            MarkdownContent(
                text = assessment.explanation,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 20.sp, color = OpenFieldSecondaryText)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            assessment.options.forEach { option ->
                AnswerOptionRow(
                    text = option.text,
                    state = if (!otherSelected && selectedOptionId == option.id) OptionState.SELECTED else OptionState.DEFAULT,
                    onClick = {
                        otherSelected = false
                        onSelect(option.id)
                    }
                )
            }
            AnswerOptionRow(
                text = "Something else",
                state = if (otherSelected) OptionState.SELECTED else OptionState.DEFAULT,
                onClick = { otherSelected = true }
            )
            if (otherSelected) {
                OutlinedTextField(
                    value = otherText,
                    onValueChange = { otherText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Describe what you see or hear…", color = OpenFieldSecondaryText.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OpenFieldSecondaryText) },
                    colors = fieldColors()
                )
            }
        }

        PrimaryButton(
            text = "Next",
            onClick = { if (otherSelected) onCustomAnswer(otherText) else onNext() },
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            enabled = if (otherSelected) otherText.isNotBlank() else selectedOptionId != null
        )

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            HistoryList(history)
        }
    }
}

@Composable
private fun StepProgress(step: Int, total: Int, category: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Step $step of $total",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = category,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText),
                modifier = Modifier.weight(1f, fill = false)
            )
        }
        LinearProgressIndicator(
            progress = { (step.toFloat() / total.coerceAtLeast(1)).coerceIn(0.05f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = OpenFieldPrimaryBlue,
            trackColor = OpenFieldSecondaryBackground
        )
    }
}

@Composable
private fun Notice(title: String, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldWarningBg,
        border = BorderStroke(1.dp, OpenFieldWarning.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = OpenFieldWarning, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldWarning))
                InlineMarkdown(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp, color = OpenFieldNavyText)
                )
            }
        }
    }
}

@Composable
private fun HistoryList(history: List<DiagnosticStepHistory>) {
    Surface(
        shape = CardShape,
        color = OpenFieldSecondaryBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Your answers so far",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldSecondaryText)
            )
            history.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        if (item.question.isNotBlank()) {
                            Text(
                                text = Markdown.stripInline(item.question),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.sp)
                            )
                        }
                        Text(
                            text = Markdown.stripInline(item.selectedOptionText),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldNavyText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionContent(
    assessment: DiagnosticAssessment,
    didFixWork: Boolean?,
    onVerify: (Boolean) -> Unit,
    onExploreAlternatives: () -> Unit,
    onNewProblem: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = CardShape,
            color = OpenFieldSuccessBg,
            border = BorderStroke(1.dp, OpenFieldSuccess.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OpenFieldSuccess, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Here's what's likely going on",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = OpenFieldSuccess)
                    )
                    Text(
                        assessment.category,
                        style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText)
                    )
                }
            }
        }

        if (!assessment.safetyNotice.isNullOrBlank()) {
            Notice(title = "Safety first", text = assessment.safetyNotice)
        }

        if (!assessment.likelyCause.isNullOrBlank()) {
            SectionTitle("Likely cause")
            MarkdownContent(text = assessment.likelyCause)
        }

        if (!assessment.recommendedSolution.isNullOrBlank()) {
            SectionTitle("How to fix it")
            MarkdownContent(text = assessment.recommendedSolution)
        }

        Surface(
            shape = CardShape,
            color = OpenFieldSecondaryBackground,
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Did this fix the problem?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                )
                when (didFixWork) {
                    null -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { onVerify(true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, OpenFieldSuccess)
                        ) { Text("Yes, it's fixed", color = OpenFieldSuccess, fontWeight = FontWeight.SemiBold) }
                        OutlinedButton(
                            onClick = { onVerify(false) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, OpenFieldError)
                        ) { Text("No, not yet", color = OpenFieldError, fontWeight = FontWeight.SemiBold) }
                    }
                    true -> Text(
                        "Great! This session is saved in your recent sessions.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldSuccess, fontWeight = FontWeight.Medium)
                    )
                    false -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Let's look at other possible causes.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldNavyText)
                        )
                        PrimaryButton(text = "Try other causes", onClick = onExploreAlternatives, leadingIcon = Icons.Default.Search)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onNewProblem,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = OpenFieldNavyText, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Solve another problem", color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OpenFieldPrimaryBlue,
    unfocusedBorderColor = OpenFieldCardBorder,
    focusedContainerColor = OpenFieldBackground,
    unfocusedContainerColor = OpenFieldBackground
)
