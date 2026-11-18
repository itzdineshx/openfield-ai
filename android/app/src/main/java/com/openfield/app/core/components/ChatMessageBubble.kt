package com.openfield.app.core.components
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    onSourceClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            OpenFieldLogo(
                size = 28.dp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = if (isUser) BubbleShapeUser else BubbleShapeAssistant,
                color = if (isUser) OpenFieldPrimaryBlueLight else OpenFieldBackground,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Attached image (real user attachment only)
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

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = OpenFieldNavyText
                            )
                        )
                    }

                    // Numbered Steps if present (e.g. "Try these steps:")
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

                    // Source Reference Card if present (e.g. in Document Q&A)
                    if (sourcePage != null && onSourceClick != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SourceReferenceCard(
                            pageLabel = sourcePage,
                            onClick = onSourceClick
                        )
                    }
                }
            }

            if (timestamp != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = OpenFieldSecondaryText.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
