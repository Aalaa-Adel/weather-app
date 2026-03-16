package com.example.breez.presentation.favorites

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastItemDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(lat, lon) {
        viewModel.loadWeatherData(lat, lon, favoriteId)
    }

    WeatherScreenBackground {
        when (val state = uiState) {
            is FavoriteDetailsUiState.Loading -> {
                LoadingContent()
            }

            is FavoriteDetailsUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadWeatherData(lat, lon) },
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
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.size(80.dp),
            cornerRadius = 40.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ErrorContent(
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
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 30.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Error loading weather",
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
                        Text("Back")
                    }
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteDetailsContent(
    cityName: String,
    currentWeather: CurrentWeatherDto,
    forecast: ForecastResponseDto,
    temperatureUnit: TemperatureUnit,
    windSpeedUnit: WindSpeedUnit,
    onBackClick: () -> Unit
) {
    val next24HoursItems = remember(forecast.list) {
        if (forecast.list.isEmpty()) emptyList()
        else {
            val firstTimestamp = forecast.list.first().dt
            val endTimestamp = firstTimestamp + (24 * 60 * 60)
            forecast.list.filter { it.dt in firstTimestamp..endTimestamp }
        }
    }

    val dailyItems = remember(forecast.list, forecast.city.timezone) {
        val grouped = forecast.list.groupBy { it.dtTxt.substringBefore(" ") }
        grouped.entries.take(6).mapNotNull { entry ->
            val dayItems = entry.value
            val representative = dayItems.firstOrNull() ?: return@mapNotNull null

            val minTemp = dayItems.minOfOrNull { it.main.tempMin } ?: representative.main.tempMin
            val maxTemp = dayItems.maxOfOrNull { it.main.tempMax } ?: representative.main.tempMax
            val avgHumidity = dayItems.map { it.main.humidity }.average().toInt()
            val avgWindSpeed = dayItems.map { it.wind.speed }.average()
            val avgClouds = dayItems.map { it.clouds.all }.average().toInt()

            val middayItem = dayItems.minByOrNull { kotlin.math.abs((it.dt % 86400) - (12 * 3600)) } ?: representative

            DailyWeatherItem(
                dayName = formatDayName(representative.dt, forecast.city.timezone),
                dateLabel = formatShortDate(representative.dt, forecast.city.timezone),
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

    val selectedDayIndex = remember { mutableIntStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            FavoriteTopBar(
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
                    selectedDayIndex.intValue = if (selectedDayIndex.intValue == clickedIndex) -1 else clickedIndex
                },
                windSpeedUnit = windSpeedUnit,
                temperatureUnit = temperatureUnit
            )
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun FavoriteTopBar(
    cityName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            onClick = onBackClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Favorite Location",
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

        Spacer(modifier = Modifier.width(48.dp))
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
    val temperatureDisplay = when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> "${temperature.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${temperature.toInt()}°F"
        TemperatureUnit.KELVIN -> "${temperature.toInt()} K"
    }

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
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
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

                AsyncImage(
                    model = "https://openweathermap.org/img/wn/$iconCode@4x.png",
                    contentDescription = description,
                    modifier = Modifier.size(148.dp)
                )
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
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
    val convertedWindSpeed = when (windSpeedUnit) {
        WindSpeedUnit.METERS_PER_SECOND -> "${windSpeed.toInt()} m/s"
        WindSpeedUnit.KILOMETERS_PER_HOUR -> String.format("%.2f", (windSpeed * 3.6)) + " km/h"
        WindSpeedUnit.MILES_PER_HOUR -> String.format("%.2f", (windSpeed * 2.23694)) + " mph"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        glassSurfaceColor(),
                        glassSurfaceColor().copy(alpha = 0.82f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = glassBorderColor(),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 14.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            icon = Icons.Outlined.WaterDrop,
            iconTint = Color(0xFF61D3FF),
            title = "Humidity",
            value = "$humidity%"
        )
        StatItem(
            icon = Icons.Outlined.Air,
            iconTint = Color(0xFF9EC5FF),
            title = "Wind",
            value = convertedWindSpeed
        )
        StatItem(
            icon = Icons.Outlined.Speed,
            iconTint = Color(0xFFFFD56A),
            title = "Pressure",
            value = "$pressure"
        )
        StatItem(
            icon = Icons.Outlined.WbCloudy,
            iconTint = Color(0xFFC9D4F7),
            title = "Clouds",
            value = "$clouds%"
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    iconTint: Color,
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
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint
            )
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
                model = "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon.orEmpty()}@2x.png",
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
    dailyItems: List<DailyWeatherItem>,
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
    item: DailyWeatherItem,
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
        WindSpeedUnit.KILOMETERS_PER_HOUR -> String.format("%.2f", (item.avgWindSpeed * 3.6)) + " km/h"
        WindSpeedUnit.MILES_PER_HOUR -> String.format("%.2f", (item.avgWindSpeed * 2.23694)) + " mph"
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
                Column(modifier = Modifier.weight(1f)) {
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
                    model = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png",
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
                    //   modifier = Modifier.rotate(if (isSelected) 180f else 0f)
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
            //    alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.18f else 0.82f
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
    formatter.timeZone = TimeZone.getTimeZone("GMT")
    return formatter.format(Date((timestamp + timezoneOffsetSeconds) * 1000L))
}

private fun formatShortDate(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("GMT")
    return formatter.format(Date((timestamp + timezoneOffsetSeconds) * 1000L))
}

data class DailyWeatherItem(
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

