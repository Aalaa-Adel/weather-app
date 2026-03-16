package com.example.breez.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.breez.data.db.entity.WeatherCacheEntity

@Dao
interface WeatherCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: WeatherCacheEntity)

    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key AND expiresAt > :currentTime LIMIT 1")
    suspend fun getCache(key: String, currentTime: Long): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCacheIgnoreExpiry(key: String): WeatherCacheEntity?

    @Query("DELETE FROM weather_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredCache(currentTime: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAllCache()
}
