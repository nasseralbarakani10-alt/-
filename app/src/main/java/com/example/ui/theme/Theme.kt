package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = BlueDark,
    secondary = BlueDark,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = InputTextPrimary,
    surface = SurfaceLight,
    onSurface = InputTextPrimary,
    onSurfaceVariant = LabelUnfocused, // dark gray (#616161), never light gray
    outline = BorderUnfocused,         // visible gray (#9E9E9E), never faded
    outlineVariant = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF64B5F6),
    onSecondary = Color.Black,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF94A3B8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
