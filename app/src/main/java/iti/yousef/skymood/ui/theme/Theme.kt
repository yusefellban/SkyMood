package iti.yousef.skymood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Dark color scheme using the SkyMood weather palette */
private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueDark,
    secondary = SunsetOrange,
    onSecondary = Color.White,
    background = NightDark,
    onBackground = Color.White,
    surface = NightMedium,
    onSurface = Color.White,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary
)

/** Light color scheme using the SkyMood weather palette */
private val LightColorScheme = lightColorScheme(
    primary = SkyBlueDark,
    onPrimary = Color.White,
    primaryContainer = SkyBlueLight,
    secondary = SunsetOrange,
    onSecondary = Color.White,
    background = SurfaceLight,
    onBackground = TextOnLight,
    surface = CardLight,
    onSurface = TextOnLight,
    surfaceVariant = CloudWhite,
    onSurfaceVariant = StormGray
)

/**
 * SkyMood app theme. Uses dark-mode-first design with the weather palette.
 * Disables dynamic color to maintain brand consistency.
 * Sets transparent status bar for edge-to-edge layouts.
 */
@Composable
fun SkyMoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Make the status bar transparent for edge-to-edge weather animations
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}