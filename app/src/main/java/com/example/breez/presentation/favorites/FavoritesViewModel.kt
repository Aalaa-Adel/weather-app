package com.example.breez.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.db.entity.FavoriteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: BreezRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getAllFavorites().collect { favorites ->
                _uiState.value = FavoritesUiState.Success(favorites = favorites)
            }
        }
    }

    fun deleteFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
            _showToast.emit("Favorite deleted")
        }
    }

    fun addFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.insertFavorite(favorite)
            _showToast.emit("Favorite added")
        }
    }
}