package com.example.breez.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.presentation.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToAddFavorite: () -> Unit = {},
    onNavigateToFavoriteDetails: (Double, Double, String, Long?) -> Unit = { _, _, _, _ -> },
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var recentlyDeleted by remember { mutableStateOf<FavoriteEntity?>(null) }

    WeatherScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                BreezTopBar(
                    title = "Favorites",
                    subtitle = "Your saved weather locations"
                )

                when (val state = uiState) {
                    is FavoritesUiState.Loading -> {
                        LoadingContent()
                    }

                    is FavoritesUiState.Success -> {
                        if (state.favorites.isEmpty()) {
                            EmptyFavoritesContent(
                                onAddFavorite = onNavigateToAddFavorite
                            )
                        } else {
                            FavoritesContent(
                                favorites = state.favorites,
                                onFavoriteClick = onNavigateToFavoriteDetails,
                                onDeleteFavorite = { favorite ->
                                    recentlyDeleted = favorite
                                    viewModel.deleteFavorite(favorite)

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Removed ${favorite.cityName}",
                                            actionLabel = "Undo",
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            recentlyDeleted?.let { deletedFavorite ->
                                                viewModel.addFavorite(deletedFavorite)
                                            }
                                        }
                                        recentlyDeleted = null
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .padding(bottom = 90.dp)
            ) {
                EnhancedFAB(
                    onClick = onNavigateToAddFavorite,
                    contentDescription = "Add favorite location"
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .padding(bottom = 90.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        EnhancedSnackbar(snackbarData = snackbarData)
                    }
                )
            }
        }
    }
}
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Loading favorites...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent(
    onAddFavorite: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    softOverlayColor(),
                                    softOverlayColor().copy(alpha = 0.4f),
                                    androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Add locations to quickly access their weather forecasts and stay updated with your favorite places.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAddFavorite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Map,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Add your first location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
private fun FavoritesContent(
    favorites: List<FavoriteEntity>,
    onFavoriteClick: (Double, Double, String, Long?) -> Unit,
    onDeleteFavorite: (FavoriteEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = favorites,
            key = { it.id }
        ) { favorite ->
            SwipeToDeleteCard(
                onSwipeDelete = { onDeleteFavorite(favorite) },
                onIconDelete = { onDeleteFavorite(favorite) },
                onClick = {
                    onFavoriteClick(
                        favorite.latitude,
                        favorite.longitude,
                        favorite.cityName,
                        favorite.id
                    )
                }
            ) {
                FavoriteCardContent(favorite = favorite)
            }
        }
    }
}

@Composable
private fun FavoriteCardContent(favorite: FavoriteEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (favorite.weatherIcon != null) {
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${favorite.weatherIcon}@2x.png",
                contentDescription = favorite.weatherDescription,
                modifier = Modifier.size(64.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = favorite.cityName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (favorite.temperature != null) {
                Text(
                    text = "${favorite.temperature.toInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            favorite.weatherDescription?.let { desc ->
                Text(
                    text = desc.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (favorite.temperature == null) {
                val subtitle = buildList {
                    favorite.state?.takeIf { it.isNotBlank() }?.let { add(it) }
                    favorite.countryCode?.takeIf { it.isNotBlank() }?.let { add(it) }
                }.joinToString(", ")

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (favorite.temperature != null && favorite.humidity != null) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF61D3FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${favorite.humidity}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }

                favorite.windSpeed?.let { wind ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Air,
                            contentDescription = null,
                            tint = Color(0xFF9EC5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${wind.toInt()} m/s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun EnhancedSnackbar(snackbarData: SnackbarData) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = glassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.5.dp,
                    glassBorderColor(),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() }
                ) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}