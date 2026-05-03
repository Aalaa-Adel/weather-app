package com.example.breez.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.dto.ForecastItemDto
import com.example.breez.presentation.components.WeatherLottieIcon
import com.example.breez.presentation.components.glassBorderColor
import com.example.breez.presentation.components.weatherLottieAsset
import com.example.breez.presentation.home.utils.formatHour
import com.example.breez.utils.formatTemperature

@Composable
fun HourlyForecastSection(
    hourlyItems: List<ForecastItemDto>,
    temperatureUnit: TemperatureUnit,
    timezoneOffsetSeconds: Int
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(hourlyItems) { index, item ->
            HourlyForecastCard(
                item = item,
                isHighlighted = index == 0,
                temperatureUnit = temperatureUnit,
                timezoneOffsetSeconds = timezoneOffsetSeconds
            )
        }
    }
}

@Composable
fun HourlyForecastCard(
    item: ForecastItemDto,
    isHighlighted: Boolean,
    temperatureUnit: TemperatureUnit,
    timezoneOffsetSeconds: Int
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val backgroundBrush = if (isHighlighted) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.secondaryContainer
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }

    val borderColor = if (isHighlighted)
        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.40f else 0.28f)
    else
        glassBorderColor()

    val context = LocalContext.current

    val tempDisplay = formatTemperature(
        context = context,
        value = item.main.temp,
        unit = temperatureUnit,
        withDegreeOnlyForCelsius = true
    )

    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundBrush)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = formatHour(item.dt, timezoneOffsetSeconds),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )

        WeatherLottieIcon(
            assetName = weatherLottieAsset(item.weather.firstOrNull()?.icon.orEmpty()),
            modifier = Modifier.size(if (isHighlighted) 52.dp else 44.dp)
        )

        Text(
            text = tempDisplay,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}