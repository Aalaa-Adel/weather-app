package com.example.breez.data.datasource.local

import com.example.breez.data.db.dao.AlertDao
import com.example.breez.data.db.dao.FavoriteDao
import com.example.breez.data.db.dao.WeatherCacheDao
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val alertDao: AlertDao,
    private val weatherCacheDao: WeatherCacheDao
) : LocalDataSource {

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

    override suspend fun getCache(cacheKey: String, currentTime: Long): WeatherCacheEntity? {
        return weatherCacheDao.getCache(cacheKey, currentTime)
    }

    override suspend fun getCacheIgnoreExpiry(cacheKey: String): WeatherCacheEntity? {
        return weatherCacheDao.getCacheIgnoreExpiry(cacheKey)
    }

    override suspend fun insertCache(cache: WeatherCacheEntity) {
        weatherCacheDao.insertCache(cache)
    }
}
