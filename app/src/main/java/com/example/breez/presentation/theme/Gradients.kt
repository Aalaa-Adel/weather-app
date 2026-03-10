package com.example.breez.presentation.theme

import androidx.compose.ui.graphics.Brush

object AppGradients {
    val darkBackground = Brush.verticalGradient(
        colors = listOf(
            DarkGradientTop,
            DarkGradientMid,
            DarkGradientBottom
        )
    )

    val lightBackground = Brush.verticalGradient(
        colors = listOf(
            LightGradientTop,
            LightGradientMid,
            LightGradientBottom
        )
    )
}