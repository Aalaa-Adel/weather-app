package com.example.breez.presentation.theme

import androidx.compose.ui.graphics.Brush

object AppGradients {
    val darkBackground = Brush.verticalGradient(
        colors = listOf(DarkGradientTop, DarkGradientBottom)
    )

    val lightBackground = Brush.verticalGradient(
        colors = listOf(LightGradientTop, LightGradientBottom)
    )
    val darkCard = Brush.verticalGradient(
        colors = listOf(DarkCardGradientStart, DarkCardGradientEnd)
    )

    val lightCard = Brush.verticalGradient(
        colors = listOf(LightCardGradientStart, LightCardGradientEnd)
    )
}