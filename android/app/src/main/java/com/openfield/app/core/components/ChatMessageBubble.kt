package com.openfield.app.core.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.theme.BubbleShapeAssistant
import com.openfield.app.core.theme.BubbleShapeUser
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
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
    onSourceClick: (() -> Unit)? = null,
    onSpeakClick: (() -> Unit)? = null,
    isSpeaking: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            OpenFieldLogo(
                size = 28.dp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // AI messages take remaining width; user messages capped at 82% via widthIn
        val columnModifier = if (!isUser) {
            Modifier.weight(1f)
        } else {
            Modifier.widthIn(max = 320.dp)
        }

        Column(
            modifier = columnModifier,
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = if (isUser) BubbleShapeUser else BubbleShapeAssistant,
                color = if (isUser) OpenFieldPrimaryBlueLight else OpenFieldBackground,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(
                    1.dp, OpenFieldCardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Attached image
                    if (imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OpenFieldSecondaryBackground)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Message attachment",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    when {
                        // Streaming: AI message still arriving — show typing dots
                        !isUser && !isComplete && message.isEmpty() -> {
                            TypingIndicator()
                        }
                        // Message text present
                        message.isNotEmpty() -> {
                            if (isUser) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            } else {
                                MarkdownText(
                                    text = message,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            }
                        }
                    }

                    // Numbered Steps if present
                    if (steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Try these steps in order:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        steps.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(OpenFieldPrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${step.number}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = OpenFieldNavyText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Source reference (Document Q&A)
                    if (sourcePage != null && onSourceClick != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SourceReferenceCard(
                            pageLabel = sourcePage,
                            onClick = onSourceClick
                        )
                    }
                }
            }

            if (isComplete && (timestamp != null || (!isUser && message.isNotEmpty()))) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (timestamp != null) {
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = OpenFieldSecondaryText.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    if (!isUser && message.isNotEmpty()) {
                        val clipboard = LocalClipboardManager.current
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onSpeakClick != null) {
                                IconButton(
                                    onClick = onSpeakClick,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isSpeaking) "Stop speaking" else "Read aloud",
                                        tint = if (isSpeaking) OpenFieldPrimaryBlue else OpenFieldSecondaryText.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(message)) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy response",
                                    tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typingAlpha"
    )
    Text(
        text = "● ● ●",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = OpenFieldSecondaryText.copy(alpha = alpha),
            letterSpacing = 2.sp,
            fontSize = 10.sp
        )
    )
}

@Composable
fun MarkdownText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val annotated = remember(text) { buildMarkdownAnnotated(text, style) }
    Text(text = annotated, style = style, modifier = modifier)
}

fun buildMarkdownAnnotated(text: String, style: TextStyle) = buildAnnotatedString {
    val lines = text.split('\n')
    lines.forEachIndexed { idx, rawLine ->
        val trimmed = rawLine.trim()
        when {
            trimmed.startsWith("### ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                    appendInlineBold(trimmed.removePrefix("### "))
                }
            }
            trimmed.startsWith("## ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                    appendInlineBold(trimmed.removePrefix("## "))
                }
            }
            trimmed.startsWith("# ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                    appendInlineBold(trimmed.removePrefix("# "))
                }
            }
            trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
                val content = if (trimmed.startsWith("- ")) trimmed.removePrefix("- ") else trimmed.removePrefix("• ")
                append("  • ")
                appendInlineBold(content)
            }
            trimmed.startsWith("* ") && !trimmed.startsWith("**") -> {
                append("  • ")
                appendInlineBold(trimmed.removePrefix("* "))
            }
            trimmed.matches(Regex("^\\d+\\. .*")) -> {
                val dotIdx = trimmed.indexOf(". ")
                if (dotIdx > 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append("  ${trimmed.substring(0, dotIdx + 2)}")
                    }
                    appendInlineBold(trimmed.substring(dotIdx + 2))
                } else {
                    appendInlineBold(rawLine)
                }
            }
            trimmed.startsWith("> ") -> {
                withStyle(SpanStyle(color = Color(0xFF68758A), fontStyle = FontStyle.Italic)) {
                    appendInlineBold(trimmed.removePrefix("> "))
                }
            }
            trimmed.startsWith("**") && trimmed.endsWith("**") && trimmed.length > 4 -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(trimmed.removeSurrounding("**"))
                }
            }
            else -> appendInlineBold(rawLine)
        }
        if (idx < lines.size - 1) append('\n')
    }
}

fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlineBold(text: String) {
    // Split on ** markers — odd-indexed segments are bold
    val parts = text.split("**")
    parts.forEachIndexed { idx, part ->
        if (idx % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
        } else {
            // Handle *italic* inside non-bold segments
            val italicParts = part.split("*")
            italicParts.forEachIndexed { iIdx, iPart ->
                if (iIdx % 2 == 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(iPart) }
                } else {
                    append(iPart)
                }
            }
        }
    }
}
