package com.openfield.app.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightCruxPalette.primary,
    onPrimary = LightCruxPalette.background,
    primaryContainer = LightCruxPalette.primaryLight,
    onPrimaryContainer = LightCruxPalette.primaryDark,
    secondary = LightCruxPalette.secondaryText,
    onSecondary = LightCruxPalette.background,
    secondaryContainer = LightCruxPalette.secondaryBackground,
    onSecondaryContainer = LightCruxPalette.navyText,
    tertiary = LightCruxPalette.offlineGreenText,
    onTertiary = LightCruxPalette.background,
    tertiaryContainer = LightCruxPalette.offlineGreenBg,
    onTertiaryContainer = LightCruxPalette.offlineGreenText,
    background = LightCruxPalette.background,
    onBackground = LightCruxPalette.navyText,
    surface = LightCruxPalette.cardBackground,
    onSurface = LightCruxPalette.navyText,
    surfaceVariant = LightCruxPalette.secondaryBackground,
    onSurfaceVariant = LightCruxPalette.secondaryText,
    outline = LightCruxPalette.cardBorder,
    outlineVariant = LightCruxPalette.divider,
    error = LightCruxPalette.error,
    onError = LightCruxPalette.background,
    errorContainer = LightCruxPalette.errorBg,
    onErrorContainer = LightCruxPalette.error
)

private val DarkColorScheme = darkColorScheme(
    primary = ObsidianAmoledPalette.primary,
    onPrimary = ObsidianAmoledPalette.background,
    primaryContainer = ObsidianAmoledPalette.primaryLight,
    onPrimaryContainer = ObsidianAmoledPalette.primary,
    secondary = ObsidianAmoledPalette.secondaryText,
    onSecondary = ObsidianAmoledPalette.background,
    secondaryContainer = ObsidianAmoledPalette.secondaryBackground,
    onSecondaryContainer = ObsidianAmoledPalette.navyText,
    tertiary = ObsidianAmoledPalette.offlineGreenText,
    onTertiary = ObsidianAmoledPalette.background,
    tertiaryContainer = ObsidianAmoledPalette.offlineGreenBg,
    onTertiaryContainer = ObsidianAmoledPalette.offlineGreenText,
    background = ObsidianAmoledPalette.background,
    onBackground = ObsidianAmoledPalette.navyText,
    surface = ObsidianAmoledPalette.cardBackground,
    onSurface = ObsidianAmoledPalette.navyText,
    surfaceVariant = ObsidianAmoledPalette.secondaryBackground,
    onSurfaceVariant = ObsidianAmoledPalette.secondaryText,
    outline = ObsidianAmoledPalette.cardBorder,
    outlineVariant = ObsidianAmoledPalette.divider,
    error = ObsidianAmoledPalette.error,
    onError = ObsidianAmoledPalette.background,
    errorContainer = ObsidianAmoledPalette.errorBg,
    onErrorContainer = ObsidianAmoledPalette.error
)

@Composable
fun OpenFieldTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) ObsidianAmoledPalette else LightCruxPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = palette.background.toArgb()
                window.navigationBarColor = palette.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalCruxPalette provides palette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OpenFieldTypography,
            shapes = OpenFieldShapes,
            content = content
        )
    }
}
