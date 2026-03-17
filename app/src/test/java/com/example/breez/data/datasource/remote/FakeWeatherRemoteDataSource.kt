package com.example.breez.data.datasource.remote

import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult


class FakeWeatherRemoteDataSource(
    private val shouldReturnError: Boolean = false
) : RemoteDataSource {

    override suspend fun fetchCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<ForecastResponseDto> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>> {
        return if (shouldReturnError) {
            ApiResult.Error("Network error")
        } else {
            ApiResult.Success(
                listOf(
                    GeocodingDto(
                        name = cityName,
                        lat = 30.0444,
                        lon = 31.2357,
                        country = "EG",
                        state = "Cairo"
                    )
                )
            )
        }
    }

    override suspend fun fetchLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>> {
        return if (shouldReturnError) {
            ApiResult.Error("Network error")
        } else {
            ApiResult.Success(
                listOf(
                    GeocodingDto(
                        name = "Cairo",
                        lat = lat,
                        lon = lon,
                        country = "EG",
                        state = "Cairo"
                    )
                )
            )
        }
    }
}
