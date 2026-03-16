package com.example.breez.presentation.favorites

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.presentation.location.MapboxPickLocationScreen

@Composable
fun AddFavoriteScreen(
    onBackClick: () -> Unit = {},
    onFavoriteSaved: () -> Unit = {},
    viewModel: AddFavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            onFavoriteSaved()
        }
    }

    LaunchedEffect(Unit) {
        val currentState = uiState
        if (currentState is AddFavoriteUiState.Idle && currentState.selectedLocation == null) {
            viewModel.updateSelectedLocation(30.0444, 31.2357)
        }
    }

    val selectedLocation = when (val state = uiState) {
        is AddFavoriteUiState.Idle -> state.selectedLocation
        is AddFavoriteUiState.Saving -> state.selectedLocation
        is AddFavoriteUiState.Error -> state.selectedLocation
        is AddFavoriteUiState.Success -> null
    }

    MapboxPickLocationScreen(
        initialLat = selectedLocation?.latitude,
        initialLon = selectedLocation?.longitude,
        onLocationChanged = { lat, lon ->
            viewModel.updateSelectedLocation(lat, lon)
        },
        onConfirm = { lat, lon ->
            viewModel.updateSelectedLocation(lat, lon)
            viewModel.saveFavorite()
        },
        onCancel = onBackClick
    )
}
