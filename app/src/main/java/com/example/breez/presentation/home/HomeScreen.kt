package com.example.breez.presentation.home
import androidx.compose.material3.Scaffold
import com.example.breez.presentation.navigation.BreezCurvedBottomBar
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.dto.ForecastItemDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BreezCurvedBottomBar(
                onHomeClick = {
                },
                onCenterClick = {
                },
                onFavoriteClick = {
                },
                onMenuClick = {
                }
            )
        }
    ) { innerPadding ->
        WeatherScreenBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()))
             {if (!isConnected) {
                 ErrorContent(
                     message = "No internet connection",
                     onRetry = viewModel::refresh
                 )
             } else {

                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }

                    uiState.error != null -> {
                        ErrorContent(
                            message = uiState.error ?: "Unknown error",
                            onRetry = viewModel::refresh
                        )
                    }

                    uiState.currentWeather != null && uiState.forecast != null -> {
                        HomeContent(
                            uiState = uiState,
                            onRefresh = viewModel::refresh
                        )
                    }
                }}
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
                    .background(
                        Color.White.copy(alpha = 0.08f)
                    )
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
    uiState: HomeUiState,
    onRefresh: () -> Unit
) {
    val currentWeather = uiState.currentWeather!!
    val forecast = uiState.forecast!!

    val hourlyItems = rememberTodayItems(forecast.list)
    val dailyItems = rememberDailyItems(forecast.list, forecast.city.timezone)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

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
                dateTimeText = formatDateTime(currentWeather.dt, currentWeather.timezone)
            )
        }

        item {
            WeatherStatsCard(
                humidity = currentWeather.main.humidity,
                windSpeed = currentWeather.wind.speed,
                pressure = currentWeather.main.pressure,
                clouds = currentWeather.clouds.all
            )
        }

        item {
            SectionTitle(title = "Today")
        }

        item {
            HourlyForecastSection(hourlyItems = hourlyItems)
        }

        item {
            SectionTitle(title = "5-Day Forecast")
        }

        item {
            DailyForecastSection(dailyItems = dailyItems)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeTopBar(
    cityName: String,
    onRefresh: () -> Unit
) {
    val refreshRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
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

        Text(
            text = cityName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

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

@Composable
private fun GlassIconButton(
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.16f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp)
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
    dateTimeText: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    val fadeIn = remember { Animatable(0f) }
    val scaleIn = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        fadeIn.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        scaleIn.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 220f)
        )
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
                                Color.White.copy(alpha = 0.13f),
                                Color.White.copy(alpha = 0.03f),
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

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.94f),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${temperature.toInt()}°",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = dateTimeText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeatherStatsCard(
    humidity: Int,
    windSpeed: Double,
    pressure: Int,
    clouds: Int
) {
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.23f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBrush)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(28.dp)
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
            iconTint = Color(0xFFD9E1FF),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Air,
                    contentDescription = "Wind",
                    tint = Color(0xFFD9E1FF)
                )
            },
            title = "Wind",
            value = "${windSpeed.toInt()} km/h"
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
            iconTint = Color(0xFFE6EAF4),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.WbCloudy,
                    contentDescription = "Clouds",
                    tint = Color(0xFFE6EAF4)
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
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.10f)),
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
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun HourlyForecastSection(
    hourlyItems: List<ForecastItemDto>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(hourlyItems) { index, item ->
            HourlyForecastCard(
                item = item,
                isHighlighted = index == 0
            )
        }
    }
}

@Composable
private fun HourlyForecastCard(
    item: ForecastItemDto,
    isHighlighted: Boolean
) {
    val backgroundBrush = if (isHighlighted) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.10f)
            )
        )
    }

    Box(
        modifier = Modifier
            .width(92.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                color = if (isHighlighted) {
                    Color.White.copy(alpha = 0.16f)
                } else {
                    Color.White.copy(alpha = 0.06f)
                },
                shape = RoundedCornerShape(24.dp)
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f),
                fontWeight = FontWeight.Medium
            )

            AsyncImage(
                model = weatherIconUrl(item.weather.firstOrNull()?.icon.orEmpty()),
                contentDescription = item.weather.firstOrNull()?.description.orEmpty(),
                modifier = Modifier.size(if (isHighlighted) 54.dp else 50.dp)
            )

            Text(
                text = "${item.main.temp.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DailyForecastSection(
    dailyItems: List<DailyWeatherUiModel>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dailyItems.forEachIndexed { index, item ->
            DailyForecastRow(
                item = item,
                showDivider = index != dailyItems.lastIndex
            )
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyWeatherUiModel,
    showDivider: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.dayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                AsyncImage(
                    model = weatherIconUrl(item.iconCode),
                    contentDescription = item.description,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.description.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.weight(1.35f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                )

                Text(
                    text = "${item.temperature.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showDivider) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.04f)
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
        tonalElevation = 8.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(cornerRadius)
                ),
            content = content
        )
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

private fun rememberTodayItems(items: List<ForecastItemDto>): List<ForecastItemDto> {
    if (items.isEmpty()) return emptyList()
    val firstDate = items.first().dtTxt.substringBefore(" ")
    return items.filter { it.dtTxt.startsWith(firstDate) }
}

private fun rememberDailyItems(
    items: List<ForecastItemDto>,
    timezoneOffsetSeconds: Int
): List<DailyWeatherUiModel> {
    val grouped = items.groupBy { it.dtTxt.substringBefore(" ") }

    return grouped.entries.take(5).mapNotNull { entry ->
        val representative = entry.value.getOrNull(0) ?: return@mapNotNull null

        DailyWeatherUiModel(
            dayName = formatDayName(
                timestamp = representative.dt,
                timezoneOffsetSeconds = timezoneOffsetSeconds
            ),
            temperature = representative.main.temp,
            description = representative.weather.firstOrNull()?.description.orEmpty(),
            iconCode = representative.weather.firstOrNull()?.icon.orEmpty()
        )
    }
}

private fun formatDayName(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
    formatter.timeZone = java.util.TimeZone.getTimeZone("GMT")
    return formatter.format(Date((timestamp + timezoneOffsetSeconds) * 1000L))
}

data class DailyWeatherUiModel(
    val dayName: String,
    val temperature: Double,
    val description: String,
    val iconCode: String
)