package com.cachescope.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF001A6B),
    primaryContainer = Color(0xFF003399),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFF80CBC4),
    background = Color(0xFF111318),
    surface = Color(0xFF1B1F27),
    surfaceVariant = Color(0xFF232830),
    onBackground = Color(0xFFE2E2E9),
    onSurface = Color(0xFFE2E2E9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A4FC0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A6B),
    secondary = Color(0xFF006B5F),
    background = Color(0xFFF8F9FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F3FB),
    onBackground = Color(0xFF1A1B21),
    onSurface = Color(0xFF1A1B21)
)

@Composable
fun CacheScopeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
