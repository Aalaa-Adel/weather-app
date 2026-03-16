package com.example.breez.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.datasource.preferences.SettingsPreferencesManager
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteDetailsViewModel @Inject constructor(
    private val weatherRepository: BreezRepository,
    private val settingsManager: SettingsPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoriteDetailsUiState>(FavoriteDetailsUiState.Loading)
    val uiState: StateFlow<FavoriteDetailsUiState> = _uiState.asStateFlow()

    private val _showError = MutableSharedFlow<String>()
    val showError: SharedFlow<String> = _showError.asSharedFlow()

    fun loadWeatherData(lat: Double, lon: Double, favoriteId: Long? = null) {
        viewModelScope.launch {
            _uiState.value = FavoriteDetailsUiState.Loading

            val settings = settingsManager.getCurrentSettings()

            val currentWeatherDeferred = async {
                weatherRepository.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    units = settings.temperatureUnit.apiValue,
                    lang = settings.language.apiValue
                )
            }

            val forecastDeferred = async {
                weatherRepository.getForecast(
                    lat = lat,
                    lon = lon,
                    units = settings.temperatureUnit.apiValue,
                    lang = settings.language.apiValue
                )
            }

            val currentWeatherResult = currentWeatherDeferred.await()
            val forecastResult = forecastDeferred.await()

            when {
                currentWeatherResult is ApiResult.Success &&
                        forecastResult is ApiResult.Success -> {
                    _uiState.value = FavoriteDetailsUiState.Success(
                        currentWeather = currentWeatherResult.data,
                        forecast = forecastResult.data,
                        temperatureUnit = settings.temperatureUnit,
                        windSpeedUnit = settings.windSpeedUnit
                    )

                    favoriteId?.let { id ->
                        val favorite = weatherRepository.getFavoriteById(id)
                        favorite?.let {
                            val weather = currentWeatherResult.data
                            weatherRepository.updateFavorite(
                                it.copy(
                                    temperature = weather.main.temp,
                                    weatherDescription = weather.weather.firstOrNull()?.description,
                                    weatherIcon = weather.weather.firstOrNull()?.icon,
                                    humidity = weather.main.humidity,
                                    windSpeed = weather.wind.speed,
                                    pressure = weather.main.pressure,
                                    clouds = weather.clouds.all,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }

                currentWeatherResult is ApiResult.Error -> {
                    _uiState.value = FavoriteDetailsUiState.Error(
                        message = currentWeatherResult.message
                    )
                    _showError.emit(currentWeatherResult.message)
                }

                forecastResult is ApiResult.Error -> {
                    _uiState.value = FavoriteDetailsUiState.Error(
                        message = forecastResult.message
                    )
                    _showError.emit(forecastResult.message)
                }

                else -> {
                    _uiState.value = FavoriteDetailsUiState.Error(
                        message = "Something went wrong"
                    )
                    _showError.emit("Something went wrong")
                }
            }
        }
    }
}
