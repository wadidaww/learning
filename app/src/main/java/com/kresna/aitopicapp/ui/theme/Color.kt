package com.kresna.aitopicapp.ui.theme

import androidx.compose.ui.graphics.Color

// Calm night (dark): soft charcoal instead of pure black and off-white text, to cut glare and
// the "halo" effect of bright text on black.
val Night0 = Color(0xFF121318)
val Night1 = Color(0xFF1A1B22)
val Night2 = Color(0xFF23242D)
val Night3 = Color(0xFF2E2F3A)
val NightOutline = Color(0xFF4A4B58)
val NightText = Color(0xFFE6E4EF)
val NightTextMuted = Color(0xFFA9A7B8)

// Day (light): warm paper instead of stark white.
val Day0 = Color(0xFFF7F5F0)
val Day1 = Color(0xFFFFFDF9)
val Day2 = Color(0xFFEFECE5)
val Day3 = Color(0xFFE4E0D7)
val DayOutline = Color(0xFFB9B4A8)
val DayText = Color(0xFF2B2A33)
val DayTextMuted = Color(0xFF5F5D6B)

/**
 * Accent and feedback colours for one theme. Dark uses soft pastels (bright saturated colours
 * vibrate on dark backgrounds); light uses deeper tones that keep 4.5:1 contrast on paper.
 */
data class AppPalette(
    val isDark: Boolean,
    val violet: Color,
    val pink: Color,
    val blue: Color,
    val cyan: Color,
    val teal: Color,
    val sky: Color,
    val amber: Color,
    val orange: Color,
    val success: Color,
    val error: Color,
    val saved: Color
)

val NightPalette = AppPalette(
    isDark = true,
    violet = Color(0xFFB4A2F8),
    pink = Color(0xFFF2A7C9),
    blue = Color(0xFF8DB4F7),
    cyan = Color(0xFF86D8E3),
    teal = Color(0xFF7FD1C0),
    sky = Color(0xFF9CCDF0),
    amber = Color(0xFFF2CF8B),
    orange = Color(0xFFF4B183),
    success = Color(0xFF7ED6A1),
    error = Color(0xFFF08A9B),
    saved = Color(0xFFEDCB6B)
)

val DayPalette = AppPalette(
    isDark = false,
    violet = Color(0xFF6D55C9),
    pink = Color(0xFFB9497F),
    blue = Color(0xFF3567C2),
    cyan = Color(0xFF1F8796),
    teal = Color(0xFF21806F),
    sky = Color(0xFF2F79A8),
    amber = Color(0xFF9A6A12),
    orange = Color(0xFFB5602A),
    success = Color(0xFF2E8B57),
    error = Color(0xFFC2415A),
    saved = Color(0xFFA57A00)
)
