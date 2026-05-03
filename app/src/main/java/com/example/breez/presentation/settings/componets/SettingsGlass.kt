package com.example.breez.presentation.settings.componets

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun settingsGlassSurfaceColor(): Color {
    return MaterialTheme.colorScheme.surface.copy(
        alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.22f else 0.74f
    )
}

@Composable
fun settingsGlassBorderColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    }
}

@Composable
fun settingsSoftOverlayColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }
}
