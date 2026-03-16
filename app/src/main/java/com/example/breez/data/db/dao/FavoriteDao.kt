package com.example.breez.data.db.dao

import androidx.room.*
import com.example.breez.data.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteById(id: Long): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("SELECT COUNT(*) FROM favorites WHERE latitude = :lat AND longitude = :lon")
    suspend fun isFavorite(lat: Double, lon: Double): Int
}