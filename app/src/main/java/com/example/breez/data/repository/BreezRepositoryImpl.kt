package com.example.breez.data.repository

import com.example.breez.data.datasource.local.LocalDataSource
import com.example.breez.data.datasource.remote.RemoteDataSource
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreezRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val gson: Gson,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BreezRepository {

    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto> {
        val cacheKey = "current_${lat}_${lon}_${units}_${lang}"

        val cachedData = localDataSource.getCache(cacheKey, System.currentTimeMillis())

        return try {
            val result = remoteDataSource.fetchCurrentWeather(
                lat = lat,
                lon = lon,
                units = units,
                lang = lang
            )

            if (result is ApiResult.Success) {
                val jsonData = gson.toJson(result.data)
                localDataSource.insertCache(
                    WeatherCacheEntity(
                        cacheKey = cacheKey,
                        jsonData = jsonData,
                        timestamp = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                    )
                )
            }

            result
        } catch (e: Exception) {

            var cacheToUse = cachedData

            if (cacheToUse == null) {
                cacheToUse = localDataSource.getCacheIgnoreExpiry(cacheKey)
            }

            if (cacheToUse != null) {
                try {
                    val weatherData =
                        gson.fromJson(cacheToUse.jsonData, CurrentWeatherDto::class.java)
                    ApiResult.Success(weatherData)
                } catch (parseError: Exception) {
                    ApiResult.Error("Network error and cached data is corrupted")
                }
            } else {
                ApiResult.Error("No internet connection")
            }
        }
    }

    override suspend fun getForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<ForecastResponseDto> {
        val cacheKey = "forecast_${lat}_${lon}_${units}_${lang}"

        val cachedData = localDataSource.getCache(cacheKey, System.currentTimeMillis())

        return try {
            val result = remoteDataSource.fetchForecast(
                lat = lat,
                lon = lon,
                units = units,
                lang = lang
            )

            if (result is ApiResult.Success) {
                val jsonData = gson.toJson(result.data)
                localDataSource.insertCache(
                    WeatherCacheEntity(
                        cacheKey = cacheKey,
                        jsonData = jsonData,
                        timestamp = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                    )
                )
            }

            result
        } catch (e: Exception) {

            var cacheToUse = cachedData

            if (cacheToUse == null) {
                cacheToUse = localDataSource.getCacheIgnoreExpiry(cacheKey)
            }

            if (cacheToUse != null) {
                try {
                    val forecastData =
                        gson.fromJson(cacheToUse.jsonData, ForecastResponseDto::class.java)
                    ApiResult.Success(forecastData)
                } catch (parseError: Exception) {
                    ApiResult.Error("Network error and cached data is corrupted")
                }
            } else {
                ApiResult.Error("No internet connection")
            }
        }
    }

    override suspend fun getCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>> {
        return remoteDataSource.fetchCoordinatesFromCityName(cityName)
    }

    override suspend fun getLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>> {
        return remoteDataSource.fetchLocationNameFromCoordinates(lat, lon)
    }

    override fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        return localDataSource.getAllFavorites()
    }

    override suspend fun getFavoriteById(id: Long): FavoriteEntity? {
        return localDataSource.getFavoriteById(id)
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        localDataSource.insertFavorite(favorite)
    }

    override suspend fun updateFavorite(favorite: FavoriteEntity) {
        localDataSource.updateFavorite(favorite)
    }

    override suspend fun deleteFavorite(favorite: FavoriteEntity) {
        localDataSource.deleteFavorite(favorite)
    }

    override suspend fun isFavorite(lat: Double, lon: Double): Boolean {
        return localDataSource.isFavorite(lat, lon)
    }

    override fun getAllAlerts(): Flow<List<AlertEntity>> {
        return localDataSource.getAllAlerts()
    }

    override suspend fun getAlertById(id: Long): AlertEntity? {
        return localDataSource.getAlertById(id)
    }

    override suspend fun insertAlert(alert: AlertEntity): Long {
        return localDataSource.insertAlert(alert)
    }

    override suspend fun updateAlert(alert: AlertEntity) {
        localDataSource.updateAlert(alert)
    }

    override suspend fun deleteAlert(alert: AlertEntity) {
        localDataSource.deleteAlert(alert)
    }

    override suspend fun getActiveAlerts(): List<AlertEntity> {
        return localDataSource.getActiveAlerts()
    }
}
