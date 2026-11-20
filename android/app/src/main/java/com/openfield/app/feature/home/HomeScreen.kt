package com.openfield.app.feature.home

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
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
    onNavigateToTasks: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val modelState by repository.modelRepository.installationState.collectAsState()
    val engineState by repository.aiEngine.engineState.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val activeTask = remember(tasks) { tasks.firstOrNull { it.status != "COMPLETED" && it.status != "CANCELLED" } }
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
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            voiceInputManager.startListening { recognizedText ->
                                promptInput = if (promptInput.isBlank()) recognizedText else "$promptInput $recognizedText"
                            }
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onPickDocument = {
                    documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                }
            )

            // Dynamic "Continue Where You Left Off" Hero Card
            if (activeTask != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTasks() },
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                    border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OpenFieldPrimaryBlueLight,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val rawSubtasks = remember(activeTask.subtasksJson) {
                                (com.openfield.app.feature.ai.mode.LenientJson.parse(activeTask.subtasksJson) as? List<*>) ?: emptyList<Any>()
                            }
                            val subtaskCount = rawSubtasks.size
                            val completedCount = rawSubtasks.count { (it as? Map<*, *>)?.get("isCompleted") == true }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONTINUE YOUR WORK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OpenFieldPrimaryBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                if (subtaskCount > 0) {
                                    Text(
                                        text = "$completedCount/$subtaskCount steps",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = OpenFieldSecondaryText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = activeTask.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OpenFieldNavyText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (rawSubtasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val progress = if (subtaskCount == 0) 0f else completedCount.toFloat() / subtaskCount.toFloat()
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = OpenFieldPrimaryBlue,
                                    trackColor = OpenFieldCardBorder
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Resume task",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

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
