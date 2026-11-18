package com.openfield.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueDark

/**
 * OpenField Logo: Mountain-inspired symbol inside a blue rounded squircle.
 */
@Composable
fun OpenFieldLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showText: Boolean = false,
    textFontSize: Int = 20
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.28f))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OpenFieldPrimaryBlue,
                            OpenFieldPrimaryBlueDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size * 0.72f)) {
                val w = this.size.width
                val h = this.size.height

                // Sun / sky subtle highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = w * 0.16f,
                    center = Offset(w * 0.72f, h * 0.28f)
                )

                // Background Mountain Peak (taller, central)
                val peakPath = Path().apply {
                    moveTo(w * 0.18f, h * 0.88f)
                    lineTo(w * 0.50f, h * 0.22f)
                    lineTo(w * 0.82f, h * 0.88f)
                    close()
                }
                drawPath(peakPath, color = Color.White.copy(alpha = 0.95f))

                // Left Mountain Peak
                val leftPeak = Path().apply {
                    moveTo(w * 0.05f, h * 0.88f)
                    lineTo(w * 0.32f, h * 0.44f)
                    lineTo(w * 0.60f, h * 0.88f)
                    close()
                }
                drawPath(leftPeak, color = Color.White.copy(alpha = 0.82f))

                // Snowcap highlight on center mountain
                val snowCap = Path().apply {
                    moveTo(w * 0.50f, h * 0.22f)
                    lineTo(w * 0.40f, h * 0.42f)
                    lineTo(w * 0.46f, h * 0.40f)
                    lineTo(w * 0.50f, h * 0.45f)
                    lineTo(w * 0.55f, h * 0.39f)
                    lineTo(w * 0.60f, h * 0.42f)
                    close()
                }
                drawPath(snowCap, color = Color.White)

                // Forefront gentle green hill / field base (representing "OpenField")
                val hillPath = Path().apply {
                    moveTo(0f, h * 0.88f)
                    cubicTo(
                        w * 0.25f, h * 0.74f,
                        w * 0.70f, h * 0.76f,
                        w, h * 0.85f
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(hillPath, color = Color(0xFF34D399)) // Vibrant field green
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "OpenField",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = textFontSize.sp,
                    color = OpenFieldNavyText
                )
            )
        }
    }
}

