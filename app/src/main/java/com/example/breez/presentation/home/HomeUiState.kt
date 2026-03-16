package com.example.breez.presentation.home

import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto

sealed class HomeUiState {
    data class Loading(val isRefreshing: Boolean = false) : HomeUiState()

    data class Success(
        val currentWeather: CurrentWeatherDto,
        val forecast: ForecastResponseDto,
        val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
        val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
        val locationSource: LocationSource = LocationSource.GPS,
        val showLocationSettingsDialog: Boolean = false,
        val isRefreshing: Boolean = false
    ) : HomeUiState()

    data class Error(
        val message: String,
        val currentWeather: CurrentWeatherDto? = null,
        val forecast: ForecastResponseDto? = null,
        val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
        val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND,
        val locationSource: LocationSource = LocationSource.GPS
    ) : HomeUiState()
}