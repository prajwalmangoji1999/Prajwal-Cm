package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Aesthetic ambient background with soft pastel light aura
 * creating a modern, calming, premium aesthetic look.
 */
@Composable
fun AestheticBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val lightColors = listOf(
        Color(0xFFF6F8FF), // Soft icy pearl
        Color(0xFFEEF2FF), // Pale lilac blue
        Color(0xFFF5F3FF), // Soft lavender
        Color(0xFFF0FDF4)  // Whispering mint
    )

    val darkColors = listOf(
        Color(0xFF090D16), // Deep obsidian
        Color(0xFF0F172A), // Dark slate
        Color(0xFF1E1B4B), // Midnight violet
        Color(0xFF0D1B2A)  // Deep cosmic teal
    )

    val baseGradient = if (isDark) {
        Brush.verticalGradient(
            colors = darkColors,
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    } else {
        Brush.linearGradient(
            colors = lightColors,
            start = Offset(0f, 0f),
            end = Offset(1000f, 1800f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
    ) {
        // Floating aesthetic glow blooms
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (isDark) {
                // Violet bloom top right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x356366F1), Color(0x006366F1)),
                        center = Offset(w * 0.85f, h * 0.15f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.65f
                )

                // Emerald teal bloom bottom left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2806B6D4), Color(0x0006B6D4)),
                        center = Offset(w * 0.15f, h * 0.75f),
                        radius = w * 0.7f
                    ),
                    center = Offset(w * 0.15f, h * 0.75f),
                    radius = w * 0.7f
                )
            } else {
                // Soft indigo lavender aura at top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x22818CF8), Color(0x00818CF8)),
                        center = Offset(w * 0.85f, h * 0.12f),
                        radius = w * 0.75f
                    ),
                    center = Offset(w * 0.85f, h * 0.12f),
                    radius = w * 0.75f
                )

                // Soft peach rose aura at bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1CF43F5E), Color(0x00F43F5E)),
                        center = Offset(w * 0.85f, h * 0.85f),
                        radius = w * 0.6f
                    ),
                    center = Offset(w * 0.85f, h * 0.85f),
                    radius = w * 0.6f
                )

                // Soft pastel mint at mid-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2234D399), Color(0x0034D399)),
                        center = Offset(w * 0.1f, h * 0.5f),
                        radius = w * 0.6f
                    ),
                    center = Offset(w * 0.1f, h * 0.5f),
                    radius = w * 0.6f
                )
            }
        }

        content()
    }
}
