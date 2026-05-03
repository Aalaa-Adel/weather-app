package com.example.breez.presentation.components

fun weatherLottieAsset(iconCode: String?): String {
    return when (iconCode) {
        "01d" -> "clear-day.json"
        "01n" -> "clear-night.json"

        "02d" -> "partly-cloudy-day.json"
        "02n" -> "partly-cloudy-night.json"

        "03d", "03n" -> "cloudy.json"
        "04d", "04n" -> "overcast.json"

        "09d", "09n" -> "drizzle.json"
        "10d", "10n" -> "rain.json"

        "11d", "11n" -> "thunderstorms.json"

        "13d", "13n" -> "snow.json"

        "50d", "50n" -> "fog.json"

        else -> "cloudy.json"
    }
}