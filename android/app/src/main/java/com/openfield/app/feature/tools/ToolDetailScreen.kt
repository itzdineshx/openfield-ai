package com.openfield.app.feature.tools

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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

@Composable
fun ToolDetailScreen(
    toolId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val tools by repository.tools.collectAsState()
    val tool = tools.find { it.id == toolId } ?: tools.first()

    var inputText by remember(toolId) { mutableStateOf("") }
    var outputText by remember(toolId) { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = tool.title,
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        inputText = ""
                        outputText = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
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

            // Tool header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OpenFieldPrimaryBlueLight,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getToolIcon(tool.id),
                            contentDescription = null,
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OpenFieldSecondaryText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Input Section
            Text(
                text = "Input",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = {
                    Text(
                        text = tool.inputPlaceholder,
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
                text = if (isExecuting) "Generating Offline..." else "Execute Offline",
                onClick = {
                    if (inputText.isNotBlank() && !isExecuting) {
                        scope.launch {
                            isExecuting = true
                            outputText = ""
                            try {
                                if (!repository.aiEngine.isReady()) {
                                    val file = repository.modelRepository.getActiveModelFile()
                                    if (file == null) {
                                        outputText = "No local AI model is installed. Open AI Model Management to download or import the model."
                                        isExecuting = false
                                        return@launch
                                    }
                                    outputText = "Loading model into memory..."
                                    val initResult = repository.aiEngine.initialize(file)
                                    if (initResult.isFailure) {
                                        outputText = "Could not load model: ${initResult.exceptionOrNull()?.message ?: "unknown error"}"
                                        isExecuting = false
                                        return@launch
                                    }
                                    outputText = ""
                                }

                                repository.executeToolActionStream(tool.id, inputText).collect { token ->
                                    outputText += token
                                }
                            } catch (e: Exception) {
                                outputText = if (outputText.isNotBlank()) outputText else "Error executing offline tool: ${e.message}"
                            } finally {
                                isExecuting = false
                            }
                        }
                    }
                },
                leadingIcon = Icons.Default.PlayArrow,
                enabled = !isExecuting
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Output Section
            if (outputText.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(outputText))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Text(
                        text = outputText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
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

