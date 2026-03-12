package com.example.breez.data.repository



import com.example.breez.data.datasource.remote.WeatherRemoteDataSource
import com.example.breez.data.dto.CurrentWeatherDto
import com.example.breez.data.dto.ForecastResponseDto
import com.example.breez.data.dto.GeocodingDto
import com.example.breez.data.util.ApiResult
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<CurrentWeatherDto> {
        return remoteDataSource.fetchCurrentWeather(
            lat = lat,
            lon = lon,
            units = units,
            lang = lang
        )
    }

    override suspend fun getForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiResult<ForecastResponseDto> {
        return remoteDataSource.fetchForecast(
            lat = lat,
            lon = lon,
            units = units,
            lang = lang
        )
    }

    override suspend fun getCoordinatesFromCityName(cityName: String): ApiResult<List<GeocodingDto>> {
        return remoteDataSource.fetchCoordinatesFromCityName(cityName)
    }

    override suspend fun getLocationNameFromCoordinates(
        lat: Double,
        lon: Double
    ): ApiResult<List<GeocodingDto>> {
        return remoteDataSource.fetchLocationNameFromCoordinates(lat, lon)
    }
}