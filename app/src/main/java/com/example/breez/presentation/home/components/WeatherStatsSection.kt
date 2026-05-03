package com.example.breez.presentation.home.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.breez.R
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.presentation.components.glassBorderColor
import com.example.breez.presentation.components.glassSurfaceColor
import com.example.breez.utils.formatPercent
import com.example.breez.utils.formatPressure
import com.example.breez.utils.formatWindSpeed

@SuppressLint("DefaultLocale")
@Composable
fun WeatherStatsCard(
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

    val context = LocalContext.current

    val humidityDisplay = formatPercent(context, humidity)
    val pressureDisplay = formatPressure(context, pressure)
    val cloudsDisplay = formatPercent(context, clouds)
    val convertedWindSpeed = formatWindSpeed(context, windSpeed, windSpeedUnit)

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
                    contentDescription = stringResource(R.string.cd_humidity),
                    tint = Color(0xFF61D3FF)
                )
            },
            title = stringResource(R.string.humidity),
            value = humidityDisplay
        )

        StatItem(
            iconTint = Color(0xFF9EC5FF),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Air,
                    contentDescription = stringResource(R.string.cd_wind),
                    tint = Color(0xFF9EC5FF)
                )
            },
            title = stringResource(R.string.wind),
            value = convertedWindSpeed
        )

        StatItem(
            iconTint = Color(0xFFFFD56A),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = stringResource(R.string.cd_pressure),
                    tint = Color(0xFFFFD56A)
                )
            },
            title = stringResource(R.string.pressure),
            value = pressureDisplay
        )

        StatItem(
            iconTint = Color(0xFFC9D4F7),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.WbCloudy,
                    contentDescription = stringResource(R.string.cd_clouds),
                    tint = Color(0xFFC9D4F7)
                )
            },
            title = stringResource(R.string.clouds),
            value = cloudsDisplay
        )
    }
}

@Composable
fun StatItem(
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