package com.example.breez.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteCard(
    modifier: Modifier = Modifier,
    onSwipeDelete: () -> Unit,
    onIconDelete: () -> Unit,
    onLongPress: () -> Unit = {},
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    var showDeleteIcon by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = offsetX.value < -30f,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .height(90.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Main card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    alpha = 1f - (abs(offsetX.value) / (swipeThreshold * 2)).coerceAtMost(0.2f)
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value <= -swipeThreshold) {
                                    onSwipeDelete()
                                }
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            }
                        }
                    ) { _, dragAmount ->
                        scope.launch {
                            val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                            offsetX.snapTo(newOffset)
                        }
                    }
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(24.dp),
            color = glassSurfaceColor(),
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        glassBorderColor(),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                content()
                
                if (showDeleteIcon) {
                    IconButton(
                        onClick = onIconDelete,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}