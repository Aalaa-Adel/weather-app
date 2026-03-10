package com.example.breez.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Settings

@Composable
fun BreezCurvedBottomBar(
    onHomeClick: () -> Unit,
    onCenterClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        CurvedBottomBarBackground()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarIcon(
                icon = Icons.Outlined.Home,
                contentDescription = "Home",
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )

            BottomBarIcon(
                icon = Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorites",
                onClick = onFavoriteClick,
                modifier = Modifier.weight(1f)
            )

            Box(modifier = Modifier.weight(1.2f))

            BottomBarIcon(
                icon = Icons.Outlined.AddAlert,
                contentDescription = "Alarm",
                onClick = onCenterClick,
                modifier = Modifier.weight(1f)
            )

            BottomBarIcon(
                icon = Icons.Outlined.Settings,
                contentDescription = "Settings",
                onClick = onMenuClick,
                modifier = Modifier.weight(1f)
            )
        }

        CenterFloatingButton(
            onClick = onCenterClick
        )
    }
}

@Composable
private fun CurvedBottomBarBackground() {
    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(0f, 28f)

                quadraticBezierTo(
                    width * 0.10f, 0f,
                    width * 0.22f, 18f
                )

                lineTo(width * 0.36f, 18f)

                cubicTo(
                    width * 0.42f, 18f,
                    width * 0.42f, 70f,
                    width * 0.50f, 70f
                )

                cubicTo(
                    width * 0.58f, 70f,
                    width * 0.58f, 18f,
                    width * 0.64f, 18f
                )

                lineTo(width * 0.78f, 18f)

                quadraticBezierTo(
                    width * 0.90f, 0f,
                    width, 28f
                )

                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path,
                color = containerColor
            )

            drawPath(
                path = path,
                color = borderColor,
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun CenterFloatingButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .border(
                    width = 1.4.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Choose location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BottomBarIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}