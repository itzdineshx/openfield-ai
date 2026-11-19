package com.openfield.app.core.components

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.theme.BubbleShapeUser
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
    isSpeaking: Boolean = false
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
            isSpeaking = isSpeaking
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
    isSpeaking: Boolean
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
                AttachmentImage(imageUri)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (message.isBlank() && !isComplete) {
                TypingIndicator(label = pendingLabel)
            } else if (message.isNotBlank()) {
                MarkdownContent(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp)
                )
            }

            if (steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                MarkdownContent(
                    text = "**Try these steps:**\n\n" + steps.joinToString("\n") { "${it.number}. ${it.text}" }
                )
            }

            if (sourcePage != null && onSourceClick != null && isComplete) {
                Spacer(modifier = Modifier.height(12.dp))
                SourceReferenceCard(pageLabel = sourcePage, onClick = onSourceClick)
            }

            if (isComplete && message.isNotBlank()) {
                MessageActions(
                    message = message,
                    timestamp = timestamp,
                    onSpeakClick = onSpeakClick,
                    isSpeaking = isSpeaking
                )
            }
        }
    }
}

@Composable
private fun MessageActions(
    message: String,
    timestamp: String?,
    onSpeakClick: (() -> Unit)?,
    isSpeaking: Boolean
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
        if (onSpeakClick != null) {
            ActionIcon(
                icon = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                description = if (isSpeaking) "Stop reading" else "Read aloud",
                onClick = onSpeakClick,
                active = isSpeaking
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
private fun AttachmentImage(imageUri: Any) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .height(170.dp)
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
