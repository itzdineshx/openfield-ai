package com.openfield.app.feature.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldMutedText
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldSecondaryText

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpenFieldBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Mountain Squircle Logo
            OpenFieldLogo(size = 88.dp)

            Spacer(modifier = Modifier.height(20.dp))

            // App Title
            Text(
                text = "CRUX",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Your offline AI companion\nfor everyday questions,\nlearning and problem solving.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = OpenFieldSecondaryText
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Landscape Hills Illustration in lower-middle area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Distant Misty Mountain Layers
                    val distantHills = Path().apply {
                        moveTo(0f, h * 0.70f)
                        cubicTo(w * 0.25f, h * 0.50f, w * 0.70f, h * 0.45f, w, h * 0.60f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(distantHills, color = Color(0xFFE2EAF4))

                    // Midground Soft Blue Hills
                    val midHills = Path().apply {
                        moveTo(0f, h * 0.78f)
                        cubicTo(w * 0.35f, h * 0.62f, w * 0.65f, h * 0.75f, w, h * 0.68f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(midHills, color = Color(0xFFC9DBEE))

                    // Foreground Rolling Green Meadow
                    val foregroundHills = Path().apply {
                        moveTo(0f, h * 0.88f)
                        cubicTo(w * 0.30f, h * 0.78f, w * 0.75f, h * 0.80f, w, h * 0.82f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(foregroundHills, color = Color(0xFF68C28A))

                    // Stylized Pine Trees on the ridge
                    val treeCoords = listOf(
                        Offset(w * 0.68f, h * 0.78f),
                        Offset(w * 0.73f, h * 0.77f),
                        Offset(w * 0.77f, h * 0.79f),
                        Offset(w * 0.82f, h * 0.80f),
                        Offset(w * 0.87f, h * 0.81f),
                        Offset(w * 0.92f, h * 0.82f)
                    )
                    treeCoords.forEach { pt ->
                        val tree = Path().apply {
                            moveTo(pt.x, pt.y - 20f)
                            lineTo(pt.x - 7f, pt.y)
                            lineTo(pt.x + 7f, pt.y)
                            close()
                        }
                        drawPath(tree, color = Color(0xFF388E3C))
                    }
                }
            }

            // Get Started Button
            PrimaryButton(
                text = "Get Started",
                onClick = onGetStarted,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dot Indicator (Active First Dot)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(OpenFieldPrimaryBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(OpenFieldMutedText.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Labels
            Text(
                text = "Offline · Private · Open Source",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = OpenFieldMutedText
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

