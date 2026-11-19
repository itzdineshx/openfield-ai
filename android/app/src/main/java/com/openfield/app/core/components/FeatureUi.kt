package com.openfield.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldErrorBg
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess

/** Card that introduces a feature, e.g. "Guided Troubleshooting — Let me help you step by step." */
@Composable
fun FeatureHeaderCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OpenFieldPrimaryBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = OpenFieldNavyText
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        color = OpenFieldSecondaryText
                    )
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.5.sp,
            color = OpenFieldNavyText
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestionChips(
    suggestions: List<String>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { suggestion ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = OpenFieldBackground,
                border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(enabled = enabled) { onClick(suggestion) }
            ) {
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) OpenFieldPrimaryBlue else OpenFieldSecondaryText
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/** Selectable pill (level, format, tone, filter). */
@Composable
fun ChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) OpenFieldPrimaryBlue else OpenFieldBackground,
        border = BorderStroke(1.dp, if (selected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) Color.White else OpenFieldNavyText
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

enum class OptionState { DEFAULT, SELECTED, CORRECT, WRONG }

/** Radio-style answer row used by troubleshooting steps and quizzes. */
@Composable
fun AnswerOptionRow(
    text: String,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val (border, bg, mark) = when (state) {
        OptionState.SELECTED -> Triple(OpenFieldPrimaryBlue, OpenFieldPrimaryBlueLight, OpenFieldPrimaryBlue)
        OptionState.CORRECT -> Triple(OpenFieldSuccess, OpenFieldSuccess.copy(alpha = 0.08f), OpenFieldSuccess)
        OptionState.WRONG -> Triple(OpenFieldError, OpenFieldErrorBg, OpenFieldError)
        OptionState.DEFAULT -> Triple(OpenFieldCardBorder, OpenFieldBackground, OpenFieldCardBorder)
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(if (state == OptionState.DEFAULT) 1.dp else 1.5.dp, border),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .then(
                        if (state == OptionState.DEFAULT) Modifier.border(1.5.dp, OpenFieldSecondaryText.copy(alpha = 0.5f), CircleShape)
                        else Modifier.background(mark)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state != OptionState.DEFAULT) {
                    Icon(
                        imageVector = if (state == OptionState.WRONG) Icons.Default.ErrorOutline else Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            InlineMarkdown(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                    color = OpenFieldNavyText,
                    fontWeight = if (state == OptionState.DEFAULT) FontWeight.Normal else FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Inline progress card shown while the on-device model works, with an optional Stop action. */
@Composable
fun GeneratingCard(
    message: String,
    modifier: Modifier = Modifier,
    detail: String = "Running on your device — this can take a little while.",
    onStop: (() -> Unit)? = null
) {
    Surface(
        shape = CardShape,
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = OpenFieldPrimaryBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = OpenFieldNavyText
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText)
            )
            if (onStop != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onStop,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = OpenFieldNavyText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stop", color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun InlineErrorCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String = "Try again",
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    Surface(
        shape = CardShape,
        color = OpenFieldErrorBg,
        border = BorderStroke(1.dp, OpenFieldError.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = OpenFieldError, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(color = OpenFieldError, fontWeight = FontWeight.SemiBold)
                )
                if (onDismiss != null) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss)
                            .padding(6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            InlineMarkdown(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp, color = OpenFieldNavyText)
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OpenFieldError.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = OpenFieldError, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(retryLabel, color = OpenFieldError, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
