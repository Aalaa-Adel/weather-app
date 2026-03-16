package com.example.breez.presentation.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.breez.data.db.entity.AlertType
import com.example.breez.data.location.LocationProvider
import com.example.breez.data.notification.AlertScheduler
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.util.ApiResult
import com.example.breez.presentation.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEditAlertViewModel @Inject constructor(
    private val repository: BreezRepository,
    private val alertScheduler: AlertScheduler,
    private val locationProvider: LocationProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alertId: Long? = savedStateHandle
        .toRoute<AppRoute.AddEditAlertRoute>()
        .alertId

    private val _uiState = MutableStateFlow<AddEditAlertUiState>(
        if (alertId != null) AddEditAlertUiState.Loading else AddEditAlertUiState.Form()
    )
    val uiState: StateFlow<AddEditAlertUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()

    init {
        if (alertId != null) loadAlert(alertId) else prefetchLocation()
    }


    private fun loadAlert(id: Long) {
        viewModelScope.launch {
            try {
                val alert = repository.getAlertById(id)
                if (alert != null) {
                    _uiState.value = AddEditAlertUiState.Form(
                        isEditing = true,
                        alertId = alert.id,
                        title = alert.title,
                        description = alert.description ?: "",
                        alertType = alert.alertType,
                        startTime = alert.startTime,
                        endTime = alert.endTime,
                        useCurrentLocation = alert.useCurrentLocation,
                        latitude = alert.latitude,
                        longitude = alert.longitude,
                        cityName = alert.cityName,
                        isOneTimeAlert = alert.isOneTime()
                    )
                } else {
                    _uiState.value = AddEditAlertUiState.Error("Alert not found")
                }
            } catch (e: Exception) {
                _uiState.value = AddEditAlertUiState.Error("Failed to load alert: ${e.message}")
            }
        }
    }


    private fun prefetchLocation() {
        viewModelScope.launch {
            try {
                val loc = locationProvider.getCurrentLocation() ?: return@launch
                val geo = repository.getLocationNameFromCoordinates(loc.first, loc.second)
                val city =
                    (geo as? ApiResult.Success)?.data?.firstOrNull()?.name ?: "Current Location"
                withForm {
                    if (useCurrentLocation)
                        copy(latitude = loc.first, longitude = loc.second, cityName = city)
                    else
                        this
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateTitle(title: String) = withForm {
        copy(title = title, titleError = if (title.isBlank()) "Title is required" else null)
    }

    fun updateDescription(desc: String) = withForm { copy(description = desc) }

    fun updateAlertType(type: AlertType) = withForm { copy(alertType = type) }

    fun updateStartTime(startTime: Date) = withForm {
        val end = if (isOneTimeAlert) startTime else endTime
        copy(
            startTime = startTime,
            endTime = end,
            timeError = validate(startTime, end, isOneTimeAlert)
        )
    }

    fun updateEndTime(endTime: Date) = withForm {
        copy(endTime = endTime, timeError = validate(startTime, endTime, isOneTimeAlert))
    }

    fun toggleAlertMode(isOneTime: Boolean) = withForm {
        val end = if (isOneTime) startTime else endTime
        copy(
            isOneTimeAlert = isOneTime,
            endTime = end,
            timeError = validate(startTime, end, isOneTime)
        )
    }

    fun toggleLocationSource(useCurrent: Boolean) {
        withForm { copy(useCurrentLocation = useCurrent, locationError = null) }
        if (useCurrent) fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                val loc = locationProvider.getCurrentLocation()
                if (loc != null) {
                    val geo = repository.getLocationNameFromCoordinates(loc.first, loc.second)
                    val city =
                        (geo as? ApiResult.Success)?.data?.firstOrNull()?.name ?: "Current Location"
                    withForm {
                        copy(
                            latitude = loc.first,
                            longitude = loc.second,
                            cityName = city,
                            locationError = null
                        )
                    }
                } else {
                    withForm { copy(locationError = "Unable to get current location. Make sure GPS is enabled.") }
                }
            } catch (e: Exception) {
                withForm { copy(locationError = "Location error: ${e.message}") }
            }
        }
    }

    fun saveAlert() {
        val state = _uiState.value as? AddEditAlertUiState.Form ?: return
        if (!state.isValid()) return

        viewModelScope.launch {
            withForm { copy(isSaving = true) }
            try {
                val entity = state.toAlertEntity()
                val savedId = if (state.isEditing) {
                    alertScheduler.cancelAlert(entity.id)
                    repository.updateAlert(entity)
                    entity.id
                } else {
                    repository.insertAlert(entity)
                }
                alertScheduler.scheduleAlert(entity.copy(id = savedId))
                _uiState.value = AddEditAlertUiState.Success
                _showToast.emit(if (state.isEditing) "Alert updated" else "Alert created")
                _navigateBack.emit(Unit)
            } catch (e: Exception) {
                _uiState.value = AddEditAlertUiState.Error("Failed to save: ${e.message}")
            }
        }
    }


    private fun withForm(block: AddEditAlertUiState.Form.() -> AddEditAlertUiState.Form) {
        val s = _uiState.value
        if (s is AddEditAlertUiState.Form) _uiState.value = s.block()
    }

    private fun validate(start: Date, end: Date, isOneTime: Boolean): String? {
        if (start.before(Date())) return "Start time cannot be in the past"
        if (!isOneTime && end.before(start)) return "End time must be after start time"
        return null
    }
}