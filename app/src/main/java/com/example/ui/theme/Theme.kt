package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VoltPrimary,
    onPrimary = VoltOnPrimary,
    primaryContainer = VoltPrimaryContainer,
    onPrimaryContainer = VoltOnPrimaryContainer,
    secondary = PulseSecondary,
    onSecondary = PulseOnSecondary,
    secondaryContainer = PulseSecondaryContainer,
    onSecondaryContainer = PulseOnSecondaryContainer,
    background = ObsidianBackground,
    onBackground = ObsidianOnBackground,
    surface = ObsidianSurface,
    onSurface = ObsidianOnSurface,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianOnSurfaceVariant,
    outline = ObsidianOutline,
    error = ObsidianError
)

private val LightColorScheme = lightColorScheme(
    primary = VoltPrimary,
    onPrimary = VoltOnPrimary,
    primaryContainer = VoltPrimaryContainer,
    onPrimaryContainer = VoltOnPrimaryContainer,
    secondary = PulseSecondary,
    onSecondary = PulseOnSecondary,
    secondaryContainer = PulseSecondaryContainer,
    onSecondaryContainer = PulseOnSecondaryContainer,
    background = ObsidianBackground,
    onBackground = ObsidianOnBackground,
    surface = ObsidianSurface,
    onSurface = ObsidianOnSurface,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianOnSurfaceVariant,
    outline = ObsidianOutline,
    error = ObsidianError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to prioritize our handcrafted High Density theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
