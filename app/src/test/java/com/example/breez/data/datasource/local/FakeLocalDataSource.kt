package com.example.breez.data.datasource.local

import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


class FakeLocalDataSource(
    val favoritesList: MutableList<FavoriteEntity>? = mutableListOf(),
    val alertsList: MutableList<AlertEntity>? = mutableListOf(),
    val cacheMap: MutableMap<String, WeatherCacheEntity>? = mutableMapOf()
) : LocalDataSource {


    override fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        return flowOf(favoritesList ?: emptyList())
    }

    override suspend fun getFavoriteById(id: Long): FavoriteEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateFavorite(favorite: FavoriteEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavorite(favorite: FavoriteEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun isFavorite(lat: Double, lon: Double): Boolean {
        return favoritesList?.any { it.latitude == lat && it.longitude == lon } ?: false
    }


    override fun getAllAlerts(): Flow<List<AlertEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAlertById(id: Long): AlertEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlert(alert: AlertEntity): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateAlert(alert: AlertEntity) {
        TODO("Not yet implemented")

    }

    override suspend fun deleteAlert(alert: AlertEntity) {
        alertsList?.remove(alert)
    }

    override suspend fun getActiveAlerts(): List<AlertEntity> {
        TODO("Not yet implemented")
    }


    override suspend fun getCache(cacheKey: String, currentTime: Long): WeatherCacheEntity? {
        TODO("Not yet implemented")

    }

    override suspend fun getCacheIgnoreExpiry(cacheKey: String): WeatherCacheEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun insertCache(cache: WeatherCacheEntity) {
        TODO("Not yet implemented")
    }
}
