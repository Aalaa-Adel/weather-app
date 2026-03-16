package com.example.breez.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breez.data.datasource.preferences.AppSettings
import com.example.breez.data.datasource.preferences.AppLanguage
import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.data.datasource.preferences.SettingsPreferencesManager
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.ThemeMode
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _showMapConfirmDialog = MutableStateFlow(false)
    private val _showGpsConfirmDialog = MutableStateFlow(false)
    private val _showLocationDisabledDialog = MutableStateFlow(false)

    private val _navigateToMapPicker = MutableSharedFlow<Unit>()
    val navigateToMapPicker: SharedFlow<Unit> = _navigateToMapPicker.asSharedFlow()

    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsPreferencesManager.settingsFlow,
            _showMapConfirmDialog,
            _showGpsConfirmDialog,
            _showLocationDisabledDialog
        ) { settings, showMapDialog, showGpsDialog, showLocationDisabled ->
            SettingsUiState(
                temperatureUnit = settings.temperatureUnit,
                locationSource = settings.locationSource,
                language = settings.language,
                themeMode = settings.themeMode,
                windSpeedUnit = settings.windSpeedUnit,
                hasHomeLocation = settings.homeLat != null && settings.homeLon != null,
                showMapConfirmDialog = showMapDialog,
                showGpsConfirmDialog = showGpsDialog,
                showLocationDisabledDialog = showLocationDisabled
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState()
            )

    val themeMode: StateFlow<ThemeMode> =
        settingsPreferencesManager.settingsFlow
            .map { it.themeMode }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = settingsPreferencesManager.getCurrentSettings().themeMode
            )

    fun showMapConfirmDialog() {
        _showMapConfirmDialog.value = true
    }

    fun dismissMapConfirmDialog() {
        _showMapConfirmDialog.value = false
    }

    fun showGpsConfirmDialog() {
        if (!locationProvider.isLocationEnabled()) {
            _showLocationDisabledDialog.value = true
        } else {
            _showGpsConfirmDialog.value = true
        }
    }

    fun dismissGpsConfirmDialog() {
        _showGpsConfirmDialog.value = false
    }

    fun dismissLocationDisabledDialog() {
        _showLocationDisabledDialog.value = false
    }

    fun saveSettings(
        temperatureUnit: TemperatureUnit,
        locationSource: LocationSource,
        language: AppLanguage,
        themeMode: ThemeMode,
        windSpeedUnit: WindSpeedUnit
    ) {
        val currentSettings = settingsPreferencesManager.getCurrentSettings()

        if (locationSource == LocationSource.MAP &&
            (currentSettings.homeLat == null || currentSettings.homeLon == null)) {
            return
        }

        settingsPreferencesManager.saveSettings(
            AppSettings(
                temperatureUnit = temperatureUnit,
                locationSource = locationSource,
                language = language,
                themeMode = themeMode,
                windSpeedUnit = windSpeedUnit,
                homeLat = currentSettings.homeLat,
                homeLon = currentSettings.homeLon
            )
        )
    }

    fun saveHomeLocation(lat: Float, lon: Float) {
        val currentSettings = settingsPreferencesManager.getCurrentSettings()
        settingsPreferencesManager.saveSettings(
            currentSettings.copy(
                homeLat = lat,
                homeLon = lon
            )
        )
    }

    fun getCurrentHomeLocation(): Pair<Double?, Double?> {
        val settings = settingsPreferencesManager.getCurrentSettings()
        return Pair(settings.homeLat?.toDouble(), settings.homeLon?.toDouble())
    }

}