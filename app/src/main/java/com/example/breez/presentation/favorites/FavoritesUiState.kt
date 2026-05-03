package com.example.breez.presentation.favorites

import com.example.breez.data.db.entity.FavoriteEntity

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()

    data class Success(
        val favorites: List<FavoriteEntity> = emptyList()
    ) : FavoritesUiState()
}