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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.data.datasource.preferences.ThemeMode
import com.example.breez.presentation.navigation.NavGraph
import com.example.breez.presentation.settings.SettingsViewModel
import com.example.breez.presentation.theme.AppGradients
import com.example.breez.presentation.theme.BreezTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode = settingsViewModel.themeMode.collectAsStateWithLifecycle().value

            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            BreezTheme(darkTheme = useDarkTheme) {
                NavGraph()
            }
        }
    }
}
@Composable
fun WeatherScreenBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == com.example.breez.presentation.theme.DarkBackground

    val brush = if (isDarkTheme) {
        AppGradients.darkBackground
    } else {
        AppGradients.lightBackground
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}