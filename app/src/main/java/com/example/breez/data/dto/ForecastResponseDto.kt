package com.example.breez.data.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<ForecastItemDto>,
    val city: ForecastCityDto
)

data class ForecastItemDto(
    val dt: Long,
    val main: ForecastMainDto,
    val weather: List<ForecastWeatherDto>,
    val clouds: ForecastCloudsDto,
    val wind: ForecastWindDto,
    val visibility: Int,
    val pop: Double,
    val sys: ForecastSysDto,
    @SerializedName("dt_txt")
    val dtTxt: String)

data class ForecastMainDto(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Int,
    @SerializedName("sea_level")
    val seaLevel: Int,
    @SerializedName("grnd_level")
    val grndLevel: Int,
    val humidity: Int,
    @SerializedName("temp_kf")
    val tempKf: Double
)

data class ForecastWeatherDto(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class ForecastCloudsDto(
    val all: Int
)

data class ForecastWindDto(
    val speed: Double,
    val deg: Int,
    val gust: Double?
)

data class ForecastSysDto(
    val pod: String
)

data class ForecastCityDto(
    val id: Int,
    val name: String,
    val coord: ForecastCoordDto,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

data class ForecastCoordDto(
    val lat: Double,
    val lon: Double
)