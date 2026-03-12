package com.example.breez.presentation.settings

import com.example.breez.data.datasource.preferences.AppLanguage
import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.ThemeMode
import com.example.breez.data.datasource.preferences.WindSpeedUnit


data class SettingsUiState(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val locationSource: LocationSource = LocationSource.GPS,
    val language: AppLanguage = AppLanguage.ARABIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND

)