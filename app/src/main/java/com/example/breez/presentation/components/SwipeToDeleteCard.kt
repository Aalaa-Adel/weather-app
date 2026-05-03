package com.example.breez.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.breez.R
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDeleteCard(
    modifier: Modifier = Modifier,
    onSwipeDelete: () -> Unit,
    onIconDelete: () -> Unit,
    onLongPress: () -> Unit = {},
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val cardShape = RoundedCornerShape(20.dp)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val revealWidthPx = with(density) { 88.dp.toPx() }
        val deleteTriggerPx = maxWidthPx * 0.35f

        val revealedFraction = (abs(offsetX.value) / revealWidthPx).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
        ) {
            if (offsetX.value < 0f) {
                DeleteBackground(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(revealedFraction),
                    shape = cardShape,
                    onDeleteClick = onIconDelete
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .graphicsLayer {
                        alpha = 1f - (abs(offsetX.value) / (maxWidthPx * 2f)).coerceAtMost(0.15f)
                    }
                    .pointerInput(maxWidthPx) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    val newOffset = (offsetX.value + dragAmount)
                                        .coerceIn(-maxWidthPx, 0f)
                                    offsetX.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    when {
                                        abs(offsetX.value) >= deleteTriggerPx -> {
                                            offsetX.animateTo(
                                                targetValue = -maxWidthPx,
                                                animationSpec = tween(
                                                    durationMillis = 220,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                            onSwipeDelete()
                                            offsetX.snapTo(0f)
                                        }

                                        abs(offsetX.value) >= revealWidthPx / 2f -> {
                                            offsetX.animateTo(
                                                targetValue = -revealWidthPx,
                                                animationSpec = tween(
                                                    durationMillis = 220,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        else -> {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(
                                                    durationMillis = 220,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongPress
                    ),
                shape = cardShape,
                color = glassSurfaceColor(),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.2.dp,
                            color = glassBorderColor(),
                            shape = cardShape
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DeleteBackground(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
            )
            .padding(end = 12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            onClick = onDeleteClick,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}