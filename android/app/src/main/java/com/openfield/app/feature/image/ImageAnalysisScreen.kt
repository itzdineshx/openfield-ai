package com.openfield.app.feature.image

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.MarkdownText
import com.openfield.app.core.components.OpenFieldLogo
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

@Composable
fun ImageAnalysisScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val controller = repository.imageAnalysisController

    val selectedUri by controller.selectedImageUri.collectAsState()
    val isAnalyzing by controller.isAnalyzing.collectAsState()
    val sections by controller.analysisSections.collectAsState()
    val rawText by controller.rawAnalysisText.collectAsState()
    val errorMsg by controller.errorMessage.collectAsState()
    val followUps by controller.followUpMessages.collectAsState()

    var userCustomPrompt by remember { mutableStateOf("") }
    var followUpInput by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isSaved = false
            controller.setImage(context, uri)
            controller.analyzeImage(userCustomPrompt.takeIf { it.isNotBlank() })
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Analyze Image",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (isAnalyzing) {
                        IconButton(onClick = { controller.stopAnalysis() }) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop analysis",
                                tint = OpenFieldError
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (sections != null || rawText.isNotBlank()) {
                ChatBottomComposer(
                    value = followUpInput,
                    onValueChange = { followUpInput = it },
                    onSend = { text ->
                        controller.askFollowUp(text)
                        followUpInput = ""
                    },
                    onAttach = { imagePickerLauncher.launch("image/*") },
                    onVoiceClick = { },
                    placeholder = "Ask about this image...",
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

            // Image Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = CardShape,
                border = BorderStroke(1.dp, OpenFieldCardBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = OpenFieldPrimaryBlue,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tap to pick an image",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plant, animal, device, text, or scene",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }

                    if (isAnalyzing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Analyzing visually…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    if (!isAnalyzing && selectedUri != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Change photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Image Preview Replace & Remove Action Row
            if (selectedUri != null && !isAnalyzing) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Text("Replace photo", fontSize = 12.sp, color = OpenFieldNavyText)
                    }
                    OutlinedButton(
                        onClick = {
                            controller.reset()
                            userCustomPrompt = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, OpenFieldError.copy(alpha = 0.4f))
                    ) {
                        Text("Remove photo", fontSize = 12.sp, color = OpenFieldError)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Question input & Analyze action button
            if (selectedUri != null && !isAnalyzing && sections == null) {
                // Quick category prompts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Error / Code" to "Analyze this error code or screenshot message and explain the cause.",
                        "Diagram" to "Explain this diagram or schematic and its key steps.",
                        "Hardware" to "Identify this hardware/component and inspect its condition."
                    ).forEach { (tag, query) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = OpenFieldSecondaryBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { userCustomPrompt = query }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = OpenFieldNavyText,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userCustomPrompt,
                    onValueChange = { userCustomPrompt = it },
                    placeholder = {
                        Text(
                            text = "What would you like to know about this image? (optional)",
                            fontSize = 13.sp,
                            color = OpenFieldSecondaryText.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenFieldPrimaryBlue,
                        unfocusedBorderColor = OpenFieldCardBorder,
                        focusedContainerColor = OpenFieldBackground,
                        unfocusedContainerColor = OpenFieldBackground
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Analyze image",
                    onClick = {
                        controller.analyzeImage(userCustomPrompt.takeIf { it.isNotBlank() })
                    },
                    leadingIcon = Icons.Default.Search
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Error Message Banner
            if (errorMsg != null) {
                Surface(
                    shape = CardShape,
                    color = OpenFieldError.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, OpenFieldError.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = OpenFieldError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMsg ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldError,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Structured Analysis Result Sections
            val s = sections
            if (s != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title / Identification badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OpenFieldLogo(size = 24.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = s.subject,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = OpenFieldNavyText
                                )
                            )
                        }

                        if (!s.possibleIdentification.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OpenFieldPrimaryBlueLight
                            ) {
                                Text(
                                    text = "Identification: ${s.possibleIdentification}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldPrimaryBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 1: What I see
                        SectionHeader(title = "What I see")
                        Text(
                            text = s.whatISee,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = OpenFieldNavyText
                            )
                        )

                        // Section 2: Important details
                        if (s.importantDetails.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SectionHeader(title = "Important details")
                            s.importantDetails.forEach { detail ->
                                Text(
                                    text = "• $detail",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = OpenFieldNavyText
                                    ),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Section 3: Explanation
                        if (s.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SectionHeader(title = "Explanation")
                            MarkdownText(
                                text = s.explanation,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = OpenFieldNavyText
                                )
                            )
                        }

                        // Section 4: Suggested next steps
                        if (s.suggestedNextSteps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SectionHeader(title = "Suggested next steps")
                            s.suggestedNextSteps.forEachIndexed { idx, step ->
                                Text(
                                    text = "${idx + 1}. $step",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = OpenFieldNavyText
                                    ),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Section 5: Uncertainty & limitations
                        if (!s.uncertaintyAndLimitations.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = OpenFieldSecondaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = s.uncertaintyAndLimitations,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = OpenFieldSecondaryText
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Share & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = OpenFieldNavyText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Share", color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            isSaved = !isSaved
                            if (isSaved) repository.saveCurrentAnalysis()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isSaved) OpenFieldPrimaryBlue else OpenFieldNavyText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaved) "Saved" else "Save",
                            color = if (isSaved) OpenFieldPrimaryBlue else OpenFieldNavyText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (rawText.isNotBlank()) {
                // Streaming text preview while parsing sections
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = OpenFieldPrimaryBlue,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extracting details…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        MarkdownText(
                            text = rawText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = OpenFieldNavyText
                            )
                        )
                    }
                }
            }

            // Follow-up Q&A messages about this image
            if (followUps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Follow-up questions",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = OpenFieldNavyText
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                followUps.forEach { msg ->
                    ChatMessageBubble(
                        isUser = msg.isUser,
                        message = msg.text,
                        timestamp = msg.timestamp,
                        isComplete = msg.isComplete
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = OpenFieldNavyText
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
