package com.openfield.app.feature.image

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.InlineErrorCard
import com.openfield.app.core.components.MarkdownContent
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.SectionTitle
import com.openfield.app.core.components.SuggestionChips
import com.openfield.app.core.components.TypingIndicator
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.mode.ImageAnalysisSections
import com.openfield.app.feature.voice.SpeechSynthesisManager
import com.openfield.app.feature.voice.rememberVoiceInput

private val imageQuickQuestions = listOf(
    "What is this?",
    "Read the text in this image",
    "Explain this diagram",
    "Is anything damaged or wrong?"
)

@Composable
fun ImageAnalysisScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProblemSolver: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val controller = repository.imageAnalysisController

    val selectedUri by controller.selectedImageUri.collectAsState()
    val isAnalyzing by controller.isAnalyzing.collectAsState()
    val isAnswering by controller.isAnswering.collectAsState()
    val sections by controller.analysisSections.collectAsState()
    val rawText by controller.rawAnalysisText.collectAsState()
    val errorMsg by controller.errorMessage.collectAsState()
    val followUps by controller.followUpMessages.collectAsState()

    var question by remember { mutableStateOf("") }
    var followUpInput by remember { mutableStateOf("") }
    var isSaved by remember(sections) { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val voice = rememberVoiceInput { recognized ->
        followUpInput = if (followUpInput.isBlank()) recognized else "$followUpInput $recognized"
    }

    val speechManager = remember { SpeechSynthesisManager(context) }
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    DisposableEffect(Unit) {
        onDispose { speechManager.release() }
    }

    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraPhotoUri != null) {
            question = ""
            controller.setImage(context, cameraPhotoUri!!)
        }
    }
    val takePhoto = {
        try {
            val photoFile = java.io.File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraPhotoUri = uri
            takePhotoLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            question = ""
            controller.setImage(context, uri)
        }
    }
    val pickImage = { imagePickerLauncher.launch("image/*") }

    LaunchedEffect(followUps.size) {
        if (followUps.isNotEmpty()) scrollState.animateScrollTo(scrollState.maxValue)
    }
    LaunchedEffect(followUps.lastOrNull()?.text?.length) {
        if (followUps.isNotEmpty() && scrollState.maxValue - scrollState.value < 600) scrollState.scrollTo(scrollState.maxValue)
    }

    val hasResult = sections != null && !isAnalyzing

    Scaffold(
        topBar = {
            ScreenTopBar(title = "Analyze Image", canNavigateBack = true, onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            if (hasResult) {
                ChatBottomComposer(
                    value = followUpInput,
                    onValueChange = { followUpInput = it },
                    onSend = { text ->
                        controller.askFollowUp(text)
                        followUpInput = ""
                    },
                    onAttach = pickImage,
                    onVoiceClick = voice.toggle,
                    isListening = voice.isListening,
                    onStopListening = voice.stop,
                    onCancelListening = voice.cancel,
                    isGenerating = isAnswering,
                    onStopGenerating = { controller.stopAnalysis() },
                    placeholder = "Ask about this image…",
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
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ImagePreview(
                uri = selectedUri,
                isBusy = isAnalyzing,
                onPick = pickImage,
                onTakePhoto = takePhoto,
                onRemove = {
                    controller.reset()
                    question = ""
                }
            )

            errorMsg?.let {
                InlineErrorCard(
                    title = "Image analysis problem",
                    message = it,
                    retryLabel = if (selectedUri != null) "Try again" else "Choose a photo",
                    onRetry = {
                        if (selectedUri != null) controller.analyzeImage(question) else pickImage()
                    }
                )
            }

            when {
                selectedUri != null && !isAnalyzing && sections == null -> {
                    SectionTitle("What do you want to know?")
                    SuggestionChips(suggestions = imageQuickQuestions, onClick = { question = it })
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        placeholder = { Text("Optional — e.g. \"Is this plant safe for pets?\"", color = OpenFieldSecondaryText.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenFieldPrimaryBlue,
                            unfocusedBorderColor = OpenFieldCardBorder,
                            focusedContainerColor = OpenFieldBackground,
                            unfocusedContainerColor = OpenFieldBackground
                        )
                    )
                    PrimaryButton(
                        text = "Analyze image",
                        onClick = { controller.analyzeImage(question) },
                        leadingIcon = Icons.Default.Search
                    )
                }

                isAnalyzing -> StreamingCard(rawText = rawText, onStop = { controller.stopAnalysis() })

                sections != null -> {
                    AnalysisResult(
                        s = sections!!,
                        isSpeaking = isSpeaking,
                        onSpeakToggle = {
                            if (isSpeaking) {
                                speechManager.stop()
                            } else {
                                val textToRead = buildString {
                                    append(sections!!.subject)
                                    sections!!.possibleIdentification?.let { append(". $it") }
                                    if (sections!!.whatISee.isNotBlank()) append(". ${sections!!.whatISee}")
                                    if (sections!!.explanation.isNotBlank()) append(". ${sections!!.explanation}")
                                }
                                speechManager.speak(textToRead)
                            }
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ResultAction(
                            label = "Share",
                            icon = Icons.Default.Share,
                            onClick = { controller.shareAnalysis(context) },
                            modifier = Modifier.weight(1f)
                        )
                        ResultAction(
                            label = if (isSaved) "Saved" else "Save",
                            icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            highlighted = isSaved,
                            onClick = {
                                if (!isSaved) {
                                    repository.saveCurrentAnalysis()
                                    isSaved = true
                                    Toast.makeText(context, "Analysis saved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ResultAction(
                            label = "Troubleshoot",
                            icon = Icons.Default.Build,
                            onClick = {
                                val summary = sections?.subject ?: "Inspecting image issue"
                                repository.sharedContextManager.handoffToTroubleshooter(
                                    problemSummary = summary,
                                    imagePath = selectedUri?.toString()
                                )
                                onNavigateToProblemSolver()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (followUps.isNotEmpty()) {
                SectionTitle("Follow-up questions")
                Column {
                    followUps.forEach { msg ->
                        ChatMessageBubble(isUser = msg.isUser, message = msg.text, isComplete = msg.isComplete)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ImagePreview(
    uri: Uri?,
    isBusy: Boolean,
    onPick: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (uri == null) 210.dp else 230.dp)
            .clip(CardShape)
            .background(if (uri == null) OpenFieldPrimaryBlueLight.copy(alpha = 0.5f) else OpenFieldSecondaryBackground),
        contentAlignment = Alignment.Center
    ) {
        if (uri == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select or Capture an Image",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Identify plants, diagnose machines, read diagrams",
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OpenFieldBackground,
                        border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isBusy, onClick = onTakePhoto)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(19.dp))
                            Text("Camera", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OpenFieldNavyText)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OpenFieldBackground,
                        border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isBusy, onClick = onPick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(19.dp))
                            Text("Gallery", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OpenFieldNavyText)
                        }
                    }
                }
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
                contentDescription = "Selected image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // On-device Gemma vision pill
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(OpenFieldSuccess)
                )
                Text(
                    text = "Vision Analysis • Ready",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
            if (!isBusy) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OverlayButton(Icons.Default.PhotoCamera, "Take photo", onTakePhoto)
                    OverlayButton(Icons.Default.SwapHoriz, "Choose photo", onPick)
                    OverlayButton(Icons.Default.Close, "Remove photo", onRemove)
                }
            }
        }
    }
}

@Composable
private fun OverlayButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.55f),
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun StreamingCard(rawText: String, onStop: () -> Unit) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = OpenFieldPrimaryBlue, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Looking at your image…",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                )
                Text(
                    "Stop",
                    style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onStop)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            if (rawText.isBlank()) {
                TypingIndicator(label = "Reading the picture — the first words can take a few seconds.")
            } else {
                MarkdownContent(text = rawText)
            }
        }
    }
}

@Composable
private fun AnalysisResult(
    s: ImageAnalysisSections,
    isSpeaking: Boolean = false,
    onSpeakToggle: () -> Unit = {}
) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OpenFieldLogo(size = 32.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "IDENTIFIED SUBJECT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldPrimaryBlue,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = Markdown.stripInline(s.subject),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText,
                                fontSize = 17.sp
                            )
                        )
                    }
                }

                IconButton(onClick = onSpeakToggle) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isSpeaking) "Stop voice" else "Read analysis aloud",
                        tint = if (isSpeaking) OpenFieldPrimaryBlue else OpenFieldSecondaryText
                    )
                }
            }

            if (!s.possibleIdentification.isNullOrBlank()) {
                ResultSection("Identification", s.possibleIdentification)
            }
            if (s.whatISee.isNotBlank()) {
                ResultSection("Visual Observations", s.whatISee)
            }
            if (s.importantDetails.isNotEmpty()) {
                ResultSection("Key Details", s.importantDetails.joinToString("\n") { "• $it" })
            }
            if (s.explanation.isNotBlank()) {
                ResultSection("Explanation & Context", s.explanation)
            }
            if (s.suggestedNextSteps.isNotEmpty()) {
                ResultSection("Recommended Next Steps", s.suggestedNextSteps.mapIndexed { i, step -> "${i + 1}. $step" }.joinToString("\n"))
            }
        }
    }

    val advisory = s.uncertaintyAndLimitations
        ?: "Note: Some similar plants, objects or components can be hazardous or misleading. Use this for general identification only."
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFEF7ED),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = Markdown.toPlainText(advisory).replace("\n", " "),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun ResultSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldSecondaryText, fontSize = 13.sp)
        )
        MarkdownContent(text = body)
    }
}

@Composable
private fun ResultAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(48.dp),
        border = BorderStroke(1.dp, if (highlighted) OpenFieldPrimaryBlue else OpenFieldCardBorder)
    ) {
        Icon(icon, contentDescription = null, tint = if (highlighted) OpenFieldPrimaryBlue else OpenFieldNavyText, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = if (highlighted) OpenFieldPrimaryBlue else OpenFieldNavyText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
