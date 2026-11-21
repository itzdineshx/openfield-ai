package com.openfield.app.core.components

import com.openfield.app.core.text.SyntaxHighlighter

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.StartOffset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.openfield.app.core.text.CalloutKind
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.text.MdBlock
import com.openfield.app.core.text.MdListItem
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldDivider
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.core.theme.OpenFieldSuccessBg
import com.openfield.app.core.theme.OpenFieldWarning
import com.openfield.app.core.theme.OpenFieldWarningBg

@Composable
fun defaultMarkdownStyle(): TextStyle = MaterialTheme.typography.bodyMedium.copy(
    fontSize = 14.5.sp,
    lineHeight = 22.sp,
    color = OpenFieldNavyText
)

@Composable
fun MarkdownContent(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = defaultMarkdownStyle(),
    accent: Color = OpenFieldPrimaryBlue
) {
    val blocks = remember(text) { Markdown.parse(text) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MdBlock.Heading -> MdHeading(block, style, isFirst = index == 0)
                is MdBlock.Paragraph -> InlineMarkdown(block.text, style)
                is MdBlock.BulletList -> MdList(block.items, numbered = false, style = style, accent = accent)
                is MdBlock.NumberedList -> MdList(block.items, numbered = true, style = style, accent = accent)
                is MdBlock.Quote -> MdQuote(block.text, style, accent)
                is MdBlock.Code -> MdCode(block.text)
                is MdBlock.Table -> MdTable(block, style)
                is MdBlock.Callout -> MdCallout(block, style)
                MdBlock.Rule -> HorizontalDivider(color = OpenFieldDivider, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

/** Single-paragraph inline markdown (bold/italic/code) — for options, labels and short fields. */
@Composable
fun InlineMarkdown(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    val codeBg = OpenFieldSecondaryBackground
    val codeClr = OpenFieldPrimaryBlue
    val annotated = remember(text, style, codeBg, codeClr) { buildInlineAnnotated(text, codeBg, codeClr) }
    Text(text = annotated, style = style, modifier = modifier, maxLines = maxLines)
}

fun buildInlineAnnotated(
    text: String,
    codeBackground: Color = Color.Unspecified,
    codeColor: Color = Color.Unspecified
): AnnotatedString = buildAnnotatedString {
    Markdown.parseInline(text).forEach { span ->
        val spanStyle = SpanStyle(
            fontWeight = if (span.bold) FontWeight.Bold else null,
            fontStyle = if (span.italic) FontStyle.Italic else null,
            fontFamily = if (span.code) FontFamily.Monospace else null,
            background = if (span.code) codeBackground else Color.Unspecified,
            color = if (span.code) codeColor else Color.Unspecified,
            textDecoration = if (span.strike) TextDecoration.LineThrough else null
        )
        withStyle(spanStyle) { append(span.text) }
    }
}

@Composable
private fun MdHeading(block: MdBlock.Heading, base: TextStyle, isFirst: Boolean) {
    val headingStyle = when (block.level) {
        1 -> base.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
        2 -> base.copy(fontSize = 16.5.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
        3 -> base.copy(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
        else -> base.copy(fontWeight = FontWeight.SemiBold)
    }
    InlineMarkdown(
        text = block.text,
        style = headingStyle,
        modifier = if (isFirst) Modifier else Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun lineHeightDp(style: TextStyle): Dp {
    val density = LocalDensity.current
    return if (style.lineHeight.isSpecified) with(density) { style.lineHeight.toDp() } else 20.dp
}

@Composable
private fun MdList(items: List<MdListItem>, numbered: Boolean, style: TextStyle, accent: Color) {
    val lineHeight = lineHeightDp(style)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (item.depth * 22).dp),
                verticalAlignment = Alignment.Top
            ) {
                if (numbered && item.depth == 0) {
                    val circle = 20.dp
                    Box(
                        modifier = Modifier
                            .padding(top = ((lineHeight - circle) / 2).coerceAtLeast(0.dp))
                            .size(circle)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.number.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    val dot = if (item.depth == 0) 6.dp else 5.dp
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp, top = ((lineHeight - dot) / 2).coerceAtLeast(0.dp))
                            .size(dot)
                            .clip(CircleShape)
                            .background(if (item.depth == 0) OpenFieldNavyText else OpenFieldSecondaryText)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                InlineMarkdown(text = item.text, style = style, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MdQuote(text: String, style: TextStyle, accent: Color) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(accent.copy(alpha = 0.35f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        InlineMarkdown(
            text = text,
            style = style.copy(color = OpenFieldSecondaryText, fontStyle = FontStyle.Italic)
        )
    }
}

@Composable
private fun MdCode(code: String) {
    if (SvgParser.isSvg(code)) {
        SvgDiagramCard(rawCode = code)
        return
    }
    if (MermaidParser.isMermaidCode(code)) {
        MermaidDiagramCard(rawCode = code)
        return
    }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    val lines = remember(code) { code.lines() }
    val highlightedCode = remember(code) { SyntaxHighlighter.highlight(code) }
    val detectedLanguage = remember(code) {
        val trimmed = code.trim().lowercase()
        when {
            trimmed.startsWith("{") && trimmed.endsWith("}") -> "JSON"
            trimmed.contains("fun ") || trimmed.contains("val ") || trimmed.contains("var ") -> "KOTLIN"
            trimmed.contains("def ") || trimmed.contains("import ") && trimmed.contains(":") -> "PYTHON"
            trimmed.contains("select ") && trimmed.contains("from ") -> "SQL"
            trimmed.startsWith("#!/bin/") || trimmed.contains("sudo ") || trimmed.contains("apt ") -> "BASH"
            else -> "CODE"
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = detectedLanguage,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            clipboard.setText(AnnotatedString(code))
                            copied = true
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (copied) "Copied" else "Copy",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (copied) Color(0xFF10B981) else Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // Code Content with Line Numbers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Row {
                    // Line numbers
                    Column(modifier = Modifier.padding(end = 12.dp)) {
                        lines.indices.forEach { idx ->
                            Text(
                                text = "${idx + 1}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                    // Highlighted Code text
                    Text(
                        text = highlightedCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MdTable(table: MdBlock.Table, style: TextStyle) {
    val cellWidth = 136.dp
    val columns = maxOf(table.header.size, table.rows.maxOfOrNull { it.size } ?: 0)
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row(modifier = Modifier.background(OpenFieldSecondaryBackground)) {
                for (c in 0 until columns) {
                    InlineMarkdown(
                        text = table.header.getOrElse(c) { "" },
                        style = style.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
                        modifier = Modifier.width(cellWidth).padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
            table.rows.forEach { row ->
                HorizontalDivider(color = OpenFieldDivider, modifier = Modifier.width(cellWidth * columns))
                Row {
                    for (c in 0 until columns) {
                        InlineMarkdown(
                            text = row.getOrElse(c) { "" },
                            style = style.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            modifier = Modifier.width(cellWidth).padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MdCallout(block: MdBlock.Callout, style: TextStyle) {
    val (bg, fg, icon) = when (block.kind) {
        CalloutKind.WARNING -> Triple(OpenFieldWarningBg, OpenFieldWarning, Icons.Default.WarningAmber)
        CalloutKind.TIP -> Triple(OpenFieldSuccessBg, OpenFieldSuccess, Icons.Default.Lightbulb)
        CalloutKind.INFO -> Triple(OpenFieldPrimaryBlueLight, OpenFieldPrimaryBlue, Icons.Default.Info)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, if (block.kind == CalloutKind.INFO) OpenFieldPrimaryBlueBorder else fg.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            InlineMarkdown(
                text = "**${block.label}:** ${block.text}",
                style = style.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Animated three-dot indicator shown while the on-device model is producing its first tokens. */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(OpenFieldPrimaryBlue)
            )
        }
        if (label != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldSecondaryText,
                    fontSize = 12.5.sp
                )
            )
        }
    }
}
