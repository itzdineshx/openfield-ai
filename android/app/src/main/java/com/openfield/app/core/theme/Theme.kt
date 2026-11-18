package com.openfield.app.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OpenFieldPrimaryBlue,
    onPrimary = OpenFieldBackground,
    primaryContainer = OpenFieldPrimaryBlueLight,
    onPrimaryContainer = OpenFieldPrimaryBlueDark,
    secondary = OpenFieldSecondaryText,
    onSecondary = OpenFieldBackground,
    secondaryContainer = OpenFieldSecondaryBackground,
    onSecondaryContainer = OpenFieldNavyText,
    tertiary = OpenFieldOfflineGreenText,
    onTertiary = OpenFieldBackground,
    tertiaryContainer = OpenFieldOfflineGreenBg,
    onTertiaryContainer = OpenFieldOfflineGreenText,
    background = OpenFieldBackground,
    onBackground = OpenFieldNavyText,
    surface = OpenFieldBackground,
    onSurface = OpenFieldNavyText,
    surfaceVariant = OpenFieldSecondaryBackground,
    onSurfaceVariant = OpenFieldSecondaryText,
    outline = OpenFieldCardBorder,
    outlineVariant = OpenFieldDivider,
    error = OpenFieldError,
    onError = OpenFieldBackground,
    errorContainer = OpenFieldErrorBg,
    onErrorContainer = OpenFieldError
)

@Composable
fun OpenFieldTheme(
    darkTheme: Boolean = false, // Forced light theme per design requirements
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = OpenFieldBackground.toArgb()
                window.navigationBarColor = OpenFieldBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OpenFieldTypography,
        shapes = OpenFieldShapes,
        content = content
    )
}

