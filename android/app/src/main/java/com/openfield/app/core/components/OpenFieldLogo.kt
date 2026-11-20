package com.openfield.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.OpenFieldNavyText

// Crux Official Brand Colors (Celestial Sapphire Blue & Sumi Obsidian)
val CruxBlue = Color(0xFF1E88E5)
val CruxBlueDark = Color(0xFF1565C0)
val CruxCyanGlow = Color(0xFF00E5FF)
val CruxIceBlue = Color(0xFF93C5FD)
val CruxObsidian = Color(0xFF111625)
val CruxObsidianDark = Color(0xFF0D1117)

/**
 * CRUX Official Brand Logo:
 * Celestial Sapphire Blue Enso Calligraphy Brush Ring with a Faceted 4-Point Celestial Crux Star.
 */
@Composable
fun CruxLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showText: Boolean = false,
    textFontSize: Int = 20,
    showSubtitle: Boolean = false
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
                            CruxObsidian,
                            CruxObsidianDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size * 0.76f)) {
                val w = this.size.width
                val h = this.size.height
                val cx = w / 2f
                val cy = h / 2f

                // 1. Outer Sapphire Blue Enso Calligraphy Ring
                drawArc(
                    color = CruxBlue,
                    startAngle = 40f,
                    sweepAngle = 290f,
                    useCenter = false,
                    topLeft = Offset(w * 0.10f, h * 0.10f),
                    size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.80f),
                    style = Stroke(width = w * 0.11f, cap = StrokeCap.Round)
                )

                // Cyan energy splash accent
                drawCircle(
                    color = CruxCyanGlow,
                    radius = w * 0.045f,
                    center = Offset(w * 0.78f, h * 0.25f)
                )

                // 2. Central 4-Point Faceted Crux Star
                val starRadius = w * 0.36f
                val innerRadius = w * 0.08f

                // Left Facets (Pure Brilliant White)
                val leftPath = Path().apply {
                    // North-West
                    moveTo(cx, cy - starRadius)
                    lineTo(cx, cy)
                    lineTo(cx - starRadius * 0.85f, cy)
                    lineTo(cx - innerRadius, cy - innerRadius)
                    close()

                    // South-West
                    moveTo(cx, cy + starRadius)
                    lineTo(cx, cy)
                    lineTo(cx - starRadius * 0.85f, cy)
                    lineTo(cx - innerRadius, cy + innerRadius)
                    close()
                }
                drawPath(leftPath, color = Color.White)

                // Right Facets (Metallic Ice Blue for 3D depth)
                val rightPath = Path().apply {
                    // North-East
                    moveTo(cx, cy - starRadius)
                    lineTo(cx, cy)
                    lineTo(cx + starRadius * 0.85f, cy)
                    lineTo(cx + innerRadius, cy - innerRadius)
                    close()

                    // South-East
                    moveTo(cx, cy + starRadius)
                    lineTo(cx, cy)
                    lineTo(cx + starRadius * 0.85f, cy)
                    lineTo(cx + innerRadius, cy + innerRadius)
                    close()
                }
                drawPath(rightPath, color = CruxIceBlue)

                // 3. Central Sapphire Star Core
                val corePath = Path().apply {
                    moveTo(cx, cy - innerRadius * 0.6f)
                    lineTo(cx + innerRadius * 0.6f, cy)
                    lineTo(cx, cy + innerRadius * 0.6f)
                    lineTo(cx - innerRadius * 0.6f, cy)
                    close()
                }
                drawPath(corePath, color = CruxBlue)
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CRUX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = textFontSize.sp,
                        letterSpacing = 1.5.sp,
                        color = OpenFieldNavyText
                    )
                )
                if (showSubtitle) {
                    Text(
                        text = "OFFLINE AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (textFontSize * 0.42f).sp,
                            letterSpacing = 2.sp,
                            color = CruxBlue
                        )
                    )
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for OpenFieldLogo -> CruxLogo.
 */
@Composable
fun OpenFieldLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showText: Boolean = false,
    textFontSize: Int = 20
) {
    CruxLogo(
        modifier = modifier,
        size = size,
        showText = showText,
        textFontSize = textFontSize,
        showSubtitle = true
    )
}
