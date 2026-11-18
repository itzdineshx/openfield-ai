package com.openfield.app.feature.problem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import kotlinx.coroutines.launch

@Composable
fun ProblemSolverScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val scope = rememberCoroutineScope()

    var problem by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var isDiagnosing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Solve a Problem",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = { }
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
                            text = "Guided Troubleshooting",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Describe what's going wrong and get a step-by-step diagnostic from the local AI.",
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
                value = problem,
                onValueChange = { problem = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = {
                    Text(
                        text = "Describe the issue (device, symptoms, what you tried)...",
                        style = MaterialTheme.typography.bodyMedium.copy(
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

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryButton(
                text = if (isDiagnosing) "Generating offline..." else "Generate diagnostic",
                onClick = {
                    if (problem.isNotBlank() && !isDiagnosing) {
                        scope.launch {
                            isDiagnosing = true
                            diagnosis = ""
                            try {
                                if (!repository.aiEngine.isReady()) {
                                    val file = repository.modelRepository.getActiveModelFile()
                                    if (file == null) {
                                        diagnosis = "No local AI model is installed. Open AI Model Management to download or import the model."
                                        isDiagnosing = false
                                        return@launch
                                    }
                                    diagnosis = "Loading model..."
                                    val initResult = repository.aiEngine.initialize(file)
                                    if (initResult.isFailure) {
                                        diagnosis = "Could not load model: ${initResult.exceptionOrNull()?.message ?: "unknown error"}"
                                        isDiagnosing = false
                                        return@launch
                                    }
                                    diagnosis = ""
                                }

                                repository.problemSolverUseCase.diagnoseStream(problem).collect { token ->
                                    diagnosis += token
                                }
                            } catch (e: Exception) {
                                if (diagnosis.isBlank()) diagnosis = "Error generating diagnostic: ${e.message}"
                            } finally {
                                isDiagnosing = false
                            }
                        }
                    }
                },
                leadingIcon = Icons.Default.PlayArrow,
                enabled = !isDiagnosing
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (diagnosis.isNotBlank()) {
                Text(
                    text = "Diagnostic Plan",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = OpenFieldNavyText
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = CardShape,
                    color = OpenFieldPrimaryBlueLight,
                    border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = diagnosis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = OpenFieldNavyText
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
