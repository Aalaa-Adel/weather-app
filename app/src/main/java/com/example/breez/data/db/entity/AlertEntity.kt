package com.example.breez.data.db.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val startTime: Date,
    val endTime: Date,
    val alertType: AlertType,
    val isActive: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cityName: String? = null,
    val useCurrentLocation: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
){
    fun isOneTime(): Boolean = startTime.time == endTime.time
}

enum class AlertType {
    NOTIFICATION,
    ALARM
}