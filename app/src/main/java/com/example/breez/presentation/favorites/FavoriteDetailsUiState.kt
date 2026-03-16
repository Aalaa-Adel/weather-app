package com.example.breez.presentation.favorites

import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto

sealed class FavoriteDetailsUiState {
    data object Loading : FavoriteDetailsUiState()

    data class Success(
        val currentWeather: CurrentWeatherDto,
        val forecast: ForecastResponseDto,
        val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
        val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND
    ) : FavoriteDetailsUiState()

    data class Error(
        val message: String
    ) : FavoriteDetailsUiState()
}