package com.example.breez.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun glassSurfaceColor(): Color {
    return MaterialTheme.colorScheme.surface.copy(
        alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.28f else 0.85f
    )
}

@Composable
fun glassBorderColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }
}

@Composable
fun softOverlayColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }
}
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassSurfaceColor())
            .border(
                width = 1.5.dp,
                color = glassBorderColor(),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit,
    size: Dp = 52.dp,
    cornerRadius: Dp = 20.dp
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        color = glassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .border(
                    width = 1.5.dp,
                    color = glassBorderColor(),
                    shape = RoundedCornerShape(cornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
fun EnhancedFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Outlined.Add,
    contentDescription: String = "Add"
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = {
            scope.launch {
                scale.animateTo(0.9f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                scale.animateTo(1f, animationSpec = tween(100, easing = FastOutSlowInEasing))
            }
            onClick()
        },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        modifier = Modifier
            .size(54.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BreezTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onBackClick?.let { backClick ->
                GlassIconButton(
                    onClick = backClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f)
                    )
                }
            }

            Row(content = actions)
        }
    }
}