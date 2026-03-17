package com.example.breez.data.datasource.remote

import com.example.breez.BuildConfig
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class WeatherRemoteDataSource @Inject constructor(
    private val apiService: WeatherApiService
) : RemoteDataSource {
    private val weatherApiKey = BuildConfig.OPEN_WEATHER_API_KEY

    override suspend fun fetchCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto> {
        return try {
            val response = apiService.getCurrentWeather(
                lat = lat,
                lon = lon,
                units = units,
                lang = lang,
                apiKey = weatherApiKey
            )
            ApiResult.Success(response)
        } catch (e: HttpException) {
            ApiResult.Error(
                message = e.message ?: "HTTP error occurred",
                code = e.code()
            )
        } catch (e: IOException) {
            ApiResult.Error("No internet connection")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun fetchForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<ForecastResponseDto> {
        return try {
            val response = apiService.getForecast(
                lat = lat,
                lon = lon,
                lang = lang,
                units = units,
                apiKey = weatherApiKey
            )
            ApiResult.Success(response)
        } catch (e: HttpException) {
            ApiResult.Error(
                message = e.message ?: "HTTP error occurred",
                code = e.code()
            )
        } catch (e: IOException) {
            ApiResult.Error("No internet connection")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun fetchCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>> {
        return try {
            val response = apiService.getCoordinatesFromCityName(
                cityName = cityName,
                apiKey = weatherApiKey
            )
            ApiResult.Success(response)
        } catch (e: HttpException) {
            ApiResult.Error(
                message = e.message ?: "HTTP error occurred",
                code = e.code()
            )
        } catch (e: IOException) {
            ApiResult.Error("Network error occurred")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun fetchLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>> {
        return try {
            val response = apiService.getLocationNameFromCoordinates(
                lat = lat,
                lon = lon,
                apiKey = weatherApiKey
            )
            ApiResult.Success(response)
        } catch (e: HttpException) {
            ApiResult.Error(
                message = e.message ?: "HTTP error occurred",
                code = e.code()
            )
        } catch (e: IOException) {
            ApiResult.Error("Network error occurred")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}