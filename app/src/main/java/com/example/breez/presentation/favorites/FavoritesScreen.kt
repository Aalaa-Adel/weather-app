package com.example.breez.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.R
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.presentation.components.BreezTopBar
import com.example.breez.presentation.components.CompactSnackbar
import com.example.breez.presentation.components.FAB
import com.example.breez.presentation.components.SwipeToDeleteCard
import com.example.breez.presentation.favorites.components.EmptyFavoritesContent
import com.example.breez.presentation.favorites.components.FavoriteCardContent
import com.example.breez.presentation.favorites.components.FavoritesLoadingContent
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
    val context = LocalContext.current
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
                    title = stringResource(R.string.favorites_title),
                    subtitle = stringResource(R.string.favorites_subtitle)
                )

                when (val state = uiState) {
                    is FavoritesUiState.Loading -> {
                        FavoritesLoadingContent()
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
                                            message = context.getString(
                                                R.string.favorite_removed,
                                                favorite.cityName
                                            ),
                                            actionLabel = context.getString(R.string.undo),
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
                    .padding(end = 20.dp, bottom = 130.dp)
            ) {
                FAB(
                    onClick = onNavigateToAddFavorite,
                    contentDescription = stringResource(R.string.cd_add_favorite_location)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 190.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        CompactSnackbar(snackbarData = snackbarData)
                    }
                )
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