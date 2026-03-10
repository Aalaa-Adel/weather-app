package com.example.breez.data.datasource.remote

import android.content.Context
import com.example.breez.BuildConfig
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
//import com.example.breez.data.network.NetworkUtils
import com.example.breez.data.util.ApiResult
import retrofit2.HttpException
import java.io.IOException

class WeatherRemoteDataSource(
    private val context: Context,
    private val apiService: WeatherApiService
) {
    private val weatherApiKey = BuildConfig.OPEN_WEATHER_API_KEY

    suspend fun fetchCurrentWeather(lat: Double, lon: Double): ApiResult<CurrentWeatherDto> {
//        if (!NetworkUtils.isInternetAvailable(context)) {
//            return ApiResult.Error("No internet connection")
//        }

        return try {
            val response = apiService.getCurrentWeather(
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
            ApiResult.Error("No internet connection")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun fetchForecast(lat: Double, lon: Double): ApiResult<ForecastResponseDto> {
//        if (!NetworkUtils.isInternetAvailable(context)) {
//            return ApiResult.Error("No internet connection")
//        }
        return try {
            val response = apiService.getForecast(
                lat = lat,
                lon = lon,
                lang = "ar",
                units = "metric",
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

    suspend fun fetchCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>> {
//        if (!NetworkUtils.isInternetAvailable(context)) {
//            return ApiResult.Error("No internet connection")
//        }

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

    suspend fun fetchLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>> {
//        if (!NetworkUtils.isInternetAvailable(context)) {
//            return ApiResult.Error("No internet connection")
//        }

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