package com.example.breez.utils

import android.content.Context
import com.example.breez.R
import com.example.breez.data.datasource.preferences.TemperatureUnit
import com.example.breez.data.datasource.preferences.WindSpeedUnit

fun formatTemperature(
    context: Context,
    value: Double,
    unit: TemperatureUnit,
    withDegreeOnlyForCelsius: Boolean = false
): String {
    val number = formatIntLocalized(value.toInt())

    return when (unit) {
        TemperatureUnit.CELSIUS -> {
            if (withDegreeOnlyForCelsius) "$number°"
            else context.getString(R.string.unit_celsius_value, number)
        }
        TemperatureUnit.FAHRENHEIT ->
            context.getString(R.string.unit_fahrenheit_value, number)

        TemperatureUnit.KELVIN ->
            context.getString(R.string.unit_kelvin_value, number)
    }
}

fun formatPercent(
    context: Context,
    value: Int
): String {
    return context.getString(
        R.string.unit_percent_value,
        formatIntLocalized(value)
    )
}

fun formatPressure(
    context: Context,
    value: Int
): String {
    return context.getString(
        R.string.unit_pressure_value,
        formatIntLocalized(value)
    )
}

fun formatWindSpeed(
    context: Context,
    value: Double,
    unit: WindSpeedUnit
): String {
    return when (unit) {
        WindSpeedUnit.METERS_PER_SECOND ->
            context.getString(
                R.string.unit_meters_per_second_value,
                formatIntLocalized(value.toInt())
            )

        WindSpeedUnit.KILOMETERS_PER_HOUR ->
            context.getString(
                R.string.unit_kilometers_per_hour_value,
                formatDoubleLocalized(value * 3.6, 2)
            )

        WindSpeedUnit.MILES_PER_HOUR ->
            context.getString(
                R.string.unit_miles_per_hour_value,
                formatDoubleLocalized(value * 2.23694, 2)
            )
    }
}