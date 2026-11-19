package com.openfield.app.feature.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.InlineMarkdown
import com.openfield.app.core.components.MarkdownContent
import com.openfield.app.core.components.TypingIndicator
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldAmber
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldDivider
import com.openfield.app.core.theme.OpenFieldEmerald
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldErrorBg
import com.openfield.app.core.theme.OpenFieldIndigo
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldPurple
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldTeal
import com.openfield.app.core.theme.OpenFieldWarning
import com.openfield.app.core.theme.OpenFieldWarningBg
import com.openfield.app.feature.ai.controller.TutorNote
import com.openfield.app.feature.ai.controller.TutorNoteKind
import com.openfield.app.feature.ai.controller.TutorNoteStatus
import com.openfield.app.feature.ai.mode.LessonPart
import com.openfield.app.feature.ai.mode.LessonSection
import com.openfield.app.feature.ai.mode.LessonTerm

// Generative-UI blocks for Learn & Explain. Every block renders model output only; a block is
// shown only when the model actually produced that kind of content for the topic.

private val partAccents = listOf(OpenFieldPrimaryBlue, OpenFieldTeal, OpenFieldPurple, OpenFieldAmber, OpenFieldIndigo, OpenFieldEmerald)

@Composable
fun LessonBlockCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = OpenFieldBackground,
    borderColor: Color = OpenFieldCardBorder,
    accent: Color = OpenFieldPrimaryBlue,
    content: @Composable () -> Unit
) {
    Surface(
        shape = CardShape,
        color = container,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { heading() }) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OpenFieldNavyText)
                )
            }
            content()
        }
    }
}

/** "In short" — the model's key facts as a scannable checklist. */
@Composable
fun KeyPointsBlock(points: List<String>) {
    LessonBlockCard(title = "In short", icon = Icons.Default.AutoAwesome, container = OpenFieldSecondaryBackground) {
        points.forEach { point ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = OpenFieldPrimaryBlue,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                InlineMarkdown(
                    text = point,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp, lineHeight = 21.sp, color = OpenFieldNavyText)
                )
            }
        }
    }
}

/** "How it works" — a vertical process diagram drawn from the model's FLOW steps. */
@Composable
fun FlowDiagramBlock(steps: List<String>) {
    LessonBlockCard(title = "How it works", icon = Icons.Default.AccountTree) {
        Column {
            steps.forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Step ${index + 1} of ${steps.size}: ${Markdown.toPlainText(step)}"
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(OpenFieldPrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OpenFieldPrimaryBlueLight,
                        border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        InlineMarkdown(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, color = OpenFieldNavyText),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
                if (index < steps.lastIndex) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue.copy(alpha = 0.55f),
                        modifier = Modifier
                            .padding(start = 5.dp, top = 3.dp, bottom = 3.dp)
                            .size(18.dp)
                    )
                }
            }
        }
    }
}

/** "Main parts" — a labelled component map built from the model's PART lines. */
@Composable
fun PartsDiagramBlock(topic: String, parts: List<LessonPart>) {
    LessonBlockCard(title = "Main parts", icon = Icons.Default.Widgets) {
        Text(
            text = Markdown.stripInline(topic),
            style = MaterialTheme.typography.labelMedium.copy(color = OpenFieldSecondaryText, fontWeight = FontWeight.Medium)
        )
        parts.withIndex().chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                row.forEach { (index, part) ->
                    val accent = partAccents[index % partAccents.size]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accent.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = part.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            }
                            if (part.role.isNotBlank()) {
                                InlineMarkdown(
                                    text = part.role,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp, color = OpenFieldSecondaryText)
                                )
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ConceptBlock(index: Int, section: LessonSection) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { heading() }) {
                Surface(shape = RoundedCornerShape(8.dp), color = OpenFieldPrimaryBlueLight) {
                    Text(
                        text = "Concept ${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = section.heading,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = OpenFieldNavyText)
            )
            MarkdownContent(text = section.content)
        }
    }
}

@Composable
fun ExampleBlock(text: String) {
    LessonBlockCard(
        title = "Everyday example",
        icon = Icons.Default.Lightbulb,
        container = OpenFieldPrimaryBlueLight,
        borderColor = OpenFieldPrimaryBlueBorder
    ) {
        MarkdownContent(text = text)
    }
}

@Composable
fun TermsBlock(terms: List<LessonTerm>) {
    LessonBlockCard(title = "Key terms", icon = Icons.Default.Translate) {
        terms.forEachIndexed { index, term ->
            if (index > 0) HorizontalDivider(color = OpenFieldDivider)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = term.term,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldPrimaryBlue)
                )
                if (term.meaning.isNotBlank()) {
                    InlineMarkdown(
                        text = term.meaning,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, color = OpenFieldNavyText)
                    )
                }
            }
        }
    }
}

/** Shows which block the on-device model is currently writing while a lesson streams. */
@Composable
fun LiveWritingIndicator(label: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TypingIndicator()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontWeight = FontWeight.Medium)
            )
        }
    }
}

/** Top-of-lesson status: live generation, or a clear "incomplete" notice after a stop/failure. */
@Composable
fun LessonStatusBanner(
    isStreaming: Boolean,
    stopReason: String?,
    onStop: () -> Unit,
    onRegenerate: () -> Unit
) {
    if (isStreaming) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = OpenFieldPrimaryBlueLight,
            border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = OpenFieldPrimaryBlue, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Writing live on your device",
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onStop) { Text("Stop", color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold) }
            }
        }
    } else if (stopReason != null) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = OpenFieldWarningBg,
            border = BorderStroke(1.dp, OpenFieldWarning.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 4.dp)) {
                Text(
                    text = "Incomplete lesson",
                    style = MaterialTheme.typography.titleSmall.copy(color = OpenFieldNavyText, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$stopReason It won't be saved.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.5.sp)
                )
                TextButton(onClick = onRegenerate) { Text("Regenerate lesson", color = OpenFieldPrimaryBlue, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

/** A tutor response (follow-up, simpler explanation, example, summary, feedback) rendered as a lesson card. */
@Composable
fun TutorNoteBlock(note: TutorNote) {
    val icon = when (note.kind) {
        TutorNoteKind.QUESTION -> Icons.AutoMirrored.Filled.HelpOutline
        TutorNoteKind.SIMPLER -> Icons.Default.Psychology
        TutorNoteKind.EXAMPLE -> Icons.Default.Lightbulb
        TutorNoteKind.SUMMARY -> Icons.Default.Summarize
        TutorNoteKind.FEEDBACK -> Icons.Default.EditNote
    }
    LessonBlockCard(title = note.kind.label, icon = icon) {
        if (!note.prompt.isNullOrBlank()) {
            Surface(shape = RoundedCornerShape(10.dp), color = OpenFieldSecondaryBackground, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = note.prompt,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp, color = OpenFieldSecondaryText),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
        when {
            note.text.isNotBlank() -> MarkdownContent(text = note.text)
            note.status == TutorNoteStatus.STREAMING -> TypingIndicator()
        }
        when (note.status) {
            TutorNoteStatus.STOPPED -> Text(
                text = if (note.text.isBlank()) "Stopped before any answer was written." else "Stopped — this answer is incomplete.",
                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldWarning, fontWeight = FontWeight.Medium)
            )
            TutorNoteStatus.FAILED -> Surface(shape = RoundedCornerShape(10.dp), color = OpenFieldErrorBg, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = OpenFieldError, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System message: ${note.systemMessage ?: "Something went wrong."}",
                        style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldNavyText)
                    )
                }
            }
            else -> Unit
        }
    }
}
