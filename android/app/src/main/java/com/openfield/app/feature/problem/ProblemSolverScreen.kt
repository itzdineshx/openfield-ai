package com.openfield.app.feature.problem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.openfield.app.feature.ai.controller.TroubleshootingUiState

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

    var problemInput by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            controller.setImage(context, uri)
        }
    }

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
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart troubleshooting"
                            )
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            when (val state = uiState) {
                is TroubleshootingUiState.Idle -> {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(OpenFieldPrimaryBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Interactive Guided Diagnostics",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Answer targeted questions to systematically pinpoint root causes offline.",
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
                        text = "What problem are you facing?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = problemInput,
                        onValueChange = { problemInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = {
                            Text(
                                text = "Describe the symptoms (e.g. 'Car engine clicks but won\\'t crank', 'Water heater leaking from base')...",
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

                    // Contextual Image Attachment
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (selectedImageUri != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OpenFieldPrimaryBlueLight,
                                border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Photo attached",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OpenFieldPrimaryBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = OpenFieldError,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { controller.removeImage() }
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OpenFieldCardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Attach photo of equipment",
                                    color = OpenFieldNavyText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PrimaryButton(
                        text = "Begin diagnostic",
                        onClick = {
                            if (problemInput.isNotBlank()) {
                                controller.startTroubleshooting(problemInput)
                            }
                        },
                        leadingIcon = Icons.Default.PlayArrow,
                        enabled = problemInput.isNotBlank()
                    )

                    val savedSessions by controller.savedSessions.collectAsState()
                    if (savedSessions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Recent Diagnostic Sessions",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        savedSessions.take(5).forEach { session ->
                            Surface(
                                shape = CardShape,
                                color = OpenFieldBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { controller.resumeSession(session) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (session.isResolved) Icons.Default.CheckCircle else Icons.Default.Build,
                                        contentDescription = null,
                                        tint = if (session.isResolved) Color(0xFF2E7D32) else OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.problem,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = OpenFieldNavyText
                                            ),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${session.category} • Step ${session.currentStepNumber} • ${if (session.isResolved) "Resolved" else "In Progress"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = OpenFieldSecondaryText
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is TroubleshootingUiState.Generating -> {
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
                                text = state.stepMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gemma 4 E2B is formulating next check offline…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }
                }

                is TroubleshootingUiState.DiagnosticStep -> {
                    val assessment = state.assessment

                    // Step Tracker & Category Pill
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
                                text = "Step ${assessment.currentStepNumber} of ${assessment.totalEstimatedSteps}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = assessment.category,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Safety Warning Banner (if present)
                    if (assessment.isEscalationNeeded) {
                        Surface(
                            shape = CardShape,
                            color = Color(0xFFFFF3E0),
                            border = BorderStroke(1.5.dp, Color(0xFFE65100)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Professional Escalation Advised",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = assessment.escalationReason ?: "This diagnostic check touches high-risk electrical, pressure, or hazardous components. Disconnect power/halt operation and consult a certified technician.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OpenFieldNavyText,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else if (!assessment.safetyNotice.isNullOrBlank()) {
                        Surface(
                            shape = CardShape,
                            color = Color(0xFFFFF4E5),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = assessment.safetyNotice,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE65100)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Diagnostic Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = assessment.question,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp,
                                    color = OpenFieldNavyText
                                )
                            )

                            if (!assessment.explanation.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = assessment.explanation,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        color = OpenFieldSecondaryText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selectable Options
                    Text(
                        text = "Select your observation:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    assessment.options.forEach { option ->
                        val isSelected = state.selectedOptionId == option.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) OpenFieldPrimaryBlueLight else OpenFieldBackground,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { controller.selectOption(option.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.id,
                                        color = if (isSelected) Color.White else OpenFieldNavyText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        color = OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Next step",
                        onClick = { controller.submitAnswerAndProceed() },
                        leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        enabled = state.selectedOptionId != null
                    )

                    // Prior Steps Summary
                    if (history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Diagnostic history",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = OpenFieldSecondaryText,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        history.forEach { item ->
                            Text(
                                text = "Step ${item.stepIndex}: ${item.question} → \"${item.selectedOptionText}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = OpenFieldSecondaryText
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                is TroubleshootingUiState.Resolution -> {
                    val assessment = state.assessment

                    // Resolution Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                        border = BorderStroke(1.5.dp, Color(0xFF2E7D32))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Diagnosis Reached",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            if (!assessment.likelyCause.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Probable Cause:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = assessment.likelyCause,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            }

                            if (!assessment.recommendedSolution.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Recommended Solution:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                MarkdownText(
                                    text = assessment.recommendedSolution,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Verification Check: "Did this resolve the issue?"
                    Surface(
                        shape = CardShape,
                        color = OpenFieldSecondaryBackground,
                        border = BorderStroke(1.dp, OpenFieldCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Did this solution resolve the problem?",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (state.didFixWork == null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { controller.verifySolution(true) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                                    ) {
                                        Text(text = "Yes, fixed!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { controller.verifySolution(false) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, OpenFieldError)
                                    ) {
                                        Text(text = "No, still broken", color = OpenFieldError, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (state.didFixWork == true) {
                                Text(
                                    text = "Great! Session saved to your troubleshooting log.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            } else {
                                Text(
                                    text = "Recorded. You can explore alternative root causes below or restart.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldError,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                PrimaryButton(
                                    text = "Explore alternative root causes",
                                    onClick = { controller.exploreAlternativeCauses() },
                                    leadingIcon = Icons.Default.Search
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Troubleshoot another problem",
                        onClick = {
                            controller.reset()
                            problemInput = ""
                        },
                        leadingIcon = Icons.Default.Refresh
                    )
                }

                is TroubleshootingUiState.Error -> {
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
                                    text = "Diagnostic Error",
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
                                    text = "Retry step",
                                    onClick = { controller.retryLastStep() },
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
