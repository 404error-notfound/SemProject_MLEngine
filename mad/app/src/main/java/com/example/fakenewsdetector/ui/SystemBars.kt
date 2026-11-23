package com.example.fakenewsdetector.ui

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

/** Light icons on dark surfaces and vice versa, following the app theme. */
@Composable
fun SystemBarIcons(window: Window, dark: Boolean) {
    SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}
