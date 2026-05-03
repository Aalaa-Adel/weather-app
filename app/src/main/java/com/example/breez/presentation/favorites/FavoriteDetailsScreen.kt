package com.example.breez.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.R
import com.example.breez.WeatherScreenBackground
import com.example.breez.presentation.components.GlassCard
import com.example.breez.presentation.favorites.components.FavoriteDetailsTopBar
import com.example.breez.presentation.home.components.CurrentWeatherHero
import com.example.breez.presentation.home.components.DailyForecastSection
import com.example.breez.presentation.home.components.HourlyForecastSection
import com.example.breez.presentation.home.components.WeatherStatsCard
import com.example.breez.presentation.home.utils.SectionTitle
import com.example.breez.presentation.home.utils.formatDateTime
import com.example.breez.presentation.home.utils.rememberDailyItems
import com.example.breez.presentation.home.utils.rememberNext24HoursItems

@Composable
fun FavoriteDetailsScreen(
    lat: Double,
    lon: Double,
    cityName: String,
    favoriteId: Long? = null,
    onBackClick: () -> Unit,
    viewModel: FavoriteDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(lat, lon, favoriteId) {
        viewModel.loadWeatherData(lat, lon, favoriteId)
    }

    WeatherScreenBackground {
        when (val state = uiState) {
            is FavoriteDetailsUiState.Loading -> {
                FavoriteDetailsLoadingContent()
            }

            is FavoriteDetailsUiState.Error -> {
                FavoriteDetailsErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadWeatherData(lat, lon, favoriteId) },
                    onBack = onBackClick
                )
            }

            is FavoriteDetailsUiState.Success -> {
                FavoriteDetailsContent(
                    cityName = cityName,
                    currentWeather = state.currentWeather,
                    forecast = state.forecast,
                    temperatureUnit = state.temperatureUnit,
                    windSpeedUnit = state.windSpeedUnit,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun FavoriteDetailsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.padding(24.dp),
            cornerRadius = 30.dp
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun FavoriteDetailsErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            cornerRadius = 30.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.favorite_error_loading_weather),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onBack) {
                        Text(stringResource(R.string.back))
                    }
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteDetailsContent(
    cityName: String,
    currentWeather: com.example.breez.data.dto.CurrentWeatherDto,
    forecast: com.example.breez.data.dto.ForecastResponseDto,
    temperatureUnit: com.example.breez.data.datasource.preferences.TemperatureUnit,
    windSpeedUnit: com.example.breez.data.datasource.preferences.WindSpeedUnit,
    onBackClick: () -> Unit
) {
    val next24HoursItems = remember(forecast.list) {
        rememberNext24HoursItems(forecast.list)
    }

    val dailyItems = remember(forecast.list, forecast.city.timezone) {
        rememberDailyItems(
            items = forecast.list,
            timezoneOffsetSeconds = forecast.city.timezone
        )
    }

    val selectedDayIndex = remember { mutableIntStateOf(-1) }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            FavoriteDetailsTopBar(
                cityName = cityName,
                onBackClick = onBackClick
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
}