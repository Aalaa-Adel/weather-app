package com.example.breez.presentation.alerts

import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.AlertType
import java.util.Date

sealed class AddEditAlertUiState {

    data object Loading : AddEditAlertUiState()

    data class Form(
        val isEditing: Boolean = false,
        val alertId: Long? = null,
        val alertType: AlertType = AlertType.NOTIFICATION,
        val startTime: Date = defaultAlertTime(),
        val endTime: Date = defaultAlertTime(),
        val useCurrentLocation: Boolean = true,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val cityName: String? = null,
        val isOneTimeAlert: Boolean = true,
        val isSaving: Boolean = false,
        val timeError: String? = null,
        val locationError: String? = null
    ) : AddEditAlertUiState() {

        fun isValid(): Boolean {
            val now = Date()

            val isStartTimeValid = !startTime.before(now)
            val isEndTimeValid = isOneTimeAlert || endTime.after(startTime)
            val isLocationValid = useCurrentLocation || (latitude != null && longitude != null)

            return isStartTimeValid &&
                    isEndTimeValid &&
                    isLocationValid &&
                    timeError == null &&
                    locationError == null
        }

        fun toAlertEntity(): AlertEntity {
            return AlertEntity(
                id = alertId ?: 0,
                title = generateAlertTitle(),
                description = null,
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

        private fun generateAlertTitle(): String {
//            val typeText = when (alertType) {
//                AlertType.NOTIFICATION -> "Weather Notification"
//                AlertType.ALARM -> "Weather Alarm"
//            }

            val locationText = when {
                !cityName.isNullOrBlank() -> cityName
                useCurrentLocation -> "Current Location"
                else -> "Selected Location"
            }

            return " $locationText"
        }
    }

    data object Success : AddEditAlertUiState()

    data class Error(
        val message: String
    ) : AddEditAlertUiState()
}

private fun defaultAlertTime(): Date {
    return Date(System.currentTimeMillis() + 5 * 60 * 1000)
}