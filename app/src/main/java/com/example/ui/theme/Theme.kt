package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF3730A3),
    background = BackgroundLight,
    onBackground = SlateDarkText,
    surface = SurfaceLight,
    onSurface = SlateDarkText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateMediumText,
    outline = BorderLight,
    error = RoseError,
    onError = Color.White
)

private val DarkColorScheme = lightColorScheme(
    // Retaining high readability and dark text on luminous surfaces
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = SlateDarkText,
    surface = SurfaceLight,
    onSurface = SlateDarkText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateMediumText,
    outline = BorderLight,
    error = RoseError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prevent washed out text colors
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
