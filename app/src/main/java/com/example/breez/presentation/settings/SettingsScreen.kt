package com.example.breez.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    bottomPadding: PaddingValues = PaddingValues(),
    onNavigateToPickLocation: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    // Auto-save when any setting changes
    LaunchedEffect(
        selectedTemperatureState.value,
        selectedLocationState.value,
        selectedLanguageState.value,
        selectedThemeState.value,
        selectedWindSpeedState.value
    ) {
        // Skip initial save on first composition
        if (selectedTemperatureState.value != uiState.temperatureUnit ||
            selectedLocationState.value != uiState.locationSource ||
            selectedLanguageState.value != uiState.language ||
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

    WeatherScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 80.dp + bottomPadding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SettingsTopBar(
                    onBackClick = onBackClick
                )

                SettingsHeroCard()

                SettingsSectionCard(
                    title = "Temperature Unit",
                    subtitle = "Choose how temperature is displayed",
                    icon = Icons.Outlined.Thermostat
                ) {
                    SettingsRadioItem(
                        title = "Celsius",
                        subtitle = "Best for metric weather data",
                        selected = selectedTemperatureState.value == TemperatureUnit.CELSIUS,
                        onClick = { selectedTemperatureState.value = TemperatureUnit.CELSIUS }
                    )

                    SettingsRadioItem(
                        title = "Fahrenheit",
                        subtitle = "Common for US-style weather display",
                        selected = selectedTemperatureState.value == TemperatureUnit.FAHRENHEIT,
                        onClick = { selectedTemperatureState.value = TemperatureUnit.FAHRENHEIT }
                    )

                    SettingsRadioItem(
                        title = "Kelvin",
                        subtitle = "Raw scientific temperature unit",
                        selected = selectedTemperatureState.value == TemperatureUnit.KELVIN,
                        onClick = { selectedTemperatureState.value = TemperatureUnit.KELVIN }
                    )
                }

                SettingsSectionCard(
                    title = "Location Source",
                    subtitle = "Choose how the app gets your location",
                    icon = Icons.Outlined.MyLocation
                ) {
                    SettingsRadioItem(
                        title = "GPS",
                        subtitle = "Use your live device location",
                        selected = selectedLocationState.value == LocationSource.GPS,
                        onClick = {
                            viewModel.showGpsConfirmDialog()
                        }
                    )

                    SettingsRadioItem(
                        title = "Map",
                        subtitle = "Pick a location manually",
                        selected = selectedLocationState.value == LocationSource.MAP,
                        onClick = {
                            viewModel.showMapConfirmDialog()
                        }
                    )

                    androidx.compose.animation.AnimatedVisibility(visible = selectedLocationState.value == LocationSource.MAP) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onNavigateToPickLocation) {
                                Text(
                                    text = "Pick Home Location",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                SettingsSectionCard(
                    title = "Language",
                    subtitle = "Choose the app language",
                    icon = Icons.Outlined.Language
                ) {
                    SettingsRadioItem(
                        title = "Arabic",
                        subtitle = "واجهة عربية",
                        selected = selectedLanguageState.value == AppLanguage.ARABIC,
                        onClick = { selectedLanguageState.value = AppLanguage.ARABIC }
                    )

                    SettingsRadioItem(
                        title = "English",
                        subtitle = "English interface",
                        selected = selectedLanguageState.value == AppLanguage.ENGLISH,
                        onClick = { selectedLanguageState.value = AppLanguage.ENGLISH }
                    )
                }

                SettingsSectionCard(
                    title = "Theme Mode",
                    subtitle = "Control the app appearance",
                    icon = Icons.Outlined.DarkMode
                ) {
                    SettingsRadioItem(
                        title = "System Default",
                        subtitle = "Follow device appearance",
                        selected = selectedThemeState.value == ThemeMode.SYSTEM,
                        onClick = { selectedThemeState.value = ThemeMode.SYSTEM }
                    )

                    SettingsRadioItem(
                        title = "Light",
                        subtitle = "Bright and clean look",
                        selected = selectedThemeState.value == ThemeMode.LIGHT,
                        onClick = { selectedThemeState.value = ThemeMode.LIGHT }
                    )

                    SettingsRadioItem(
                        title = "Dark",
                        subtitle = "Deeper low-light style",
                        selected = selectedThemeState.value == ThemeMode.DARK,
                        onClick = { selectedThemeState.value = ThemeMode.DARK }
                    )
                }

                SettingsSectionCard(
                    title = "Wind Speed Unit",
                    subtitle = "Choose the unit for wind speed",
                    icon = Icons.Outlined.Air
                ) {
                    SettingsRadioItem(
                        title = "Meters/Second (m/s)",
                        subtitle = "Metric system unit",
                        selected = selectedWindSpeedState.value == WindSpeedUnit.METERS_PER_SECOND,
                        onClick = { selectedWindSpeedState.value = WindSpeedUnit.METERS_PER_SECOND }
                    )

                    SettingsRadioItem(
                        title = "Kilometers/Hour (km/h)",
                        subtitle = "Common in most countries",
                        selected = selectedWindSpeedState.value == WindSpeedUnit.KILOMETERS_PER_HOUR,
                        onClick = { selectedWindSpeedState.value = WindSpeedUnit.KILOMETERS_PER_HOUR }
                    )

                    SettingsRadioItem(
                        title = "Miles/Hour (mph)",
                        subtitle = "Common for U.S. system",
                        selected = selectedWindSpeedState.value == WindSpeedUnit.MILES_PER_HOUR,
                        onClick = { selectedWindSpeedState.value = WindSpeedUnit.MILES_PER_HOUR }
                    )
                }
            }
        }

        // Map Confirm Dialog
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

        // GPS Confirm Dialog
        if (uiState.showGpsConfirmDialog) {
            GpsConfirmDialog(
                onDismiss = { viewModel.dismissGpsConfirmDialog() },
                onProceed = {
                    selectedLocationState.value = LocationSource.GPS
                    viewModel.dismissGpsConfirmDialog()
                }
            )
        }

        // Location Disabled Dialog
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

@Composable
private fun SettingsTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconButton(
            onClick = onBackClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Customize your Breez experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
    }
}

@Composable
private fun SettingsHeroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = settingsGlassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = settingsGlassBorderColor(),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                settingsSoftOverlayColor(),
                                settingsSoftOverlayColor().copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Personalize the app your way",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Control temperature units, location source, app language, and appearance mode from one clean place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = settingsGlassSurfaceColor(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = settingsGlassBorderColor(),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(settingsSoftOverlayColor()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun SettingsRadioItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selected) 1.dp else 0.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
                )
            }
        }
    }
}

@Composable
private fun SettingsIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = settingsGlassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .border(
                    width = 1.dp,
                    color = settingsGlassBorderColor(),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
private fun settingsGlassSurfaceColor(): Color {
    return MaterialTheme.colorScheme.surface.copy(
        alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.22f else 0.74f
    )
}

@Composable
private fun settingsGlassBorderColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    }
}

@Composable
private fun settingsSoftOverlayColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }
}

@Composable
private fun MapConfirmDialog(
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "Map",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Pick from Map?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Open the interactive map to precisely select your weather location. This allows you to choose any spot worldwide.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onProceed) {
                Text("PROCEED", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun GpsConfirmDialog(
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "GPS",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Use GPS?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Text(
                text = "The app will use your device's GPS to automatically detect and update your weather location. Do you want to enable this?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onProceed) {
                Text("PROCEED", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun LocationDisabledDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "Location Disabled",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Location is Disabled",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Please enable location services in your device settings to use GPS automatically.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}