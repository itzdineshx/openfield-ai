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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
    onTranslate: ((String) -> Unit)? = null
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
            onTranslate = onTranslate
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
    onTranslate: ((String) -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        OpenFieldLogo(size = 28.dp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (imageUri != null) {
                AttachmentImage(imageUri, isVisionAnalysis = true)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (message.isBlank() && !isComplete) {
                TypingIndicator(label = pendingLabel)
            } else if (message.isNotBlank()) {
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
                InStreamHandoffChips(
                    message = message,
                    onOpenInStudio = onOpenInStudio,
                    onCreateChecklist = onCreateChecklist,
                    onAskQuiz = onAskQuiz,
                    onTranslate = onTranslate
                )

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
