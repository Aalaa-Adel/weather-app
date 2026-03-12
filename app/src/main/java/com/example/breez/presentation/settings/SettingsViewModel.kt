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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferencesManager: SettingsPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        settingsPreferencesManager.settingsFlow
            .map { settings ->
                SettingsUiState(
                    temperatureUnit = settings.temperatureUnit,
                    locationSource = settings.locationSource,
                    language = settings.language,
                    themeMode = settings.themeMode,
                    windSpeedUnit = settings.windSpeedUnit
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

    fun saveSettings(
        temperatureUnit: TemperatureUnit,
        locationSource: LocationSource,
        language: AppLanguage,
        themeMode: ThemeMode,
        windSpeedUnit: WindSpeedUnit
    ) {
        settingsPreferencesManager.saveSettings(
            AppSettings(
                temperatureUnit = temperatureUnit,
                locationSource = locationSource,
                language = language,
                themeMode = themeMode,
                windSpeedUnit = windSpeedUnit
            )
        )
    }
}