package com.openfield.app.feature.home

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import com.openfield.app.core.components.HomePromptComposer
import com.openfield.app.feature.ai.engine.AiEngineState
import com.openfield.app.feature.ai.model.ModelInstallationState
import com.openfield.app.feature.ai.model.ModelManagementDialog
import com.openfield.app.core.components.OfflineStatusBanner
import com.openfield.app.feature.voice.VoiceInputManager
import com.openfield.app.feature.voice.VoiceInputState
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.RoundedFeatureCard
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun HomeScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToImageAnalysis: () -> Unit,
    onNavigateToDocumentQA: () -> Unit,
    onNavigateToProblemSolver: () -> Unit,
    onNavigateToLearnExplain: () -> Unit,
    onNavigateToCreateWrite: () -> Unit,
    onOpenProfileMenu: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val modelState by repository.modelRepository.installationState.collectAsState()
    val engineState by repository.aiEngine.engineState.collectAsState()
    var promptInput by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(false) }

    val voiceInputManager = remember { VoiceInputManager(context) }
    val voiceState by voiceInputManager.state.collectAsState()
    val isListening = voiceState is VoiceInputState.Listening

    DisposableEffect(Unit) {
        onDispose {
            voiceInputManager.release()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceInputManager.startListening { recognizedText ->
                promptInput = if (promptInput.isBlank()) recognizedText else "$promptInput $recognizedText"
            }
        }
    }

    // Image Picker for direct attachment from Home
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.analyzeSelectedImage(uri, context)
            onNavigateToImageAnalysis()
        }
    }

    // Document Picker for direct attachment from Home
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.importRealDocument(context, uri)
            onNavigateToDocumentQA()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OpenFieldBackground)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // Top Header: Logo + Brand Name (Left), Profile Button (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OpenFieldLogo(
                    size = 36.dp,
                    showText = true,
                    textFontSize = 19
                )

                Surface(
                    shape = CircleShape,
                    color = OpenFieldPrimaryBlueLight,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onOpenProfileMenu() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile and settings",
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline Status Banner with dynamic on-device AI state
            val bannerTitle = when {
                modelState is ModelInstallationState.Downloading ->
                    "Downloading AI Model (${(modelState as ModelInstallationState.Downloading).progressPercent}%)"
                modelState is ModelInstallationState.Verifying -> "Verifying AI Model..."
                modelState is ModelInstallationState.Installed && engineState is AiEngineState.Loading ->
                    "Loading AI Model..."
                modelState is ModelInstallationState.Installed ->
                    "Offline Mode"
                else -> "Setup Offline AI Model"
            }
            val bannerSubtitle = when {
                modelState is ModelInstallationState.Downloading -> "Tap to view background download"
                modelState is ModelInstallationState.Installed && engineState is AiEngineState.Loading ->
                    "Preparing local AI, features will be ready shortly"
                modelState is ModelInstallationState.Installed -> "All AI features available offline"
                else -> "Tap to download or import Gemma 4 E2B IT"
            }
            OfflineStatusBanner(
                title = bannerTitle,
                subtitle = bannerSubtitle,
                onClick = { showModelDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Title
            Text(
                text = "How can I help you today?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Large Prompt Composer
            HomePromptComposer(
                value = promptInput,
                onValueChange = { promptInput = it },
                onSend = { prompt ->
                    if (prompt.isNotBlank()) {
                        scope.launch {
                            val sessionId = repository.createNewSessionNow(prompt.take(30))
                            repository.sendMessage(sessionId, prompt)
                            promptInput = ""
                            onNavigateToChat(sessionId)
                        }
                    }
                },
                onPickImage = { imagePickerLauncher.launch("image/*") },
                onCameraClick = { imagePickerLauncher.launch("image/*") },
                onVoiceClick = {
                    if (isListening) {
                        voiceInputManager.stopListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onPickDocument = {
                    documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6 Feature Cards in a 2-Column Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Ask AI (Chat) — creates a fresh session
                RoundedFeatureCard(
                    title = "Ask AI\n(Chat)",
                    description = "Ask anything and get clear answers.",
                    icon = Icons.Default.ChatBubble,
                    onClick = {
                        scope.launch {
                            val sessionId = repository.createNewSessionNow("New chat")
                            onNavigateToChat(sessionId)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Analyze Image
                RoundedFeatureCard(
                    title = "Analyze\nImage",
                    description = "Identify, explain, and understand images.",
                    icon = Icons.Default.Image,
                    onClick = onNavigateToImageAnalysis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Ask Document
                RoundedFeatureCard(
                    title = "Ask\nDocument",
                    description = "Summarize and ask questions about PDFs.",
                    icon = Icons.Default.Description,
                    onClick = onNavigateToDocumentQA,
                    modifier = Modifier.weight(1f)
                )

                // Card 4: Solve a Problem
                RoundedFeatureCard(
                    title = "Solve a\nProblem",
                    description = "Troubleshoot with step-by-step help.",
                    icon = Icons.Default.Build,
                    onClick = onNavigateToProblemSolver,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 5: Learn & Explain
                RoundedFeatureCard(
                    title = "Learn &\nExplain",
                    description = "Understand concepts and learn skills.",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    onClick = onNavigateToLearnExplain,
                    modifier = Modifier.weight(1f)
                )

                // Card 6: Create & Write
                RoundedFeatureCard(
                    title = "Create\n& Write",
                    description = "Generate ideas and useful content.",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToCreateWrite,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showModelDialog) {
            item {
                ModelManagementDialog(
                    modelRepository = repository.modelRepository,
                    onDismissRequest = { showModelDialog = false }
                )
            }
        }
    }
}
