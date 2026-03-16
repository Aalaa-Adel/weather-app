package com.example.breez.presentation.alerts

import com.example.breez.data.db.entity.AlertEntity

sealed class AlertsUiState {

    data object Loading : AlertsUiState()

    data class Success(
        val alerts: List<AlertEntity> = emptyList(),
        val activeAlerts: List<AlertEntity> = emptyList(),
        val hasNotificationPermission: Boolean = true
    ) : AlertsUiState()

    data class Error(
        val message: String
    ) : AlertsUiState()
}