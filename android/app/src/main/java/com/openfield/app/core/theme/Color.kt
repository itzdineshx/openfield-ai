package com.openfield.app.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// CRUX Semantic Constants (Universal Across Themes)
// ============================================================================
val OpenFieldPrimaryBlue = Color(0xFF1769E8)
val OpenFieldPrimaryBlueDark = Color(0xFF1255C0)

val OpenFieldError = Color(0xFFD94A4A)
val OpenFieldErrorBg = Color(0xFFFDECEC)
val OpenFieldSuccess = Color(0xFF239653)
val OpenFieldSuccessBg = Color(0xFFEAF7ED)
val OpenFieldWarning = Color(0xFFD18A22)
val OpenFieldWarningBg = Color(0xFFFEF7ED)

// Decorative accent colors for library/tool categories
val OpenFieldTeal = Color(0xFF0D9488)
val OpenFieldPurple = Color(0xFF7C3AED)
val OpenFieldIndigo = Color(0xFF4F46E5)
val OpenFieldAmber = Color(0xFFD97706)
val OpenFieldEmerald = Color(0xFF059669)

// ============================================================================
// CRUX Color Palette Specification: Light vs Obsidian AMOLED
// ============================================================================
data class CruxPalette(
    val isDark: Boolean,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val primaryBorder: Color,
    val background: Color,
    val secondaryBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val divider: Color,
    val navyText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val offlineGreenBg: Color,
    val offlineGreenText: Color
)

val LightCruxPalette = CruxPalette(
    isDark = false,
    primary = Color(0xFF1769E8),
    primaryDark = Color(0xFF1255C0),
    primaryLight = Color(0xFFEEF5FF),
    primaryBorder = Color(0xFFD4E5FF),
    background = Color(0xFFFFFFFF),
    secondaryBackground = Color(0xFFF7F9FC),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE5EAF2),
    divider = Color(0xFFEDF2F7),
    navyText = Color(0xFF101B36),
    secondaryText = Color(0xFF68758A),
    mutedText = Color(0xFF94A3B8),
    offlineGreenBg = Color(0xFFEAF7ED),
    offlineGreenText = Color(0xFF1F8A4C)
)

val ObsidianAmoledPalette = CruxPalette(
    isDark = true,
    primary = Color(0xFF38BDF8), // Electric sky / sapphire blue
    primaryDark = Color(0xFF0284C7),
    primaryLight = Color(0xFF0C223E), // Midnight blue container
    primaryBorder = Color(0xFF1E3A8A),
    background = Color(0xFF000000), // 100% Pure Pitch Black for AMOLED battery saving
    secondaryBackground = Color(0xFF0A0F1D), // Obsidian background
    cardBackground = Color(0xFF0D1220), // Obsidian card surface
    cardBorder = Color(0xFF1C263A), // Dark border
    divider = Color(0xFF182236),
    navyText = Color(0xFFF8FAFC), // Crisp light text
    secondaryText = Color(0xFF94A3B8), // Readable slate
    mutedText = Color(0xFF64748B),
    offlineGreenBg = Color(0xFF064E3B),
    offlineGreenText = Color(0xFF34D399)
)

val LocalCruxPalette = staticCompositionLocalOf { LightCruxPalette }

// ============================================================================
// Dynamic Composable Color Accessors (Seamless Theme Reactivity)
// ============================================================================
val OpenFieldPrimaryBlueLight: Color
    @Composable get() = LocalCruxPalette.current.primaryLight

val OpenFieldPrimaryBlueBorder: Color
    @Composable get() = LocalCruxPalette.current.primaryBorder

val OpenFieldBackground: Color
    @Composable get() = LocalCruxPalette.current.background

val OpenFieldSecondaryBackground: Color
    @Composable get() = LocalCruxPalette.current.secondaryBackground

val OpenFieldCardBackground: Color
    @Composable get() = LocalCruxPalette.current.cardBackground

val OpenFieldCardBorder: Color
    @Composable get() = LocalCruxPalette.current.cardBorder

val OpenFieldDivider: Color
    @Composable get() = LocalCruxPalette.current.divider

val OpenFieldNavyText: Color
    @Composable get() = LocalCruxPalette.current.navyText

val OpenFieldSecondaryText: Color
    @Composable get() = LocalCruxPalette.current.secondaryText

val OpenFieldMutedText: Color
    @Composable get() = LocalCruxPalette.current.mutedText

val OpenFieldOfflineGreenBg: Color
    @Composable get() = LocalCruxPalette.current.offlineGreenBg

val OpenFieldOfflineGreenText: Color
    @Composable get() = LocalCruxPalette.current.offlineGreenText
