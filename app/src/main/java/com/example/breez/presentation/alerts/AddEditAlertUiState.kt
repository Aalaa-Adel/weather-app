package com.example.breez.presentation.alerts

import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.AlertType
import java.util.Date
import kotlin.takeIf
import kotlin.text.isNotBlank
import kotlin.text.trim
sealed class AddEditAlertUiState {
    data object Loading : AddEditAlertUiState()

    data class Form(
        val isEditing: Boolean = false,
        val alertId: Long? = null,
        val title: String = "",
        val description: String = "",
        val alertType: AlertType = AlertType.NOTIFICATION,
        val startTime: Date = Date(),
        val endTime: Date = Date(),
        val useCurrentLocation: Boolean = true,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val cityName: String? = null,
        val isOneTimeAlert: Boolean = true,
        val isSaving: Boolean = false,
        val titleError: String? = null,
        val timeError: String? = null,
        val locationError: String? = null
    ) : AddEditAlertUiState() {
        fun isValid(): Boolean {
            return title.isNotBlank() && 
                   titleError == null && 
                   timeError == null && 
                   locationError == null &&
                   (useCurrentLocation || (latitude != null && longitude != null))
        }
        fun toAlertEntity(): AlertEntity {
            return AlertEntity(
                id = alertId ?: 0,
                title = title.trim(),
                description = description.trim().takeIf { it.isNotBlank() },
                startTime = startTime,
                endTime = if (isOneTimeAlert) startTime else endTime,
                alertType = alertType,
                isActive = true,
                latitude = if (useCurrentLocation) null else latitude,
                longitude = if (useCurrentLocation) null else longitude,
                cityName = cityName,
                useCurrentLocation = useCurrentLocation
            )
        }
    }

    data object Success : AddEditAlertUiState()


    data class Error(
        val message: String
    ) : AddEditAlertUiState()
}