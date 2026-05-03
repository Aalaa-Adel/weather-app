package com.example.breez.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.provider.Settings
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.datasource.preferences.AppLanguage
import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.ThemeMode
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.presentation.settings.componets.GpsConfirmDialog
import com.example.breez.presentation.settings.componets.LocationDisabledDialog
import com.example.breez.presentation.settings.componets.MapConfirmDialog
import com.example.breez.presentation.settings.componets.SettingsHeroCard
import com.example.breez.presentation.settings.componets.SettingsRadioItem
import com.example.breez.presentation.settings.componets.SettingsSectionCard
import com.example.breez.presentation.settings.componets.SettingsTopBar
import com.example.breez.utils.LocalizationHelper
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.res.stringResource
import com.example.breez.R

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    bottomPadding: PaddingValues = PaddingValues(),
    onNavigateToPickLocation: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    val selectedTemperatureState = remember(uiState.temperatureUnit) {
        mutableStateOf(uiState.temperatureUnit)
    }
    val selectedLocationState = remember(uiState.locationSource) {
        mutableStateOf(uiState.locationSource)
    }
    val selectedLanguageState = remember(uiState.language) {
        mutableStateOf(uiState.language)
    }
    val selectedThemeState = remember(uiState.themeMode) {
        mutableStateOf(uiState.themeMode)
    }
    val selectedWindSpeedState = remember(uiState.windSpeedUnit) {
        mutableStateOf(uiState.windSpeedUnit)
    }
    val expandedSection = remember { mutableStateOf("theme") }

    LaunchedEffect(
        selectedTemperatureState.value,
        selectedLocationState.value,
        selectedThemeState.value,
        selectedWindSpeedState.value
    ) {
        if (selectedTemperatureState.value != uiState.temperatureUnit ||
            selectedLocationState.value != uiState.locationSource ||
            selectedThemeState.value != uiState.themeMode ||
            selectedWindSpeedState.value != uiState.windSpeedUnit
        ) {
            viewModel.saveSettings(
                temperatureUnit = selectedTemperatureState.value,
                locationSource = selectedLocationState.value,
                language = selectedLanguageState.value,
                themeMode = selectedThemeState.value,
                windSpeedUnit = selectedWindSpeedState.value
            )
        }
    }

    LaunchedEffect(selectedLanguageState.value) {
        if (selectedLanguageState.value != uiState.language) {
            viewModel.saveSettings(
                temperatureUnit = selectedTemperatureState.value,
                locationSource = selectedLocationState.value,
                language = selectedLanguageState.value,
                themeMode = selectedThemeState.value,
                windSpeedUnit = selectedWindSpeedState.value
            )

            LocalizationHelper.saveLanguageToPrefs(
                context,
                selectedLanguageState.value
            )

            activity?.recreate()
        }
    }

    WeatherScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 80.dp + bottomPadding.calculateBottomPadding())
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SettingsTopBar(onBackClick = onBackClick)

                SettingsHeroCard()

                SettingsSectionCard(
                    title = stringResource(R.string.temperature_unit),
                    subtitle = stringResource(R.string.choose_temperature_display),
                    icon = Icons.Outlined.Thermostat,
                    expanded = expandedSection.value == "temperature",
                    onToggleExpanded = {
                        expandedSection.value =
                            if (expandedSection.value == "temperature") "" else "temperature"
                    }
                ) {
                    SettingsRadioItem(
                        title = stringResource(R.string.celsius),
                        subtitle = stringResource(R.string.celsius_desc),
                        selected = selectedTemperatureState.value == TemperatureUnit.CELSIUS,
                        onClick = { selectedTemperatureState.value = TemperatureUnit.CELSIUS }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.fahrenheit),
                        subtitle = stringResource(R.string.fahrenheit_desc),
                        selected = selectedTemperatureState.value == TemperatureUnit.FAHRENHEIT,
                        onClick = { selectedTemperatureState.value = TemperatureUnit.FAHRENHEIT }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.kelvin),
                        subtitle = stringResource(R.string.kelvin_desc),
                        selected = selectedTemperatureState.value == TemperatureUnit.KELVIN,
                        onClick = { selectedTemperatureState.value = TemperatureUnit.KELVIN }
                    )
                }

                SettingsSectionCard(
                    title = stringResource(R.string.location_source),
                    subtitle = stringResource(R.string.choose_location_source),
                    icon = Icons.Outlined.MyLocation,
                    expanded = expandedSection.value == "location",
                    onToggleExpanded = {
                        expandedSection.value =
                            if (expandedSection.value == "location") "" else "location"
                    }
                ) {
                    SettingsRadioItem(
                        title = stringResource(R.string.gps),
                        subtitle = stringResource(R.string.gps_desc),
                        selected = selectedLocationState.value == LocationSource.GPS,
                        onClick = { viewModel.showGpsConfirmDialog() }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.map),
                        subtitle = stringResource(R.string.map_desc),
                        selected = selectedLocationState.value == LocationSource.MAP,
                        onClick = { viewModel.showMapConfirmDialog() }
                    )

                    AnimatedVisibility(
                        visible = selectedLocationState.value == LocationSource.MAP
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onNavigateToPickLocation) {
                                Text(
                                    text = stringResource(R.string.pick_home_location),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                SettingsSectionCard(
                    title = stringResource(R.string.language),
                    subtitle = stringResource(R.string.choose_app_language),
                    icon = Icons.Outlined.Language,
                    expanded = expandedSection.value == "language",
                    onToggleExpanded = {
                        expandedSection.value =
                            if (expandedSection.value == "language") "" else "language"
                    }
                ) {
                    SettingsRadioItem(
                        title = stringResource(R.string.arabic),
                        subtitle = stringResource(R.string.arabic_desc),
                        selected = selectedLanguageState.value == AppLanguage.ARABIC,
                        onClick = { selectedLanguageState.value = AppLanguage.ARABIC }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.english),
                        subtitle = stringResource(R.string.english_desc),
                        selected = selectedLanguageState.value == AppLanguage.ENGLISH,
                        onClick = { selectedLanguageState.value = AppLanguage.ENGLISH }
                    )
                }

                SettingsSectionCard(
                    title = stringResource(R.string.theme_mode),
                    subtitle = stringResource(R.string.control_app_appearance),
                    icon = Icons.Outlined.DarkMode,
                    expanded = expandedSection.value == "theme",
                    onToggleExpanded = {
                        expandedSection.value =
                            if (expandedSection.value == "theme") "" else "theme"
                    }
                ) {
                    SettingsRadioItem(
                        title = stringResource(R.string.system_default),
                        subtitle = stringResource(R.string.system_default_desc),
                        selected = selectedThemeState.value == ThemeMode.SYSTEM,
                        onClick = { selectedThemeState.value = ThemeMode.SYSTEM }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.light),
                        subtitle = stringResource(R.string.light_desc),
                        selected = selectedThemeState.value == ThemeMode.LIGHT,
                        onClick = { selectedThemeState.value = ThemeMode.LIGHT }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.dark),
                        subtitle = stringResource(R.string.dark_desc),
                        selected = selectedThemeState.value == ThemeMode.DARK,
                        onClick = { selectedThemeState.value = ThemeMode.DARK }
                    )
                }

                SettingsSectionCard(
                    title = stringResource(R.string.wind_speed_unit),
                    subtitle = stringResource(R.string.choose_wind_speed_unit),
                    icon = Icons.Outlined.Air,
                    expanded = expandedSection.value == "wind",
                    onToggleExpanded = {
                        expandedSection.value =
                            if (expandedSection.value == "wind") "" else "wind"
                    }
                ) {
                    SettingsRadioItem(
                        title = stringResource(R.string.meters_per_second),
                        subtitle = stringResource(R.string.meters_per_second_desc),
                        selected = selectedWindSpeedState.value == WindSpeedUnit.METERS_PER_SECOND,
                        onClick = { selectedWindSpeedState.value = WindSpeedUnit.METERS_PER_SECOND }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.kilometers_per_hour),
                        subtitle = stringResource(R.string.kilometers_per_hour_desc),
                        selected = selectedWindSpeedState.value == WindSpeedUnit.KILOMETERS_PER_HOUR,
                        onClick = { selectedWindSpeedState.value = WindSpeedUnit.KILOMETERS_PER_HOUR }
                    )

                    SettingsRadioItem(
                        title = stringResource(R.string.miles_per_hour),
                        subtitle = stringResource(R.string.miles_per_hour_desc),
                        selected = selectedWindSpeedState.value == WindSpeedUnit.MILES_PER_HOUR,
                        onClick = { selectedWindSpeedState.value = WindSpeedUnit.MILES_PER_HOUR }
                    )
                }
            }
        }

        if (uiState.showMapConfirmDialog) {
            MapConfirmDialog(
                onDismiss = { viewModel.dismissMapConfirmDialog() },
                onProceed = {
                    selectedLocationState.value = LocationSource.MAP
                    viewModel.dismissMapConfirmDialog()
                    onNavigateToPickLocation()
                }
            )
        }

        if (uiState.showGpsConfirmDialog) {
            GpsConfirmDialog(
                onDismiss = { viewModel.dismissGpsConfirmDialog() },
                onProceed = {
                    selectedLocationState.value = LocationSource.GPS
                    viewModel.dismissGpsConfirmDialog()
                }
            )
        }

        if (uiState.showLocationDisabledDialog) {
            LocationDisabledDialog(
                onDismiss = { viewModel.dismissLocationDisabledDialog() },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                    viewModel.dismissLocationDisabledDialog()
                }
            )
        }
    }
}