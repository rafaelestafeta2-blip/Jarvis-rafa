package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = MetallicObsidian,
    primaryContainer = DeepSpaceSurface,
    onPrimaryContainer = ArcCyan,
    secondary = StarkGold,
    onSecondary = MetallicObsidian,
    secondaryContainer = DeepSpaceSurface,
    onSecondaryContainer = StarkGold,
    background = MetallicObsidian,
    onBackground = TextPrimary,
    surface = DeepSpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = HolographicCardBackground,
    onSurfaceVariant = TextSecondary,
    outline = BorderCyan,
    error = JarvisRedAlert
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MetallicObsidian.toArgb()
            window.navigationBarColor = MetallicObsidian.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
