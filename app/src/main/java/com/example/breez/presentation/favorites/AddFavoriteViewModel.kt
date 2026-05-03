package com.example.breez.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.datasource.preferences.SettingsPreferencesManager
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectedLocation(
    val latitude: Double,
    val longitude: Double
)

@HiltViewModel
class AddFavoriteViewModel @Inject constructor(
    private val repository: BreezRepository,
    private val settingsManager: SettingsPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddFavoriteUiState>(AddFavoriteUiState.Idle())
    val uiState: StateFlow<AddFavoriteUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    private fun t(ar: String, en: String): String {
        return if (settingsManager.getCurrentSettings().language.apiValue == "ar") ar else en
    }

    fun updateSelectedLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = when (currentState) {
                is AddFavoriteUiState.Idle -> currentState.copy(
                    selectedLocation = SelectedLocation(latitude, longitude)
                )

                is AddFavoriteUiState.Saving -> currentState
                is AddFavoriteUiState.Success -> currentState
                is AddFavoriteUiState.Error -> AddFavoriteUiState.Idle(
                    selectedLocation = SelectedLocation(latitude, longitude)
                )
            }

            val result = repository.getLocationNameFromCoordinates(latitude, longitude)
            if (result is ApiResult.Success && result.data.isNotEmpty()) {
                val locationData = result.data.first()
                val state = _uiState.value
                if (state is AddFavoriteUiState.Idle) {
                    _uiState.value = state.copy(
                        cityName = locationData.name,
                        countryCode = locationData.country,
                        stateName = locationData.state
                    )
                }
            }
        }
    }

    fun saveFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val location = when (currentState) {
                is AddFavoriteUiState.Idle -> currentState.selectedLocation
                else -> return@launch
            } ?: return@launch

            val cityName = when (currentState) {
                is AddFavoriteUiState.Idle -> currentState.cityName ?: t(
                    ar = "موقع غير معروف",
                    en = "Unknown Location"
                )

                else -> t(
                    ar = "موقع غير معروف",
                    en = "Unknown Location"
                )
            }

            val countryCode = when (currentState) {
                is AddFavoriteUiState.Idle -> currentState.countryCode
                else -> null
            }

            val stateName = when (currentState) {
                is AddFavoriteUiState.Idle -> currentState.stateName
                else -> null
            }

            _uiState.value = AddFavoriteUiState.Saving(selectedLocation = location)

            try {
                val settings = settingsManager.getCurrentSettings()

                val weatherResult = repository.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    units = settings.temperatureUnit.apiValue,
                    lang = settings.language.apiValue
                )

                val favorite = if (weatherResult is ApiResult.Success) {
                    val weather = weatherResult.data
                    FavoriteEntity(
                        cityName = cityName,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        countryCode = countryCode,
                        state = stateName,
                        temperature = weather.main.temp,
                        weatherDescription = weather.weather.firstOrNull()?.description,
                        weatherIcon = weather.weather.firstOrNull()?.icon,
                        humidity = weather.main.humidity,
                        windSpeed = weather.wind.speed,
                        pressure = weather.main.pressure,
                        clouds = weather.clouds.all,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    FavoriteEntity(
                        cityName = cityName,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        countryCode = countryCode,
                        state = stateName
                    )
                }

                repository.insertFavorite(favorite)
                _uiState.value = AddFavoriteUiState.Success
                _navigateBack.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = AddFavoriteUiState.Error(
                    message = t(
                        ar = "فشل حفظ الموقع المفضل: ${e.message}",
                        en = "Failed to save favorite: ${e.message}"
                    ),
                    selectedLocation = location
                )
            }
        }
    }
}