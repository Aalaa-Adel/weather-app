package com.example.breez.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String? = null,
    val state: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val temperature: Double? = null,
    val weatherDescription: String? = null,
    val weatherIcon: String? = null,
    val humidity: Int? = null,
    val windSpeed: Double? = null,
    val pressure: Int? = null,
    val clouds: Int? = null,
    val lastUpdated: Long? = null
)