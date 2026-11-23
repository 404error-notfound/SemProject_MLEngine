package com.example.fakenewsdetector.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** User preference for the app theme. System is the default. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Resolved brightness used by previews and system-bar styling. */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun FakeNewsDetectorTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = mode.isDark()
    CompositionLocalProvider(
        LocalVerdictColors provides if (dark) DarkVerdictColors else LightVerdictColors
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

/** Convenience for the many places that need a verdict token set. */
@Composable
@ReadOnlyComposable
fun verdictColors() = LocalVerdictColors.current
