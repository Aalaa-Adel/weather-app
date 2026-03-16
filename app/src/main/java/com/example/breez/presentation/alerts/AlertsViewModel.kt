package com.example.breez.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.notification.AlertScheduler
import com.example.breez.data.notification.NotificationHelper
import com.example.breez.data.repository.BreezRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: BreezRepository,
    private val alertScheduler: AlertScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()

    private val _navigateToAddAlert = MutableSharedFlow<Unit>()
    val navigateToAddAlert: SharedFlow<Unit> = _navigateToAddAlert.asSharedFlow()

    private val _navigateToEditAlert = MutableSharedFlow<Long>()
    val navigateToEditAlert: SharedFlow<Long> = _navigateToEditAlert.asSharedFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            try {
                repository.getAllAlerts()
                    .catch { error ->
                        _uiState.value = AlertsUiState.Error(
                            message = "Failed to load alerts: ${error.message}"
                        )
                    }
                    .collect { alerts ->
                        val activeAlerts = alerts.filter { it.isActive }

                        _uiState.value = AlertsUiState.Success(
                            alerts = alerts,
                            activeAlerts = activeAlerts,
                            hasNotificationPermission = notificationHelper.hasNotificationPermission()
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = AlertsUiState.Error(
                    message = "Failed to load alerts: ${e.message}"
                )
            }
        }
    }

    fun refreshAlerts() {
        _uiState.value = AlertsUiState.Loading
        loadAlerts()
    }

    fun onAddAlertClick() {
        viewModelScope.launch {
            _navigateToAddAlert.emit(Unit)
        }
    }

    fun onEditAlertClick(alertId: Long) {
        viewModelScope.launch {
            _navigateToEditAlert.emit(alertId)
        }
    }

    fun toggleAlertStatus(alert: AlertEntity) {
        viewModelScope.launch {
            try {
                val updatedAlert = alert.copy(isActive = !alert.isActive)
                repository.updateAlert(updatedAlert)

                if (updatedAlert.isActive) {
                    alertScheduler.scheduleAlert(updatedAlert)
                    _showToast.emit("Alert activated")
                } else {
                    alertScheduler.cancelAlert(alert.id)
                    _showToast.emit("Alert deactivated")
                }
            } catch (e: Exception) {
                _showToast.emit("Failed to update alert: ${e.message}")
            }
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            try {
                alertScheduler.cancelAlert(alert.id)

                repository.deleteAlert(alert)

                _showToast.emit("Alert deleted")
            } catch (e: Exception) {
                _showToast.emit("Failed to delete alert: ${e.message}")
            }
        }
    }

    fun checkNotificationPermission() {
        val currentState = _uiState.value
        if (currentState is AlertsUiState.Success) {
            _uiState.value = currentState.copy(
                hasNotificationPermission = notificationHelper.hasNotificationPermission()
            )
        }
    }

    fun testNotification(alert: AlertEntity) {
        viewModelScope.launch {
            try {
                alertScheduler.scheduleTestNotification(alert)
                _showToast.emit("Test notification scheduled (5 seconds)")
            } catch (e: Exception) {
                _showToast.emit("Test failed: ${e.message}")
            }
        }
    }
}