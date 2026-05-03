package com.example.breez.presentation.home.utils

import com.example.breez.data.dto.ForecastItemDto
import com.example.breez.presentation.home.components.DailyWeatherUiModel

fun rememberNext24HoursItems(
    items: List<ForecastItemDto>
): List<ForecastItemDto> {
    if (items.isEmpty()) return emptyList()

    val firstTimestamp = items.first().dt
    val endTimestamp = firstTimestamp + (24 * 60 * 60)

    return items.filter { it.dt in firstTimestamp..endTimestamp }
}

fun rememberDailyItems(
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
