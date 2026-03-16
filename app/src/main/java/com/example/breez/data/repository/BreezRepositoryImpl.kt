package com.example.breez.data.repository

import com.example.breez.data.datasource.remote.WeatherRemoteDataSource
import com.example.breez.data.db.dao.AlertDao
import com.example.breez.data.db.dao.FavoriteDao
import com.example.breez.data.db.dao.WeatherCacheDao
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreezRepositoryImpl @Inject constructor(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val weatherCacheDao: WeatherCacheDao,
    private val favoriteDao: FavoriteDao,
    private val alertDao: AlertDao,
    private val gson: Gson
) : BreezRepository {

    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto> {
        val cacheKey = "current_${lat}_${lon}_${units}_${lang}"

        val cachedData = weatherCacheDao.getCache(cacheKey, System.currentTimeMillis())

        return try {
            val result = remoteDataSource.fetchCurrentWeather(
                lat = lat,
                lon = lon,
                units = units,
                lang = lang
            )

            if (result is ApiResult.Success) {
                val jsonData = gson.toJson(result.data)
                weatherCacheDao.insertCache(
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
                cacheToUse = weatherCacheDao.getCacheIgnoreExpiry(cacheKey)
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

        val cachedData = weatherCacheDao.getCache(cacheKey, System.currentTimeMillis())

        return try {
            val result = remoteDataSource.fetchForecast(
                lat = lat,
                lon = lon,
                units = units,
                lang = lang
            )

            if (result is ApiResult.Success) {
                val jsonData = gson.toJson(result.data)
                weatherCacheDao.insertCache(
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
                cacheToUse = weatherCacheDao.getCacheIgnoreExpiry(cacheKey)
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
        return favoriteDao.getAllFavorites()
    }

    override suspend fun getFavoriteById(id: Long): FavoriteEntity? {
        return favoriteDao.getFavoriteById(id)
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun updateFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun deleteFavorite(favorite: FavoriteEntity) {
        favoriteDao.deleteFavorite(favorite)
    }

    override suspend fun isFavorite(lat: Double, lon: Double): Boolean {
        return favoriteDao.isFavorite(lat, lon) > 0
    }

    override fun getAllAlerts(): Flow<List<AlertEntity>> {
        return alertDao.getAllAlerts()
    }

    override suspend fun getAlertById(id: Long): AlertEntity? {
        return alertDao.getAlertById(id)
    }

    override suspend fun insertAlert(alert: AlertEntity): Long {
        return alertDao.insertAlert(alert)
    }

    override suspend fun updateAlert(alert: AlertEntity) {
        alertDao.updateAlert(alert)
    }

    override suspend fun deleteAlert(alert: AlertEntity) {
        alertDao.deleteAlert(alert)
    }

    override suspend fun getActiveAlerts(): List<AlertEntity> {
        return alertDao.getActiveAlerts()
    }
}
