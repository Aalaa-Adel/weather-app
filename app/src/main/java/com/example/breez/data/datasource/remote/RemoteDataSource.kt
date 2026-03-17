package com.example.breez.data.datasource.remote

import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult


interface RemoteDataSource {

    suspend fun fetchCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto>

    suspend fun fetchForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<ForecastResponseDto>

    suspend fun fetchCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>>

    suspend fun fetchLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>>
}
