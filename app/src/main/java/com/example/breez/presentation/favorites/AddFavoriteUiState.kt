package com.example.breez.presentation.favorites

sealed class AddFavoriteUiState {
    data class Idle(
        val selectedLocation: SelectedLocation? = null,
        val cityName: String? = null,
        val countryCode: String? = null,
        val stateName: String? = null
    ) : AddFavoriteUiState()

    data class Saving(
        val selectedLocation: SelectedLocation
    ) : AddFavoriteUiState()

    data object Success : AddFavoriteUiState()

    data class Error(
        val message: String,
        val selectedLocation: SelectedLocation? = null
    ) : AddFavoriteUiState()
}