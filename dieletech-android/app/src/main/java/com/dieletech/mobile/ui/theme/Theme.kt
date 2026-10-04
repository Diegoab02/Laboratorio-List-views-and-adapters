package com.dieletech.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryBlue = Color(0xFF2563EB)
private val PrimaryDark = Color(0xFF1E40AF)
private val AccentGreen = Color(0xFF10B981)
private val BackgroundLight = Color(0xFFF9FAFB)
private val TextDark = Color(0xFF1F2937)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    secondary = AccentGreen,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = AccentGreen,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937)
)

@Composable
fun DielTechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
