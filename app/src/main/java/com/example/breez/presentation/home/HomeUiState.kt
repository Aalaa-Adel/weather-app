package com.example.breez.presentation.home

import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentWeather: CurrentWeatherDto? = null,
    val forecast: ForecastResponseDto? = null
)