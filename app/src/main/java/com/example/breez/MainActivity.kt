package com.example.breez

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.example.breez.data.repository.WeatherRepository
import com.example.breez.data.util.ApiResult
import com.example.breez.presentation.navigation.NavGraph
import com.example.breez.presentation.theme.AppGradients
import com.example.breez.presentation.theme.BreezTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: WeatherRepository

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