package com.example.breez.data.datasource.local

import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

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

    suspend fun getCache(cacheKey: String, currentTime: Long): WeatherCacheEntity?
    suspend fun getCacheIgnoreExpiry(cacheKey: String): WeatherCacheEntity?
    suspend fun insertCache(cache: WeatherCacheEntity)
}
