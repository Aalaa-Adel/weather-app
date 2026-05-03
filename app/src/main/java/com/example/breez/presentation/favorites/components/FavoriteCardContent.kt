package com.example.breez.presentation.favorites.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.breez.R
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.presentation.components.WeatherLottieIcon
import com.example.breez.presentation.components.weatherLottieAsset
import com.example.breez.utils.formatPercent
import com.example.breez.utils.formatTemperature
import com.example.breez.utils.formatWindSpeed

@Composable
fun FavoriteCardContent(favorite: FavoriteEntity) {
    val context = LocalContext.current

    val temperatureText = formatTemperature(
        context = context,
        value = favorite.temperature ?: 0.0,
        unit = TemperatureUnit.CELSIUS,
        withDegreeOnlyForCelsius = true
    )

    val humidityText = formatPercent(context, favorite.humidity ?: 0)

    val windText = formatWindSpeed(
        context = context,
        value = favorite.windSpeed ?: 0.0,
        unit = WindSpeedUnit.METERS_PER_SECOND
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeatherLottieIcon(
            assetName = weatherLottieAsset(favorite.weatherIcon),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = favorite.cityName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = temperatureText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            favorite.weatherDescription?.let { desc ->
                Text(
                    text = desc.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = stringResource(R.string.cd_humidity),
                    tint = Color(0xFF61D3FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text =humidityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }

            favorite.windSpeed?.let { wind ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Air,
                        contentDescription = stringResource(R.string.cd_wind),
                        tint = Color(0xFF9EC5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = windText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}