package com.example.breez.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.datasource.preferences.SettingsPreferencesManager
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.network.NetworkMonitor
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.data.location.LocationProvider
import com.example.breez.data.location.LocationResult

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BreezRepository,
    private val networkMonitor: NetworkMonitor,
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private var currentTemperatureUnit: TemperatureUnit =
        settingsPreferencesManager.getCurrentSettings().temperatureUnit

    private var currentWindSpeedUnit: WindSpeedUnit = settingsPreferencesManager.getCurrentSettings().windSpeedUnit

    private var currentLanguageApiValue: String =
        settingsPreferencesManager.getCurrentSettings().language.apiValue

    private var currentLocationSource: LocationSource =
        settingsPreferencesManager.getCurrentSettings().locationSource

    init {
        observeInternetConnection()
        observeSettings()
        loadWeatherData()
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
                val oldLocationSource = currentLocationSource
                val oldSettings = settings

                currentTemperatureUnit = settings.temperatureUnit
                currentLanguageApiValue = settings.language.apiValue
                currentWindSpeedUnit = settings.windSpeedUnit
                currentLocationSource = settings.locationSource

                val shouldReload =
                    _uiState.value !is HomeUiState.Success ||
                            oldUnit != settings.temperatureUnit ||
                            oldLanguage != settings.language.apiValue ||
                            oldWindSpeedUnit != settings.windSpeedUnit ||
                            oldLocationSource != settings.locationSource ||
                            (settings.locationSource == LocationSource.MAP && (currentLat != settings.homeLat?.toDouble() || currentLon != settings.homeLon?.toDouble()))

                if (shouldReload) {
                    loadWeatherData()
                }
            }
        }
    }
    fun loadWeatherData(
        forceRefresh: Boolean = false,
        grantedLat: Double? = null,
        grantedLon: Double? = null
    ) {

        viewModelScope.launch {
            val currentState = _uiState.value
            val hasExistingData = currentState is HomeUiState.Success

            _uiState.value = if (!hasExistingData && !forceRefresh) {
                HomeUiState.Loading()
            } else if (forceRefresh && hasExistingData) {
                (currentState as HomeUiState.Success).copy(isRefreshing = true)
            } else {
                currentState
            }

            if (currentLocationSource == LocationSource.GPS) {
                if (grantedLat != null && grantedLon != null) {
                    currentLat = grantedLat
                    currentLon = grantedLon
                } else {
                    when (val result = locationProvider.getCurrentLocationWithStatus()) {
                        is LocationResult.Success -> {
                            currentLat = result.location.latitude
                            currentLon = result.location.longitude
                        }
                        is LocationResult.LocationDisabled -> {
                            if (currentState is HomeUiState.Success) {
                                _uiState.value = currentState.copy(
                                    showLocationSettingsDialog = true,
                                    isRefreshing = false
                                )
                            } else {
                                _showToast.emit("Location is disabled. Please enable it in Settings.")
                            }
                            return@launch
                        }
                        is LocationResult.PermissionDenied -> {
                            if (currentState is HomeUiState.Success) {
                                _uiState.value = currentState.copy(isRefreshing = false)
                            } else {
                                _uiState.value = HomeUiState.Error(
                                    message = "Location permission required. Please grant permission in Settings.",
                                    temperatureUnit = currentTemperatureUnit,
                                    windSpeedUnit = currentWindSpeedUnit,
                                    locationSource = currentLocationSource
                                )
                            }
                            return@launch
                        }
                        is LocationResult.Error -> {
                            if (currentLat == null) {
                                if (currentState is HomeUiState.Success) {
                                    _uiState.value = currentState.copy(isRefreshing = false)
                                } else {
                                    _uiState.value = HomeUiState.Error(
                                        message = "Unable to get location. Please try again.",
                                        temperatureUnit = currentTemperatureUnit,
                                        windSpeedUnit = currentWindSpeedUnit,
                                        locationSource = currentLocationSource
                                    )
                                }
                                return@launch
                            }
                        }
                    }
                }
            } else {
                val settings = settingsPreferencesManager.getCurrentSettings()
                if (settings.homeLat != null && settings.homeLon != null) {
                    currentLat = settings.homeLat.toDouble()
                    currentLon = settings.homeLon.toDouble()
                } else {
                    _uiState.value = HomeUiState.Error(
                        message = "No map location selected. Please pick a location in Settings.",
                        temperatureUnit = currentTemperatureUnit,
                        windSpeedUnit = currentWindSpeedUnit,
                        locationSource = currentLocationSource
                    )
                    return@launch
                }
            }

            val finalLat = currentLat ?: return@launch
            val finalLon = currentLon ?: return@launch

            val currentWeatherDeferred = async {
                repository.getCurrentWeather(
                    lat = finalLat,
                    lon = finalLon,
                    units = currentTemperatureUnit.apiValue,
                    lang = currentLanguageApiValue
                )
            }

            val forecastDeferred = async {
                repository.getForecast(
                    lat = finalLat,
                    lon = finalLon,
                    units = currentTemperatureUnit.apiValue,
                    lang = currentLanguageApiValue
                )
            }

            val currentWeatherResult = currentWeatherDeferred.await()
            val forecastResult = forecastDeferred.await()

            when {
                currentWeatherResult is ApiResult.Success &&
                        forecastResult is ApiResult.Success -> {
                    _uiState.value = HomeUiState.Success(
                        currentWeather = currentWeatherResult.data,
                        forecast = forecastResult.data,
                        temperatureUnit = currentTemperatureUnit,
                        windSpeedUnit = currentWindSpeedUnit,
                        locationSource = currentLocationSource,
                        isRefreshing = false
                    )
                }

                currentWeatherResult is ApiResult.Error -> {
                    val state = _uiState.value
                    if (state is HomeUiState.Success) {
                        _uiState.value = state.copy(isRefreshing = false)
                    } else {
                        _uiState.value = HomeUiState.Error(
                            message = currentWeatherResult.message,
                            temperatureUnit = currentTemperatureUnit,
                            windSpeedUnit = currentWindSpeedUnit,
                            locationSource = currentLocationSource
                        )
                    }
                }

                forecastResult is ApiResult.Error -> {
                    val state = _uiState.value
                    if (state is HomeUiState.Success) {
                        _uiState.value = state.copy(isRefreshing = false)
                    } else {
                        _uiState.value = HomeUiState.Error(
                            message = forecastResult.message,
                            temperatureUnit = currentTemperatureUnit,
                            windSpeedUnit = currentWindSpeedUnit,
                            locationSource = currentLocationSource
                        )
                    }
                }

                else -> {
                    val state = _uiState.value
                    if (state is HomeUiState.Success) {
                        _uiState.value = state.copy(isRefreshing = false)
                    } else {
                        _uiState.value = HomeUiState.Error(
                            message = "Something went wrong",
                            temperatureUnit = currentTemperatureUnit,
                            windSpeedUnit = currentWindSpeedUnit,
                            locationSource = currentLocationSource
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadWeatherData(
            forceRefresh = true
        )
    }



    fun dismissLocationDialog() {
        val state = _uiState.value
        if (state is HomeUiState.Success) {
            _uiState.value = state.copy(showLocationSettingsDialog = false)
        }
    }
}
