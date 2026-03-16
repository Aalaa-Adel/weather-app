package com.example.breez.presentation.home

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.dto.ForecastItemDto
import com.example.breez.data.datasource.preferences.LocationSource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    val context = LocalContext.current

    WeatherScreenBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "Loading weather...",
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
        Box(
            contentAlignment = Alignment.Center
        ) {
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
                        text = "Something went wrong",
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
                            text = "Retry",
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
                SectionTitle(title = "Next 24 Hours")
            }

            item {
                HourlyForecastSection(
                    hourlyItems = next24HoursItems,
                    temperatureUnit = temperatureUnit
                )
            }

            item {
                SectionTitle(title = "5-Day Forecast")
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
                contentDescription = "Offline",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "no internet connection",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    cityName: String,
    onRefresh: () -> Unit
) {
    val refreshRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassIconButton(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                )

                Text(
                    text = cityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            GlassIconButton(
                onClick = {
                    scope.launch {
                        refreshRotation.snapTo(0f)
                        refreshRotation.animateTo(
                            targetValue = 360f,
                            animationSpec = tween(700, easing = LinearEasing)
                        )
                    }
                    onRefresh()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.rotate(refreshRotation.value)
                    )
                }
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = glassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = 1.dp,
                    color = glassBorderColor(),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
private fun CurrentWeatherHero(
    temperature: Double,
    description: String,
    iconCode: String,
    dateTimeText: String,
    temperatureUnit: TemperatureUnit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    val fadeIn = remember { Animatable(0f) }
    val scaleIn = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        fadeIn.animateTo(
            targetValue = 1f,
            animationSpec = tween(850, easing = FastOutSlowInEasing)
        )
        scaleIn.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 240f)
        )
    }

    val temperatureDisplay = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "${temperature.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${temperature.toInt()}°F"
        TemperatureUnit.KELVIN -> "${temperature.toInt()} K"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = fadeIn.value
                scaleX = scaleIn.value
                scaleY = scaleIn.value
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = glassSurfaceColor(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = glassBorderColor(),
                        shape = RoundedCornerShape(34.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(haloScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        softOverlayColor(),
                                        softOverlayColor().copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .offset(y = floatOffset.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = weatherIconUrl(iconCode),
                            contentDescription = description,
                            modifier = Modifier.size(148.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = temperatureDisplay,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun WeatherStatsCard(
    humidity: Int,
    windSpeed: Double,
    pressure: Int,
    clouds: Int,
    windSpeedUnit: WindSpeedUnit
) {
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            glassSurfaceColor(),
            glassSurfaceColor().copy(alpha = 0.82f)
        )
    )

    val convertedWindSpeed = when (windSpeedUnit) {
        WindSpeedUnit.METERS_PER_SECOND -> "${windSpeed.toInt()} m/s"
        WindSpeedUnit.KILOMETERS_PER_HOUR -> String.format("%.2f", (windSpeed * 3.6)) + " km/h"
        WindSpeedUnit.MILES_PER_HOUR -> String.format("%.2f", (windSpeed * 2.23694)) + " mph"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(cardBrush)
            .border(
                width = 1.dp,
                color = glassBorderColor(),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 14.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            iconTint = Color(0xFF61D3FF),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = "Humidity",
                    tint = Color(0xFF61D3FF)
                )
            },
            title = "Humidity",
            value = "$humidity%"
        )

        StatItem(
            iconTint = Color(0xFF9EC5FF),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Air,
                    contentDescription = "Wind",
                    tint = Color(0xFF9EC5FF)
                )
            },
            title = "Wind",
            value = convertedWindSpeed
        )

        StatItem(
            iconTint = Color(0xFFFFD56A),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = "Pressure",
                    tint = Color(0xFFFFD56A)
                )
            },
            title = "Pressure",
            value = "$pressure"
        )

        StatItem(
            iconTint = Color(0xFFC9D4F7),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.WbCloudy,
                    contentDescription = "Clouds",
                    tint = Color(0xFFC9D4F7)
                )
            },
            title = "Clouds",
            value = "$clouds%"
        )
    }
}

@Composable
private fun StatItem(
    iconTint: Color,
    icon: @Composable () -> Unit,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun HourlyForecastSection(
    hourlyItems: List<ForecastItemDto>,
    temperatureUnit: TemperatureUnit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(hourlyItems) { index, item ->
            HourlyForecastCard(
                item = item,
                isHighlighted = index == 0,
                temperatureUnit = temperatureUnit
            )
        }
    }
}

@Composable
private fun HourlyForecastCard(
    item: ForecastItemDto,
    isHighlighted: Boolean,
    temperatureUnit: TemperatureUnit
) {
    val backgroundBrush = if (isHighlighted) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                glassSurfaceColor(),
                glassSurfaceColor().copy(alpha = 0.86f)
            )
        )
    }
    val temperatureDisplay = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "${item.main.temp.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${item.main.temp.toInt()}°F"
        TemperatureUnit.KELVIN -> "${item.main.temp.toInt()} K"
    }


    Box(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                } else {
                    glassBorderColor()
                },
                shape = RoundedCornerShape(26.dp)
            )
            .padding(vertical = 16.dp, horizontal = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatHour(item.dt),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
                fontWeight = FontWeight.Medium
            )

            AsyncImage(
                model = weatherIconUrl(item.weather.firstOrNull()?.icon.orEmpty()),
                contentDescription = item.weather.firstOrNull()?.description.orEmpty(),
                modifier = Modifier.size(if (isHighlighted) 56.dp else 50.dp)
            )

            Text(
                text = temperatureDisplay,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DailyForecastSection(
    dailyItems: List<DailyWeatherUiModel>,
    selectedIndex: Int,
    onDayClick: (Int) -> Unit,
    windSpeedUnit: WindSpeedUnit,
    temperatureUnit: TemperatureUnit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dailyItems.forEachIndexed { index, item ->
            DailyForecastRow(
                item = item,
                isSelected = selectedIndex == index,
                onClick = { onDayClick(index) },
                temperatureUnit = temperatureUnit,
                windSpeedUnit = windSpeedUnit
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DailyForecastRow(
    item: DailyWeatherUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    temperatureUnit: TemperatureUnit,
    windSpeedUnit: WindSpeedUnit
) {

    val maxTempDisplay = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "${item.maxTemp.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${item.maxTemp.toInt()}°F"
        TemperatureUnit.KELVIN -> "${item.maxTemp.toInt()} K"
    }

    val minTempDisplay = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "${item.minTemp.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${item.minTemp.toInt()}°F"
        TemperatureUnit.KELVIN -> "${item.minTemp.toInt()} K"
    }

    val convertedWindSpeed = when (windSpeedUnit) {
        WindSpeedUnit.METERS_PER_SECOND -> "${item.avgWindSpeed.toInt()} m/s"
        WindSpeedUnit.KILOMETERS_PER_HOUR -> String.format(
            "%.2f",
            (item.avgWindSpeed * 3.6)
        ) + " km/h"

        WindSpeedUnit.MILES_PER_HOUR -> String.format(
            "%.2f",
            (item.avgWindSpeed * 2.23694)
        ) + " mph"
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            glassSurfaceColor()
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                        glassBorderColor()
                    },
                    shape = RoundedCornerShape(26.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.dayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
                    )
                }

                AsyncImage(
                    model = weatherIconUrl(item.iconCode),
                    contentDescription = item.description,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = maxTempDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f),
                    modifier = Modifier.rotate(if (isSelected) 180f else 0f)
                )
            }

            AnimatedVisibility(visible = isSelected) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = glassBorderColor().copy(alpha = 0.55f)
                    )

                    Text(
                        text = item.description.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = "Min",
                            value = minTempDisplay,
                            icon = Icons.Outlined.Thermostat,
                            iconTint = Color(0xFF7CCBFF)
                        )

                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = "Max",
                            value = maxTempDisplay,
                            icon = Icons.Outlined.Thermostat,
                            iconTint = Color(0xFFFFB86B)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = "Humidity",
                            value = "${item.avgHumidity}%",
                            icon = Icons.Outlined.WaterDrop,
                            iconTint = Color(0xFF61D3FF)
                        )

                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = "Wind",
                            value = convertedWindSpeed,
                            icon = Icons.Outlined.Air,
                            iconTint = Color(0xFF9EC5FF)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = "Clouds",
                            value = "${item.avgClouds}%",
                            icon = Icons.Outlined.Cloud,
                            iconTint = Color(0xFFC9D4F7)
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDetailStat(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(
            alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.18f else 0.82f
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = glassBorderColor().copy(alpha = 0.7f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
                )
            }
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = glassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.border(
                width = 1.dp,
                color = glassBorderColor(),
                shape = RoundedCornerShape(cornerRadius)
            ),
            content = content
        )
    }
}

@Composable
private fun glassSurfaceColor(): Color {
    return MaterialTheme.colorScheme.surface.copy(
        alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.22f else 0.72f
    )
}

@Composable
private fun glassBorderColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    }
}

@Composable
private fun softOverlayColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }
}

private fun weatherIconUrl(iconCode: String): String {
    return "https://openweathermap.org/img/wn/$iconCode@4x.png"
}

private fun formatDateTime(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val adjustedTime = (timestamp + timezoneOffsetSeconds) * 1000L
    val formatter = SimpleDateFormat("EEEE, d MMMM | HH:mm", Locale.getDefault())
    return formatter.format(Date(adjustedTime))
}

private fun formatHour(timestamp: Long): String {
    val formatter = SimpleDateFormat("ha", Locale.getDefault())
    return formatter.format(Date(timestamp * 1000L))
}

private fun formatDayName(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
    formatter.timeZone = java.util.TimeZone.getTimeZone("GMT")
    return formatter.format(Date((timestamp + timezoneOffsetSeconds) * 1000L))
}

private fun formatShortDate(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
    formatter.timeZone = java.util.TimeZone.getTimeZone("GMT")
    return formatter.format(Date((timestamp + timezoneOffsetSeconds) * 1000L))
}

private fun rememberNext24HoursItems(
    items: List<ForecastItemDto>
): List<ForecastItemDto> {
    if (items.isEmpty()) return emptyList()

    val firstTimestamp = items.first().dt
    val endTimestamp = firstTimestamp + (24 * 60 * 60)

    return items.filter { it.dt in firstTimestamp..endTimestamp }
}

private fun rememberDailyItems(
    items: List<ForecastItemDto>,
    timezoneOffsetSeconds: Int
): List<DailyWeatherUiModel> {
    val grouped = items.groupBy { it.dtTxt.substringBefore(" ") }

    return grouped.entries.take(6).mapNotNull { entry ->
        val dayItems = entry.value
        val representative = dayItems.firstOrNull() ?: return@mapNotNull null

        val minTemp = dayItems.minOfOrNull { it.main.tempMin } ?: representative.main.tempMin
        val maxTemp = dayItems.maxOfOrNull { it.main.tempMax } ?: representative.main.tempMax

        val avgHumidity = dayItems.map { it.main.humidity }.average().toInt()
        val avgWindSpeed = dayItems.map { it.wind.speed }.average()
        val avgClouds = dayItems.map { it.clouds.all }.average().toInt()

        val middayItem = dayItems.minByOrNull { kotlin.math.abs((it.dt % 86400) - (12 * 3600)) }
            ?: representative

        DailyWeatherUiModel(
            dayName = formatDayName(
                timestamp = representative.dt,
                timezoneOffsetSeconds = timezoneOffsetSeconds
            ),
            dateLabel = formatShortDate(
                timestamp = representative.dt,
                timezoneOffsetSeconds = timezoneOffsetSeconds
            ),
            minTemp = minTemp,
            maxTemp = maxTemp,
            description = middayItem.weather.firstOrNull()?.description.orEmpty(),
            iconCode = middayItem.weather.firstOrNull()?.icon.orEmpty(),
            avgHumidity = avgHumidity,
            avgWindSpeed = avgWindSpeed,
            avgClouds = avgClouds
        )
    }
}

data class DailyWeatherUiModel(
    val dayName: String,
    val dateLabel: String,
    val minTemp: Double,
    val maxTemp: Double,
    val description: String,
    val iconCode: String,
    val avgHumidity: Int,
    val avgWindSpeed: Double,
    val avgClouds: Int
)


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
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Location Services Disabled",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Location services are disabled. Please enable location to get weather for your current location.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
