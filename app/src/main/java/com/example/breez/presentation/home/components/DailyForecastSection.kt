package com.example.breez.presentation.home.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.breez.R
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.presentation.components.WeatherLottieIcon
import com.example.breez.presentation.components.glassBorderColor
import com.example.breez.presentation.components.glassSurfaceColor
import com.example.breez.presentation.components.weatherLottieAsset
import com.example.breez.utils.formatPercent
import com.example.breez.utils.formatTemperature
import com.example.breez.utils.formatWindSpeed

@Composable
fun DailyForecastSection(
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
fun DailyForecastRow(
    item: DailyWeatherUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    temperatureUnit: TemperatureUnit,
    windSpeedUnit: WindSpeedUnit
) {
    val context = LocalContext.current

    val maxTempDisplay = formatTemperature(
        context = context,
        value = item.maxTemp,
        unit = temperatureUnit
    )

    val minTempDisplay = formatTemperature(
        context = context,
        value = item.minTemp,
        unit = temperatureUnit
    )

    val convertedWindSpeed = formatWindSpeed(
        context = context,
        value = item.avgWindSpeed,
        unit = windSpeedUnit
    )

    val humidityDisplay = formatPercent(context, item.avgHumidity)
    val cloudsDisplay = formatPercent(context, item.avgClouds)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                WeatherLottieIcon(
                    assetName = weatherLottieAsset(item.iconCode),
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
                    contentDescription = if (isSelected) {
                        stringResource(R.string.cd_collapse)
                    } else {
                        stringResource(R.string.cd_expand)
                    },
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
                            title = stringResource(R.string.min),
                            value = minTempDisplay,
                            icon = Icons.Outlined.Thermostat,
                            iconTint = Color(0xFF7CCBFF)
                        )

                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.max),
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
                            title = stringResource(R.string.humidity),
                            value = humidityDisplay,                            icon = Icons.Outlined.WaterDrop,
                            iconTint = Color(0xFF61D3FF)
                        )

                        DayDetailStat(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.wind),
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
                            title = stringResource(R.string.clouds),
                            value = cloudsDisplay,
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
fun DayDetailStat(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
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