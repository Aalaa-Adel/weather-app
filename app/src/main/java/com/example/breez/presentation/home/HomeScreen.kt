package com.example.breez.presentation.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.R
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.datasource.preferences.LocationSource
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.presentation.components.GlassCard
import com.example.breez.presentation.components.softOverlayColor
import com.example.breez.presentation.home.components.CurrentWeatherHero
import com.example.breez.presentation.home.components.DailyForecastSection
import com.example.breez.presentation.home.components.HomeTopBar
import com.example.breez.presentation.home.components.HourlyForecastSection
import com.example.breez.presentation.home.components.WeatherStatsCard
import com.example.breez.presentation.home.utils.SectionTitle
import com.example.breez.presentation.home.utils.formatDateTime
import com.example.breez.presentation.home.utils.rememberDailyItems
import com.example.breez.presentation.home.utils.rememberNext24HoursItems

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    val context = LocalContext.current

    WeatherScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LoadingContent()
                }

                is HomeUiState.Error -> {
                    if (state.currentWeather != null && state.forecast != null) {
                        HomeContent(
                            currentWeather = state.currentWeather,
                            forecast = state.forecast,
                            temperatureUnit = state.temperatureUnit,
                            windSpeedUnit = state.windSpeedUnit,
                            locationSource = state.locationSource,
                            isRefreshing = false,
                            isConnected = isConnected,
                            onRefresh = viewModel::refresh
                        )
                    } else {
                        ErrorContent(
                            message = state.message,
                            onRetry = viewModel::refresh
                        )
                    }
                }

                is HomeUiState.Success -> {
                    HomeContent(
                        currentWeather = state.currentWeather,
                        forecast = state.forecast,
                        temperatureUnit = state.temperatureUnit,
                        windSpeedUnit = state.windSpeedUnit,
                        locationSource = state.locationSource,
                        isRefreshing = state.isRefreshing,
                        isConnected = isConnected,
                        onRefresh = viewModel::refresh
                    )

                    if (state.showLocationSettingsDialog) {
                        LocationSettingsDialog(
                            onDismiss = { viewModel.dismissLocationDialog() },
                            onOpenSettings = {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                                viewModel.dismissLocationDialog()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(softOverlayColor())
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.loading_weather),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 30.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.something_went_wrong),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier.align(Alignment.End),
                        onClick = onRetry,
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                    ) {
                        Text(
                            text = stringResource(R.string.retry),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    currentWeather: com.example.breez.data.dto.CurrentWeatherDto,
    forecast: com.example.breez.data.dto.ForecastResponseDto,
    temperatureUnit: TemperatureUnit,
    windSpeedUnit: WindSpeedUnit,
    locationSource: LocationSource,
    isRefreshing: Boolean,
    isConnected: Boolean,
    bottomPadding: PaddingValues = PaddingValues(),
    onRefresh: () -> Unit
) {
    val next24HoursItems = rememberNext24HoursItems(forecast.list)

    val dailyItems = rememberDailyItems(
        items = forecast.list,
        timezoneOffsetSeconds = forecast.city.timezone
    )

    val selectedDayIndex = rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = bottomPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                HomeTopBar(
                    cityName = currentWeather.name,
                    onRefresh = onRefresh
                )
            }

            item {
                CurrentWeatherHero(
                    temperature = currentWeather.main.temp,
                    description = currentWeather.weather.firstOrNull()?.description.orEmpty(),
                    iconCode = currentWeather.weather.firstOrNull()?.icon.orEmpty(),
                    dateTimeText = formatDateTime(currentWeather.dt, currentWeather.timezone),
                    temperatureUnit = temperatureUnit
                )
            }

            item {
                WeatherStatsCard(
                    humidity = currentWeather.main.humidity,
                    windSpeed = currentWeather.wind.speed,
                    pressure = currentWeather.main.pressure,
                    clouds = currentWeather.clouds.all,
                    windSpeedUnit = windSpeedUnit
                )
            }

            item {
                SectionTitle(title = stringResource(R.string.next_24_hours))
            }

            item {
                HourlyForecastSection(
                    hourlyItems = next24HoursItems,
                    temperatureUnit = temperatureUnit,
                    timezoneOffsetSeconds = forecast.city.timezone
                )
            }

            item {
                SectionTitle(title = stringResource(R.string.five_day_forecast))
            }

            item {
                DailyForecastSection(
                    dailyItems = dailyItems,
                    selectedIndex = selectedDayIndex.intValue,
                    onDayClick = { clickedIndex ->
                        selectedDayIndex.intValue =
                            if (selectedDayIndex.intValue == clickedIndex) -1 else clickedIndex
                    },
                    windSpeedUnit = windSpeedUnit,
                    temperatureUnit = temperatureUnit
                )
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        AnimatedVisibility(
            visible = !isConnected,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            OfflineBanner()
        }
    }
}

@Composable
private fun OfflineBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1E1E).copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Cloud,
                contentDescription = stringResource(R.string.cd_offline),
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.no_internet_connection),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LocationSettingsDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.GridView,
                contentDescription = stringResource(R.string.cd_location),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.location_services_disabled),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.enable_location_for_current_weather),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}