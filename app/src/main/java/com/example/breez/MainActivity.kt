package com.example.breez

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.breez.navigation.NavGraph
import com.example.breez.ui.theme.AppGradients
import com.example.breez.ui.theme.BreezTheme
import com.example.breez.ui.theme.DarkGradientBottom
import com.example.breez.ui.theme.DarkGradientTop
import com.example.breez.ui.theme.LightGradientBottom
import com.example.breez.ui.theme.LightGradientTop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreezTheme {
               NavGraph()
            }

        }
    }
}

@Composable
fun WeatherScreenBackground(
    content: @Composable BoxScope.() -> Unit
) {

    val brush =
        if (isSystemInDarkTheme()) AppGradients.darkBackground else AppGradients.lightBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = brush)
    ) {
        content()
    }
}