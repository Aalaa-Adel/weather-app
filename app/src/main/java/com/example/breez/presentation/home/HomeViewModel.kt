package com.example.breez.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.datasource.preferences.SettingsPreferencesManager
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.network.NetworkMonitor
import com.example.breez.data.repository.WeatherRepository
import com.example.breez.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val networkMonitor: NetworkMonitor,
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var currentLat: Double = 26.571800
    private var currentLon: Double = 31.708733

    private var currentTemperatureUnit: TemperatureUnit =
        settingsPreferencesManager.getCurrentSettings().temperatureUnit

    private var currentWindSpeedUnit: WindSpeedUnit = settingsPreferencesManager.getCurrentSettings().windSpeedUnit

    private var currentLanguageApiValue: String =
        settingsPreferencesManager.getCurrentSettings().language.apiValue

    init {
        observeInternetConnection()
        observeSettings()
    }

    private fun observeInternetConnection() {
        viewModelScope.launch {
            networkMonitor.observeNetworkStatus().collect { connected ->
                _isConnected.value = connected
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsPreferencesManager.settingsFlow.collect { settings ->
                val oldUnit = currentTemperatureUnit
                val oldLanguage = currentLanguageApiValue
                val oldWindSpeedUnit = currentWindSpeedUnit

                currentTemperatureUnit = settings.temperatureUnit
                currentLanguageApiValue = settings.language.apiValue
                currentWindSpeedUnit = settings.windSpeedUnit

                val shouldReload =
                    _uiState.value.currentWeather == null ||
                            oldUnit != settings.temperatureUnit ||
                            oldLanguage != settings.language.apiValue ||
                            oldWindSpeedUnit != settings.windSpeedUnit

                if (shouldReload) {
                    loadWeatherData()
                }
            }
        }
    }
    fun loadWeatherData(
        lat: Double = currentLat,
        lon: Double = currentLon,
        forceRefresh: Boolean = false
    ) {
        currentLat = lat
        currentLon = lon

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.currentWeather == null,
                isRefreshing = forceRefresh && _uiState.value.currentWeather != null,
                error = null
            )

            val currentWeatherDeferred = async {
                repository.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    units = currentTemperatureUnit.apiValue,
                    lang = currentLanguageApiValue
                )
            }

            val forecastDeferred = async {
                repository.getForecast(
                    lat = lat,
                    lon = lon,
                    units = currentTemperatureUnit.apiValue,
                    lang = currentLanguageApiValue
                )
            }

            val currentWeatherResult = currentWeatherDeferred.await()
            val forecastResult = forecastDeferred.await()

            when {
                currentWeatherResult is ApiResult.Success &&
                        forecastResult is ApiResult.Success -> {
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        isRefreshing = false,
                        currentWeather = currentWeatherResult.data,
                        forecast = forecastResult.data,
                        temperatureUnit = currentTemperatureUnit,
                        windSpeedUnit = currentWindSpeedUnit
                    )
                }

                currentWeatherResult is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = currentWeatherResult.message,
                        temperatureUnit = currentTemperatureUnit,
                        windSpeedUnit = currentWindSpeedUnit
                    )
                }

                forecastResult is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = forecastResult.message,
                        temperatureUnit = currentTemperatureUnit,
                        windSpeedUnit = currentWindSpeedUnit
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Something went wrong",
                        temperatureUnit = currentTemperatureUnit,
                        windSpeedUnit = currentWindSpeedUnit
                    )
                }
            }
        }
    }

    fun refresh() {
        loadWeatherData(
            lat = currentLat,
            lon = currentLon,
            forceRefresh = true
        )
    }

}