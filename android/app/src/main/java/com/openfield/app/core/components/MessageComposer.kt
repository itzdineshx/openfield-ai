package com.openfield.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.ComposerShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldMutedText
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText

/**
 * Large Home Dashboard composer card matching the 5th screen in the reference.
 */
@Composable
fun HomePromptComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ask anything..."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Action shortcut buttons: Image, Camera, Mic, Document
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComposerActionButton(
                    icon = Icons.Default.Image,
                    contentDescription = "Attach image",
                    onClick = onPickImage
                )
                ComposerActionButton(
                    icon = Icons.Default.CameraAlt,
                    contentDescription = "Take photo",
                    onClick = onCameraClick
                )
                ComposerActionButton(
                    icon = Icons.Default.Mic,
                    contentDescription = "Voice input",
                    onClick = onVoiceClick
                )
                ComposerActionButton(
                    icon = Icons.Default.Description,
                    contentDescription = "Attach document",
                    onClick = onPickDocument
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Input field and send button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OpenFieldSecondaryBackground)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = OpenFieldNavyText,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(OpenFieldPrimaryBlue),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = OpenFieldMutedText,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (value.isNotBlank()) {
                            onSend(value)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (value.isNotBlank()) OpenFieldPrimaryBlue else OpenFieldPrimaryBlue.copy(alpha = 0.4f)),
                    enabled = value.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send prompt",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComposerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = OpenFieldSecondaryBackground,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = OpenFieldSecondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Fixed bottom message composer used across Chat, Image Analysis, Document Q&A, Learn & Explain.
 */
@Composable
fun ChatBottomComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onAttach: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ask a follow-up...",
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = OpenFieldBackground,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth()
                .clip(ComposerShape)
                .background(OpenFieldSecondaryBackground)
                .border(1.dp, OpenFieldCardBorder, ComposerShape)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment '+' button
            IconButton(
                onClick = onAttach,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach",
                    tint = OpenFieldSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text input
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = OpenFieldNavyText,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(OpenFieldPrimaryBlue),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OpenFieldMutedText,
                                fontSize = 14.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )

            // Voice input button
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice input",
                    tint = OpenFieldSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Send button
            if (value.isNotBlank()) {
                IconButton(
                    onClick = {
                        if (value.isNotBlank() && enabled) {
                            onSend(value)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OpenFieldPrimaryBlue),
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

