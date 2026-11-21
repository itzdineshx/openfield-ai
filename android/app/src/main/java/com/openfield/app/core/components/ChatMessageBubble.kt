package com.openfield.app.core.components

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.theme.BubbleShapeUser
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldMutedText
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText

data class StepItem(
    val number: Int,
    val text: String
)

@Composable
fun ChatMessageBubble(
    isUser: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    timestamp: String? = null,
    steps: List<StepItem> = emptyList(),
    imageUri: Any? = null,
    sourcePage: String? = null,
    isComplete: Boolean = true,
    mode: String? = null,
    pendingLabel: String = "Thinking…",
    onSourceClick: (() -> Unit)? = null,
    onSpeakClick: (() -> Unit)? = null,
    isSpeaking: Boolean = false,
    onSaveClick: (() -> Unit)? = null,
    isSaved: Boolean = false,
    onShareClick: (() -> Unit)? = null,
    onOpenInStudio: ((String) -> Unit)? = null,
    onCreateChecklist: ((String) -> Unit)? = null,
    onAskQuiz: ((String) -> Unit)? = null,
    onTranslate: ((String) -> Unit)? = null,
    onFollowUpAction: ((String) -> Unit)? = null
) {
    if (isUser) {
        UserMessage(message, imageUri, timestamp, modifier)
    } else {
        AssistantMessage(
            message = message,
            modifier = modifier,
            timestamp = timestamp,
            steps = steps,
            imageUri = imageUri,
            sourcePage = sourcePage,
            isComplete = isComplete,
            mode = mode,
            pendingLabel = pendingLabel,
            onSourceClick = onSourceClick,
            onSpeakClick = onSpeakClick,
            isSpeaking = isSpeaking,
            onSaveClick = onSaveClick,
            isSaved = isSaved,
            onShareClick = onShareClick,
            onOpenInStudio = onOpenInStudio,
            onCreateChecklist = onCreateChecklist,
            onAskQuiz = onAskQuiz,
            onTranslate = onTranslate,
            onFollowUpAction = onFollowUpAction
        )
    }
}

@Composable
private fun UserMessage(message: String, imageUri: Any?, timestamp: String?, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = BubbleShapeUser,
            color = OpenFieldPrimaryBlueLight,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                if (imageUri != null) {
                    AttachmentImage(imageUri)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            color = OpenFieldNavyText
                        )
                    )
                }
            }
        }
        if (!timestamp.isNullOrBlank()) {
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = OpenFieldMutedText),
                modifier = Modifier.padding(top = 3.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun AssistantMessage(
    message: String,
    modifier: Modifier,
    timestamp: String?,
    steps: List<StepItem>,
    imageUri: Any?,
    sourcePage: String?,
    isComplete: Boolean,
    mode: String?,
    pendingLabel: String,
    onSourceClick: (() -> Unit)?,
    onSpeakClick: (() -> Unit)?,
    isSpeaking: Boolean,
    onSaveClick: (() -> Unit)?,
    isSaved: Boolean,
    onShareClick: (() -> Unit)?,
    onOpenInStudio: ((String) -> Unit)? = null,
    onCreateChecklist: ((String) -> Unit)? = null,
    onAskQuiz: ((String) -> Unit)? = null,
    onTranslate: ((String) -> Unit)? = null,
    onFollowUpAction: ((String) -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        OpenFieldLogo(size = 28.dp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            val effectiveMode = mode ?: if (imageUri != null) "IMAGE" else if (sourcePage != null) "DOCUMENT" else null
            if (effectiveMode != null) {
                ModeResultBadge(effectiveMode)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (imageUri != null) {
                AttachmentImage(imageUri, isVisionAnalysis = true)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (message.isBlank() && !isComplete) {
                TypingIndicator(label = pendingLabel)
            } else if (message.isNotBlank()) {
                when {
                    effectiveMode.equals("WRITE", ignoreCase = true) -> {
                        WritingStudioResultCard(
                            text = message,
                            isComplete = isComplete,
                            onSaveClick = onSaveClick,
                            onCopyClick = {
                                clipboard.setText(AnnotatedString(message))
                                Toast.makeText(context, "Draft copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onFollowUpAction = onFollowUpAction
                        )
                    }
                    effectiveMode.equals("SOLVE", ignoreCase = true) || effectiveMode.equals("TROUBLESHOOT", ignoreCase = true) -> {
                        TroubleshootResultCard(
                            text = message,
                            isComplete = isComplete,
                            onFollowUpAction = onFollowUpAction
                        )
                    }
                    effectiveMode.equals("EXPLAIN", ignoreCase = true) || effectiveMode.equals("LEARN", ignoreCase = true) -> {
                        TutorLessonResultCard(
                            text = message,
                            isComplete = isComplete,
                            onFollowUpAction = onFollowUpAction
                        )
                    }
                    effectiveMode.equals("IMAGE", ignoreCase = true) -> {
                        VisionResultCard(
                            text = message,
                            isComplete = isComplete,
                            onFollowUpAction = onFollowUpAction
                        )
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MarkdownContent(
                                text = message,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 3.dp)
                            )
                            if (!isComplete) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StreamingCursor()
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Generating...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = OpenFieldPrimaryBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                MarkdownContent(
                    text = "**Try these steps:**\n\n" + steps.joinToString("\n") { "${it.number}. ${it.text}" }
                )
            }

            if (sourcePage != null && isComplete) {
                Spacer(modifier = Modifier.height(12.dp))
                SourceReferenceCard(pageLabel = sourcePage, onClick = onSourceClick ?: {})
            }

            if (isComplete && message.isNotBlank()) {
                if (effectiveMode == null || effectiveMode.equals("ASK_AI", ignoreCase = true)) {
                    InStreamHandoffChips(
                        message = message,
                        onOpenInStudio = onOpenInStudio,
                        onCreateChecklist = onCreateChecklist,
                        onAskQuiz = onAskQuiz,
                        onTranslate = onTranslate
                    )
                }

                MessageActions(
                    message = message,
                    timestamp = timestamp,
                    onSpeakClick = onSpeakClick,
                    isSpeaking = isSpeaking,
                    onSaveClick = onSaveClick,
                    isSaved = isSaved,
                    onShareClick = onShareClick
                )
            }
        }
    }
}

@Composable
private fun InStreamHandoffChips(
    message: String,
    onOpenInStudio: ((String) -> Unit)?,
    onCreateChecklist: ((String) -> Unit)?,
    onAskQuiz: ((String) -> Unit)?,
    onTranslate: ((String) -> Unit)?
) {
    if (onOpenInStudio == null && onCreateChecklist == null && onAskQuiz == null && onTranslate == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (onOpenInStudio != null) {
            HandoffChip(
                icon = Icons.Default.EditNote,
                label = "Open in Studio",
                onClick = { onOpenInStudio(message) }
            )
        }
        if (onCreateChecklist != null) {
            HandoffChip(
                icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                label = "Create Checklist",
                onClick = { onCreateChecklist(message) }
            )
        }
        if (onAskQuiz != null) {
            HandoffChip(
                icon = Icons.Default.Lightbulb,
                label = "Test Knowledge",
                onClick = { onAskQuiz(message) }
            )
        }
        if (onTranslate != null) {
            HandoffChip(
                icon = Icons.Default.Translate,
                label = "Translate",
                onClick = { onTranslate(message) }
            )
        }
    }
}

@Composable
private fun HandoffChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OpenFieldPrimaryBlue,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldNavyText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun MessageActions(
    message: String,
    timestamp: String?,
    onSpeakClick: (() -> Unit)?,
    isSpeaking: Boolean,
    onSaveClick: (() -> Unit)?,
    isSaved: Boolean,
    onShareClick: (() -> Unit)?
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ActionIcon(
            icon = Icons.Default.ContentCopy,
            description = "Copy response",
            onClick = {
                clipboard.setText(AnnotatedString(Markdown.toPlainText(message)))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }
        )
        if (onSaveClick != null) {
            ActionIcon(
                icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                description = if (isSaved) "Saved" else "Save answer",
                onClick = onSaveClick,
                active = isSaved
            )
        }
        if (onSpeakClick != null) {
            ActionIcon(
                icon = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                description = if (isSpeaking) "Stop reading" else "Read aloud",
                onClick = onSpeakClick,
                active = isSpeaking
            )
        }
        if (onShareClick != null) {
            ActionIcon(
                icon = Icons.Default.Share,
                description = "Share",
                onClick = onShareClick
            )
        }
        if (!timestamp.isNullOrBlank()) {
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = OpenFieldMutedText),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun ActionIcon(icon: ImageVector, description: String, onClick: () -> Unit, active: Boolean = false) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (active) OpenFieldPrimaryBlue else OpenFieldSecondaryText.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AttachmentImage(imageUri: Any, isVisionAnalysis: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OpenFieldSecondaryBackground)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Attached image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        if (isVisionAnalysis) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OpenFieldPrimaryBlue.copy(alpha = 0.88f),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Vision Grounded",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/** Renders AI-generated markdown as formatted blocks. Kept for existing call sites. */
@Composable
fun MarkdownText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    MarkdownContent(text = text, style = style, modifier = modifier)
}

@Composable
fun StreamingCursor(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    Box(
        modifier = modifier
            .size(width = 8.dp, height = 15.dp)
            .clip(RoundedCornerShape(2.dp))
            .alpha(alpha)
            .background(OpenFieldPrimaryBlue)
    )
}

@Composable
private fun ModeResultBadge(mode: String) {
    val (icon, title, subtitle) = when (mode.uppercase()) {
        "IMAGE" -> Triple(Icons.Default.CameraAlt, "VISION ANALYSIS", "Gemma 4 Vision")
        "SOLVE", "TROUBLESHOOT" -> Triple(Icons.Default.Build, "DIAGNOSTIC WORKFLOW", "Step-by-Step")
        "EXPLAIN", "LEARN" -> Triple(Icons.Default.Lightbulb, "INTERACTIVE LESSON", "Tutor")
        "WRITE" -> Triple(Icons.Default.EditNote, "WRITING STUDIO", "Draft")
        "DOCUMENT" -> Triple(Icons.Default.Description, "DOCUMENT GROUNDED", "Evidence-Based")
        "SUMMARIZE" -> Triple(Icons.Default.Description, "EXECUTIVE SUMMARY", "Key Takeaways")
        "TRANSLATE" -> Triple(Icons.Default.Translate, "TRANSLATION", "100% Offline")
        else -> Triple(Icons.Default.AutoAwesome, mode, "CRUX AI")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = OpenFieldPrimaryBlueLight,
        border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OpenFieldPrimaryBlue,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldPrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OpenFieldPrimaryBlue.copy(alpha = 0.5f))
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldNavyText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun TroubleshootResultCard(
    text: String,
    isComplete: Boolean,
    onFollowUpAction: ((String) -> Unit)?
) {
    val hasHazardWarning = text.contains("hazard", ignoreCase = true) ||
            text.contains("safety", ignoreCase = true) ||
            text.contains("warning", ignoreCase = true) ||
            text.contains("caution", ignoreCase = true)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (hasHazardWarning) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Safety Alert",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Safety Precaution: Ensure proper safety measures before inspecting components.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }

        MarkdownContent(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        if (!isComplete) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreamingCursor()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Analyzing diagnostic step...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = OpenFieldPrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else if (onFollowUpAction != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "FEEDBACK OBSERVATION:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldSecondaryText,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ObservationActionButton(
                    icon = Icons.Default.CheckCircle,
                    label = "Fixed / Worked",
                    tint = Color(0xFF2E7D32),
                    onClick = { onFollowUpAction("It worked! The issue is resolved.") }
                )
                ObservationActionButton(
                    icon = Icons.Default.Cancel,
                    label = "Still Broken / Next Step",
                    tint = Color(0xFFC62828),
                    onClick = { onFollowUpAction("I completed this check, but the issue remains. What should I inspect next?") }
                )
                ObservationActionButton(
                    icon = Icons.Default.HelpOutline,
                    label = "Need Simpler Step",
                    tint = OpenFieldPrimaryBlue,
                    onClick = { onFollowUpAction("Can you explain this step more simply or provide a safer alternative check?") }
                )
            }
        }
    }
}

@Composable
private fun ObservationActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldNavyText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun TutorLessonResultCard(
    text: String,
    isComplete: Boolean,
    onFollowUpAction: ((String) -> Unit)?
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val quizBlock = remember(text) { parseLessonQuiz(text) }

    Column(modifier = Modifier.fillMaxWidth()) {
        MarkdownContent(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        if (!isComplete) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreamingCursor()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Preparing lesson blocks...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = OpenFieldPrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            if (quizBlock != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OpenFieldSecondaryBackground,
                    border = BorderStroke(1.dp, OpenFieldCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = OpenFieldPrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "KNOWLEDGE CHECK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = OpenFieldPrimaryBlue,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quizBlock.question,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        quizBlock.options.forEach { (letter, optText) ->
                            val isSelected = selectedOption == letter
                            val isCorrect = letter.equals(quizBlock.correctLetter, ignoreCase = true)
                            val containerColor = when {
                                isSelected && isCorrect -> Color(0xFFE8F5E9)
                                isSelected && !isCorrect -> Color(0xFFFFEBEE)
                                else -> OpenFieldBackground
                            }
                            val borderColor = when {
                                isSelected && isCorrect -> Color(0xFF2E7D32)
                                isSelected && !isCorrect -> Color(0xFFC62828)
                                else -> OpenFieldCardBorder
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = containerColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedOption = letter }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$letter)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) OpenFieldNavyText else OpenFieldSecondaryText
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OpenFieldNavyText,
                                            fontSize = 12.5.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val isCorrect = selectedOption.equals(quizBlock.correctLetter, ignoreCase = true)
                            Text(
                                text = if (isCorrect) "✅ Correct! ${quizBlock.why}" else "❌ Incorrect. Correct answer is ${quizBlock.correctLetter}. ${quizBlock.why}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            if (onFollowUpAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HandoffChip(
                        icon = Icons.Default.Lightbulb,
                        label = "Explain More Simply",
                        onClick = { onFollowUpAction("Can you explain this concept more simply with an everyday analogy?") }
                    )
                    HandoffChip(
                        icon = Icons.Default.AutoAwesome,
                        label = "Another Example",
                        onClick = { onFollowUpAction("Can you give me another practical, real-world example of this?") }
                    )
                    HandoffChip(
                        icon = Icons.Default.HelpOutline,
                        label = "Give Practice Question",
                        onClick = { onFollowUpAction("Give me a practice quiz question to test my understanding.") }
                    )
                    HandoffChip(
                        icon = Icons.Default.Description,
                        label = "Summarize",
                        onClick = { onFollowUpAction("Summarize the key takeaway of this lesson in 2 bullet points.") }
                    )
                }
            }
        }
    }
}

@Composable
private fun WritingStudioResultCard(
    text: String,
    isComplete: Boolean,
    onSaveClick: (() -> Unit)?,
    onCopyClick: () -> Unit,
    onFollowUpAction: ((String) -> Unit)?
) {
    var isRawMode by remember { mutableStateOf(false) }
    val wordCount = remember(text) { text.split(Regex("\\s+")).count { it.isNotBlank() } }
    val readTime = (wordCount / 200).coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OpenFieldBackground,
            border = BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OpenFieldPrimaryBlue.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(12.dp))
                            Text(
                                text = "$wordCount words • ~$readTime min read",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OpenFieldSecondaryBackground)
                            .padding(2.dp)
                    ) {
                        Surface(
                            onClick = { isRawMode = false },
                            shape = RoundedCornerShape(4.dp),
                            color = if (!isRawMode) OpenFieldBackground else Color.Transparent
                        ) {
                            Text(
                                text = "Preview",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (!isRawMode) OpenFieldPrimaryBlue else OpenFieldSecondaryText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Surface(
                            onClick = { isRawMode = true },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isRawMode) OpenFieldBackground else Color.Transparent
                        ) {
                            Text(
                                text = "Raw Text",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isRawMode) OpenFieldPrimaryBlue else OpenFieldSecondaryText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isRawMode) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = OpenFieldNavyText
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    MarkdownContent(text = text, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (!isComplete) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreamingCursor()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Drafting document in Studio...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = OpenFieldPrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HandoffChip(icon = Icons.Default.ContentCopy, label = "Copy Draft", onClick = onCopyClick)
                if (onSaveClick != null) {
                    HandoffChip(icon = Icons.Default.Bookmark, label = "Save to Library", onClick = onSaveClick)
                }
                if (onFollowUpAction != null) {
                    HandoffChip(
                        icon = Icons.Default.AutoAwesome,
                        label = "Polish & Flow",
                        onClick = { onFollowUpAction("Polish this draft for better flow, tone, and conciseness.") }
                    )
                    HandoffChip(
                        icon = Icons.Default.EditNote,
                        label = "Shorten 30%",
                        onClick = { onFollowUpAction("Shorten this draft by 30% while retaining all essential points.") }
                    )
                    HandoffChip(
                        icon = Icons.Default.Description,
                        label = "Add Outline",
                        onClick = { onFollowUpAction("Add a structured outline and summary bullets to this draft.") }
                    )
                }
            }
        }
    }
}

@Composable
private fun VisionResultCard(
    text: String,
    isComplete: Boolean,
    onFollowUpAction: ((String) -> Unit)?
) {
    val hasCaution = text.contains("uncertain", ignoreCase = true) ||
            text.contains("caveat", ignoreCase = true) ||
            text.contains("limitation", ignoreCase = true) ||
            text.contains("safety", ignoreCase = true) ||
            text.contains("edib", ignoreCase = true) ||
            text.contains("toxic", ignoreCase = true)

    Column(modifier = Modifier.fillMaxWidth()) {
        MarkdownContent(text = text, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))

        if (hasCaution) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    Text(
                        text = "Visual Identification Advisory: Confirm critical identifications independently.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }

        if (!isComplete) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreamingCursor()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Analyzing visual features...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = OpenFieldPrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else if (onFollowUpAction != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HandoffChip(
                    icon = Icons.Default.Build,
                    label = "Troubleshoot this issue",
                    onClick = { onFollowUpAction("Can you guide me through troubleshooting and fixing the issue shown in this image step by step?") }
                )
                HandoffChip(
                    icon = Icons.Default.Lightbulb,
                    label = "Explain how it works",
                    onClick = { onFollowUpAction("Explain the working mechanism and components visible in this image.") }
                )
            }
        }
    }
}

private data class ParsedQuiz(
    val question: String,
    val options: List<Pair<String, String>>,
    val correctLetter: String,
    val why: String
)

private fun parseLessonQuiz(text: String): ParsedQuiz? {
    if (!text.contains("QUIZ", ignoreCase = true)) return null
    val lines = text.lines().map { it.trim() }
    val quizLineIdx = lines.indexOfFirst { it.startsWith("QUIZ:", ignoreCase = true) || it.contains("QUIZ:", ignoreCase = true) }
    if (quizLineIdx == -1) return null

    val question = lines[quizLineIdx].substringAfter("QUIZ:").trim().ifBlank {
        lines.getOrNull(quizLineIdx + 1) ?: "Test your understanding"
    }

    val options = mutableListOf<Pair<String, String>>()
    var answerLetter = "A"
    var whyText = ""

    for (i in (quizLineIdx + 1) until lines.size) {
        val line = lines[i]
        when {
            line.matches(Regex("^[A-D]\\)\\s*.+")) -> {
                val letter = line.take(1)
                val optText = line.substring(2).trim()
                options.add(letter to optText)
            }
            line.startsWith("ANSWER:", ignoreCase = true) -> {
                answerLetter = line.substringAfter("ANSWER:").trim().take(1).uppercase()
            }
            line.startsWith("WHY:", ignoreCase = true) -> {
                whyText = line.substringAfter("WHY:").trim()
            }
        }
    }

    if (options.isEmpty()) return null
    return ParsedQuiz(question, options, answerLetter, whyText)
}
