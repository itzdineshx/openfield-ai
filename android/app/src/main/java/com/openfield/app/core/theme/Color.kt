package com.openfield.app.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// CRUX Semantic Constants (Universal Across Themes)
// ============================================================================
val OpenFieldPrimaryBlue = Color(0xFF1769E8)
val OpenFieldPrimaryBlueDark = Color(0xFF1255C0)

// Decorative accent colors for library/tool categories
val OpenFieldTeal = Color(0xFF0D9488)
val OpenFieldPurple = Color(0xFF7C3AED)
val OpenFieldIndigo = Color(0xFF4F46E5)
val OpenFieldAmber = Color(0xFFD97706)
val OpenFieldEmerald = Color(0xFF059669)

// Default static fallbacks
val StaticError = Color(0xFFD94A4A)
val StaticErrorBg = Color(0xFFFDECEC)
val StaticSuccess = Color(0xFF239653)
val StaticSuccessBg = Color(0xFFEAF7ED)
val StaticWarning = Color(0xFFD18A22)
val StaticWarningBg = Color(0xFFFEF7ED)

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
    val offlineGreenText: Color,
    val userBubbleBg: Color,
    val userBubbleText: Color,
    val warning: Color = StaticWarning,
    val warningBg: Color = StaticWarningBg,
    val warningBorder: Color = Color(0xFFFDE68A),
    val warningText: Color = Color(0xFF92400E),
    val warningIcon: Color = Color(0xFFD97706),
    val error: Color = StaticError,
    val errorBg: Color = StaticErrorBg,
    val errorBorder: Color = Color(0xFFFECACA),
    val errorText: Color = Color(0xFF991B1B),
    val success: Color = StaticSuccess,
    val successBg: Color = StaticSuccessBg,
    val successBorder: Color = Color(0xFFA5D6A7),
    val successText: Color = Color(0xFF1B5E20)
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
    offlineGreenText = Color(0xFF1F8A4C),
    userBubbleBg = Color(0xFFEEF5FF),
    userBubbleText = Color(0xFF101B36),
    warning = Color(0xFFD18A22),
    warningBg = Color(0xFFFEF7ED),
    warningBorder = Color(0xFFFDE68A),
    warningText = Color(0xFF92400E),
    warningIcon = Color(0xFFD97706),
    error = Color(0xFFD94A4A),
    errorBg = Color(0xFFFDECEC),
    errorBorder = Color(0xFFFECACA),
    errorText = Color(0xFF991B1B),
    success = Color(0xFF239653),
    successBg = Color(0xFFEAF7ED),
    successBorder = Color(0xFFA5D6A7),
    successText = Color(0xFF1B5E20)
)

val ObsidianAmoledPalette = CruxPalette(
    isDark = true,
    primary = Color(0xFF38BDF8), // Electric sky / sapphire blue
    primaryDark = Color(0xFF0284C7),
    primaryLight = Color(0xFF1E293B), // Slate 800 container
    primaryBorder = Color(0xFF334155), // Slate 700 border
    background = Color(0xFF0E0E10), // ChatGPT deep neutral charcoal/black
    secondaryBackground = Color(0xFF18181B), // Zinc 900
    cardBackground = Color(0xFF202024), // Zinc 850
    cardBorder = Color(0xFF2E2E33), // Zinc 800
    divider = Color(0xFF27272A),
    navyText = Color(0xFFF4F4F5), // Crisp Zinc 100
    secondaryText = Color(0xFFA1A1AA), // Zinc 400
    mutedText = Color(0xFF71717A), // Zinc 500
    offlineGreenBg = Color(0xFF092918),
    offlineGreenText = Color(0xFF34D399),
    userBubbleBg = Color(0xFF27272A), // Elevated neutral dark charcoal (ChatGPT mobile style)
    userBubbleText = Color(0xFFF4F4F5),
    warning = Color(0xFFFBBF24),
    warningBg = Color(0xFF2B1D08),
    warningBorder = Color(0xFF6B3E08),
    warningText = Color(0xFFFDE68A),
    warningIcon = Color(0xFFFBBF24),
    error = Color(0xFFF87171),
    errorBg = Color(0xFF2D1214),
    errorBorder = Color(0xFF7F1D1D),
    errorText = Color(0xFFFCA5A5),
    success = Color(0xFF34D399),
    successBg = Color(0xFF092918),
    successBorder = Color(0xFF065F46),
    successText = Color(0xFF6EE7B7)
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

val OpenFieldUserBubbleBg: Color
    @Composable get() = LocalCruxPalette.current.userBubbleBg

val OpenFieldUserBubbleText: Color
    @Composable get() = LocalCruxPalette.current.userBubbleText

val OpenFieldOfflineGreenBg: Color
    @Composable get() = LocalCruxPalette.current.offlineGreenBg

val OpenFieldOfflineGreenText: Color
    @Composable get() = LocalCruxPalette.current.offlineGreenText

val OpenFieldWarning: Color
    @Composable get() = LocalCruxPalette.current.warning

val OpenFieldWarningBg: Color
    @Composable get() = LocalCruxPalette.current.warningBg

val OpenFieldWarningBorder: Color
    @Composable get() = LocalCruxPalette.current.warningBorder

val OpenFieldWarningText: Color
    @Composable get() = LocalCruxPalette.current.warningText

val OpenFieldWarningIcon: Color
    @Composable get() = LocalCruxPalette.current.warningIcon

val OpenFieldError: Color
    @Composable get() = LocalCruxPalette.current.error

val OpenFieldErrorBg: Color
    @Composable get() = LocalCruxPalette.current.errorBg

val OpenFieldErrorBorder: Color
    @Composable get() = LocalCruxPalette.current.errorBorder

val OpenFieldErrorText: Color
    @Composable get() = LocalCruxPalette.current.errorText

val OpenFieldSuccess: Color
    @Composable get() = LocalCruxPalette.current.success

val OpenFieldSuccessBg: Color
    @Composable get() = LocalCruxPalette.current.successBg

val OpenFieldSuccessBorder: Color
    @Composable get() = LocalCruxPalette.current.successBorder

val OpenFieldSuccessText: Color
    @Composable get() = LocalCruxPalette.current.successText

