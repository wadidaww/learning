package com.kresna.aitopicapp.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B2470),
    onPrimaryContainer = VioletLight,
    secondary = Cyan,
    onSecondary = Ink950,
    secondaryContainer = Color(0xFF0E3A44),
    onSecondaryContainer = Color(0xFFB5F3FC),
    tertiary = Pink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4A1430),
    onTertiaryContainer = Color(0xFFFBCFE8),
    background = Ink950,
    onBackground = Ink100,
    surface = Ink900,
    onSurface = Ink100,
    surfaceVariant = Ink800,
    onSurfaceVariant = Ink300,
    surfaceContainerLowest = Ink950,
    surfaceContainerLow = Ink900,
    surfaceContainer = Ink850,
    surfaceContainerHigh = Ink800,
    surfaceContainerHighest = Ink700,
    outline = Ink500,
    outlineVariant = Ink700,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VioletDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE5FF),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF0E7490),
    tertiary = Color(0xFFBE185D),
    background = Paper,
    surface = PaperCard,
    surfaceVariant = Color(0xFFEEEDF5)
)

/**
 * The app is dark-first: the immersive feed is designed for a dark canvas, like short-form video apps.
 * Dynamic (wallpaper) colour is off by default so card-type colours stay recognisable.
 */
@Composable
fun AITopicLearningTheme(
    darkTheme: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
