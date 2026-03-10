package com.example.breez.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breez.R
import com.example.breez.WeatherScreenBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val splashTotalMs = 2600L

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.76f) }
    val logoRotation = remember { Animatable(-6f) }
    val contentAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(12f) }

    val onBg = MaterialTheme.colorScheme.onBackground
    val density = LocalDensity.current

    val infinite = rememberInfiniteTransition(label = "splash")
    val floatY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(420)) }
        launch { contentAlpha.animateTo(1f, tween(560)) }

        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 950
                    0.76f at 0
                    1.08f at 650
                    1.0f at 950
                }
            )
        }

        launch {
            logoRotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 950
                    -6f at 0
                    2f at 650
                    0f at 950
                }
            )
        }

        launch {
            textOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }

        delay(splashTotalMs)
        onFinish()
    }

    WeatherScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = center
                    val baseR = size.minDimension * 0.40f
                    val pulseR = baseR + (size.minDimension * 0.07f * pulse)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                onBg.copy(alpha = 0.14f),
                                onBg.copy(alpha = 0.02f),
                                onBg.copy(alpha = 0f)
                            ),
                            center = c,
                            radius = pulseR * 1.25f
                        ),
                        radius = pulseR * 1.25f,
                        center = c
                    )

                    drawCircle(
                        color = onBg.copy(alpha = 0.08f),
                        radius = baseR * 1.22f,
                        center = c,
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = onBg.copy(alpha = 0.05f),
                        radius = baseR * 1.50f,
                        center = c,
                        style = Stroke(width = 2f)
                    )

                    val dots = 10
                    val orbitR = baseR * 1.40f

                    repeat(dots) { i ->
                        val ang = phase + (i * (2f * PI / dots).toFloat())
                        val x = c.x + cos(ang) * orbitR
                        val y = c.y + sin(ang * 1.2f) * (orbitR * 0.35f)

                        val alpha = 0.05f + (0.06f * (0.5f + 0.5f * sin(ang)))
                        val radius = 3.5f + (2.5f * (0.5f + 0.5f * cos(ang)))

                        drawCircle(
                            color = onBg.copy(alpha = alpha),
                            radius = radius,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Breez Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            translationY = with(density) { floatY.dp.toPx() }
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            rotationZ = logoRotation.value
                            alpha = logoAlpha.value
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Forecast made calm",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                    color = onBg.copy(alpha = 0.80f),
                    modifier = Modifier
                        .alpha(contentAlpha.value)
                        .offset(y = textOffset.value.dp)
                )
            }
        }
    }
}