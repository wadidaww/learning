package com.kresna.aitopicapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NightPalette.violet,
    onPrimary = Color(0xFF231A45),
    primaryContainer = Color(0xFF362C5E),
    onPrimaryContainer = Color(0xFFE3DBFF),
    secondary = NightPalette.teal,
    onSecondary = Color(0xFF0F2A25),
    secondaryContainer = Color(0xFF1F3B36),
    onSecondaryContainer = Color(0xFFC9EEE6),
    tertiary = NightPalette.pink,
    onTertiary = Color(0xFF3D1429),
    tertiaryContainer = Color(0xFF4A2638),
    onTertiaryContainer = Color(0xFFFADDEA),
    background = Night0,
    onBackground = NightText,
    surface = Night1,
    onSurface = NightText,
    surfaceVariant = Night2,
    onSurfaceVariant = NightTextMuted,
    surfaceContainerLowest = Night0,
    surfaceContainerLow = Night1,
    surfaceContainer = Night1,
    surfaceContainerHigh = Night2,
    surfaceContainerHighest = Night3,
    outline = NightOutline,
    outlineVariant = Night3,
    error = NightPalette.error,
    onError = Color(0xFF3B0D17),
    errorContainer = Color(0xFF4D2129),
    onErrorContainer = Color(0xFFFFD9DE)
)

private val LightColorScheme = lightColorScheme(
    primary = DayPalette.violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E0FA),
    onPrimaryContainer = Color(0xFF2A1A66),
    secondary = DayPalette.teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3EFE8),
    onSecondaryContainer = Color(0xFF0B3A31),
    tertiary = DayPalette.pink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF7DCE8),
    onTertiaryContainer = Color(0xFF4A1530),
    background = Day0,
    onBackground = DayText,
    surface = Day1,
    onSurface = DayText,
    surfaceVariant = Day2,
    onSurfaceVariant = DayTextMuted,
    surfaceContainerLowest = Day1,
    surfaceContainerLow = Day1,
    surfaceContainer = Day1,
    surfaceContainerHigh = Day2,
    surfaceContainerHighest = Day3,
    outline = DayOutline,
    outlineVariant = Day3,
    error = DayPalette.error,
    onError = Color.White
)

private val LocalAppPalette = staticCompositionLocalOf { NightPalette }

/** Theme-aware accent colours that Material's colour scheme has no slot for. */
object AppTheme {
    val palette: AppPalette
        @Composable get() = LocalAppPalette.current
}

/**
 * Follows the system light/dark setting. Both themes are tuned to be easy on the eyes: no pure
 * black or white, soft accents, low-glare gradients. Dynamic (wallpaper) colour is off by
 * default so card-type colours stay recognisable.
 */
@Composable
fun AITopicLearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalAppPalette provides if (darkTheme) NightPalette else DayPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
