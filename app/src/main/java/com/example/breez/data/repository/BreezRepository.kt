package com.example.breez.data.repository

import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface BreezRepository {
    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto>

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<ForecastResponseDto>

    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    suspend fun getFavoriteById(id: Long): FavoriteEntity?
    suspend fun insertFavorite(favorite: FavoriteEntity)
    suspend fun updateFavorite(favorite: FavoriteEntity)
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    suspend fun isFavorite(lat: Double, lon: Double): Boolean
    fun getAllAlerts(): Flow<List<AlertEntity>>
    suspend fun getAlertById(id: Long): AlertEntity?
    suspend fun insertAlert(alert: AlertEntity): Long
    suspend fun updateAlert(alert: AlertEntity)
    suspend fun deleteAlert(alert: AlertEntity)
    suspend fun getActiveAlerts(): List<AlertEntity>
    suspend fun getCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>>
    suspend fun getLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>>
}