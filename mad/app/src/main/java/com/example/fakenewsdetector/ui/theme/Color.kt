package com.example.fakenewsdetector.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The only file in the app allowed to name raw colours.
 * Every composable must read tokens from [androidx.compose.material3.MaterialTheme]
 * or [VerdictColors] instead.
 */
private object Palette {
    // Shared
    val White = Color(0xFFFFFFFF)

    // Light surfaces
    val LightBackground = Color(0xFFFDF7FF)
    val LightContainer = Color(0xFFF3EDF7)
    val LightOnSurface = Color(0xFF1D1B20)
    val LightOnSurfaceVariant = Color(0xFF49454F)
    val LightOutline = Color(0xFF79747E)
    val LightOutlineVariant = Color(0xFFCAC4D0)
    val LightPrimary = Color(0xFF6750A4)
    val LightSecondary = Color(0xFF625B71)
    val LightSecondaryContainer = Color(0xFFE8DEF8)
    val LightPrimaryContainer = Color(0xFFEADDFF)

    // Dark surfaces
    val DarkBackground = Color(0xFF141218)
    val DarkContainer = Color(0xFF211F26)
    val DarkOnSurface = Color(0xFFE6E0E9)
    val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
    val DarkOutline = Color(0xFF938F99)
    val DarkOutlineVariant = Color(0xFF49454F)
    val DarkPrimary = Color(0xFFD0BCFF)
    val DarkOnPrimary = Color(0xFF381E72)
    val DarkSecondary = Color(0xFFCCC2DC)
    val DarkSecondaryContainer = Color(0xFF4A4458)

    // Verdict false
    val FalseContainerLight = Color(0xFFF9DEDC)
    val FalseOnContainerLight = Color(0xFF410E0B)
    val FalseAccentLight = Color(0xFF8C1D18)
    val FalseContainerDark = Color(0xFF601410)
    val FalseOnContainerDark = Color(0xFFF9DEDC)
    val FalseAccentDark = Color(0xFFF2B8B5)

    // Verdict unverified
    val UnverifiedContainerLight = Color(0xFFFFE8B3)
    val UnverifiedOnContainerLight = Color(0xFF3A2600)
    val UnverifiedAccentLight = Color(0xFF6B4500)
    val UnverifiedContainerDark = Color(0xFF4A3500)
    val UnverifiedOnContainerDark = Color(0xFFFFE8B3)
    val UnverifiedAccentDark = Color(0xFFF5C46A)

    // Verdict true
    val TrueContainerLight = Color(0xFFD8F0DF)
    val TrueOnContainerLight = Color(0xFF0F3D22)
    val TrueAccentLight = Color(0xFF14532D)
    val TrueContainerDark = Color(0xFF0F3D22)
    val TrueOnContainerDark = Color(0xFFD8F0DF)
    val TrueAccentDark = Color(0xFF8FD9A8)
}

val LightColors = lightColorScheme(
    primary = Palette.LightPrimary,
    onPrimary = Palette.White,
    primaryContainer = Palette.LightPrimaryContainer,
    onPrimaryContainer = Palette.LightOnSurface,
    secondary = Palette.LightSecondary,
    onSecondary = Palette.White,
    secondaryContainer = Palette.LightSecondaryContainer,
    onSecondaryContainer = Palette.LightOnSurface,
    background = Palette.LightBackground,
    onBackground = Palette.LightOnSurface,
    surface = Palette.LightBackground,
    onSurface = Palette.LightOnSurface,
    surfaceVariant = Palette.LightContainer,
    onSurfaceVariant = Palette.LightOnSurfaceVariant,
    surfaceContainer = Palette.LightContainer,
    surfaceContainerLow = Palette.LightBackground,
    surfaceContainerHigh = Palette.LightContainer,
    surfaceContainerHighest = Palette.LightContainer,
    outline = Palette.LightOutline,
    outlineVariant = Palette.LightOutlineVariant,
    error = Palette.FalseAccentLight,
    onError = Palette.White,
    errorContainer = Palette.FalseContainerLight,
    onErrorContainer = Palette.FalseOnContainerLight,
    scrim = Palette.LightOnSurface,
    inverseSurface = Palette.LightOnSurface,
    inverseOnSurface = Palette.LightBackground,
    surfaceTint = Palette.LightPrimary
)

val DarkColors = darkColorScheme(
    primary = Palette.DarkPrimary,
    onPrimary = Palette.DarkOnPrimary,
    primaryContainer = Palette.DarkSecondaryContainer,
    onPrimaryContainer = Palette.LightSecondaryContainer,
    secondary = Palette.DarkSecondary,
    onSecondary = Palette.DarkOnPrimary,
    secondaryContainer = Palette.DarkSecondaryContainer,
    onSecondaryContainer = Palette.LightSecondaryContainer,
    background = Palette.DarkBackground,
    onBackground = Palette.DarkOnSurface,
    surface = Palette.DarkBackground,
    onSurface = Palette.DarkOnSurface,
    surfaceVariant = Palette.DarkContainer,
    onSurfaceVariant = Palette.DarkOnSurfaceVariant,
    surfaceContainer = Palette.DarkContainer,
    surfaceContainerLow = Palette.DarkBackground,
    surfaceContainerHigh = Palette.DarkContainer,
    surfaceContainerHighest = Palette.DarkContainer,
    outline = Palette.DarkOutline,
    outlineVariant = Palette.DarkOutlineVariant,
    error = Palette.FalseAccentDark,
    onError = Palette.FalseContainerDark,
    errorContainer = Palette.FalseContainerDark,
    onErrorContainer = Palette.FalseOnContainerDark,
    scrim = Palette.DarkBackground,
    inverseSurface = Palette.DarkOnSurface,
    inverseOnSurface = Palette.DarkBackground,
    surfaceTint = Palette.DarkPrimary
)

/**
 * Verdict colours are not part of the Material scheme: a verdict has a
 * container, an on-container text colour and an accent used for its icon.
 */
@Immutable
data class VerdictColors(
    val falseContainer: Color,
    val onFalseContainer: Color,
    val falseAccent: Color,
    val unverifiedContainer: Color,
    val onUnverifiedContainer: Color,
    val unverifiedAccent: Color,
    val trueContainer: Color,
    val onTrueContainer: Color,
    val trueAccent: Color
)

val LightVerdictColors = VerdictColors(
    falseContainer = Palette.FalseContainerLight,
    onFalseContainer = Palette.FalseOnContainerLight,
    falseAccent = Palette.FalseAccentLight,
    unverifiedContainer = Palette.UnverifiedContainerLight,
    onUnverifiedContainer = Palette.UnverifiedOnContainerLight,
    unverifiedAccent = Palette.UnverifiedAccentLight,
    trueContainer = Palette.TrueContainerLight,
    onTrueContainer = Palette.TrueOnContainerLight,
    trueAccent = Palette.TrueAccentLight
)

val DarkVerdictColors = VerdictColors(
    falseContainer = Palette.FalseContainerDark,
    onFalseContainer = Palette.FalseOnContainerDark,
    falseAccent = Palette.FalseAccentDark,
    unverifiedContainer = Palette.UnverifiedContainerDark,
    onUnverifiedContainer = Palette.UnverifiedOnContainerDark,
    unverifiedAccent = Palette.UnverifiedAccentDark,
    trueContainer = Palette.TrueContainerDark,
    onTrueContainer = Palette.TrueOnContainerDark,
    trueAccent = Palette.TrueAccentDark
)

val LocalVerdictColors = staticCompositionLocalOf { LightVerdictColors }
