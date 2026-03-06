package com.example.breez.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

    val SPLASH_TOTAL_MS = 5000L

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.75f) }
    val logoRotation = remember { Animatable(-8f) }
    val contentAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(14f) }

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
        launch { logoAlpha.animateTo(1f, tween(380)) }
        launch { contentAlpha.animateTo(1f, tween(520)) }

        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 950
                    0.75f at 0
                    1.10f at 650
                    1.0f at 950
                }
            )
        }

        launch {
            logoRotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 950
                    -8f at 0
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

        delay(SPLASH_TOTAL_MS)
        onFinish()
    }

    WeatherScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            val haloSize = 340.dp

            Box(
                modifier = Modifier
                    .size(haloSize)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = center

                    val baseR = size.minDimension * 0.40f
                    val pulseR = baseR + (size.minDimension * 0.07f * pulse)

                    drawCircle(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                onBg.copy(alpha = 0.14f),
                                onBg.copy(alpha = 0.02f),
                                onBg.copy(alpha = 0.00f),
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
                        color = onBg.copy(alpha = 0.06f),
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

                        val a = 0.05f + (0.06f * (0.5f + 0.5f * sin(ang)))
                        val r = 3.5f + (2.5f * (0.5f + 0.5f * cos(ang)))

                        drawCircle(
                            color = onBg.copy(alpha = a),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Breez Logo",
                    modifier = Modifier
                        .size(185.dp)
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