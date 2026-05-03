package com.example.breez.presentation.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.presentation.components.WeatherLottieIcon
import com.example.breez.presentation.components.glassBorderColor
import com.example.breez.presentation.components.glassSurfaceColor
import com.example.breez.presentation.components.weatherLottieAsset
import com.example.breez.utils.formatTemperature

@Composable
fun CurrentWeatherHero(
    temperature: Double,
    description: String,
    iconCode: String,
    dateTimeText: String,
    temperatureUnit: TemperatureUnit
) {
    val fadeIn = remember { Animatable(0f) }
    val scaleIn = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        fadeIn.animateTo(
            targetValue = 1f,
            animationSpec = tween(850, easing = FastOutSlowInEasing)
        )
        scaleIn.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 240f)
        )
    }

    val context = LocalContext.current

    val temperatureDisplay = formatTemperature(
        context = context,
        value = temperature,
        unit = temperatureUnit
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = fadeIn.value
                scaleX = scaleIn.value
                scaleY = scaleIn.value
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = glassSurfaceColor(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = glassBorderColor(),
                        shape = RoundedCornerShape(34.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    WeatherLottieIcon(
                        assetName = weatherLottieAsset(iconCode),
                        modifier = Modifier.size(148.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = temperatureDisplay,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}