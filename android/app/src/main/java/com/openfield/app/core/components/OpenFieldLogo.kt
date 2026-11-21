package com.openfield.app.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.R
import com.openfield.app.core.theme.LocalCruxPalette
import com.openfield.app.core.theme.OpenFieldNavyText

/**
 * CRUX Official Brand Logo:
 * Pure, high-resolution PNG emblem with transparent background and seamless
 * light/dark mode adaptation. No background box, clean and uncluttered.
 */
@Composable
fun CruxLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showText: Boolean = false,
    textFontSize: Int = 20,
    showSubtitle: Boolean = false // Kept for backwards compatibility, subtitle omitted for clean look
) {
    val isDark = LocalCruxPalette.current.isDark

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // In light mode, crop bottom ~12% to mask any dark artifact in the PNG
        Box(modifier = Modifier.size(size)) {
            Box(
                modifier = Modifier
                    .size(width = size, height = if (!isDark) size * 0.88f else size)
                    .clip(RectangleShape),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(
                        id = if (isDark) R.drawable.app_logo_dark else R.drawable.app_logo_light
                    ),
                    contentDescription = "CRUX Logo",
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.TopCenter
                )
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "CRUX",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = textFontSize.sp,
                    letterSpacing = 1.2.sp,
                    color = OpenFieldNavyText
                )
            )
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
        textFontSize = textFontSize
    )
}
