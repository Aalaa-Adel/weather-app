package com.example.breez.data.datasource.repository

import com.example.breez.data.datasource.local.FakeLocalDataSource
import com.example.breez.data.datasource.remote.FakeWeatherRemoteDataSource
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.repository.BreezRepositoryImpl
import com.example.breez.data.util.ApiResult
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.Date

class BreezRepositoryTest {

    private lateinit var repository: BreezRepository
    private lateinit var fakeRemoteDataSource: FakeWeatherRemoteDataSource
    private lateinit var fakeLocalDataSource: FakeLocalDataSource
    private lateinit var gson: Gson

    private val localFavorites = listOf(
        FavoriteEntity(id = 1, cityName = "Cairo", latitude = 30.0444, longitude = 31.2357, countryCode = "EG"),
        FavoriteEntity(id = 2, cityName = "Alexandria", latitude = 31.2001, longitude = 29.9187, countryCode = "EG")
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        fakeRemoteDataSource = FakeWeatherRemoteDataSource()
        fakeLocalDataSource = FakeLocalDataSource(
            favoritesList = localFavorites.toMutableList(),
        )
        gson = Gson()

        repository = BreezRepositoryImpl(
            fakeRemoteDataSource,
            fakeLocalDataSource,
            gson
        )
    }

    @Test
    fun getAllFavorites_returnsListFromLocal() = runTest {
        // when getting all favorites
        val favorites = mutableListOf<FavoriteEntity>()
        repository.getAllFavorites().collect { list ->
            favorites.addAll(list)
        }

        // then the favorites should come from local data
        assertThat(favorites, `is`(localFavorites))
    }

    @Test
    fun isFavorite_withExistingLocation_returnsTrue() = runTest {
        // when checking if Cairo is favorite
        val result = repository.isFavorite(30.0444, 31.2357)

        // then it should return true from local data
        assertThat(result, `is`(true))
    }

    @Test
    fun getLocationNameFromCoordinates_returnsSuccessFromRemote() = runTest {
        // when getting location name from coordinates
        val result = repository.getLocationNameFromCoordinates(30.0444, 31.2357)

        // then the result should be success from remote data
        assertThat(result is ApiResult.Success, `is`(true))
    }

    @Test
    fun getCoordinatesFromCityName_returnsSuccessFromRemote() = runTest {
        // when getting coordinates from city name
        val result = repository.getCoordinatesFromCityName("Cairo")

        // then the result should be success from remote data
        assertThat(result is ApiResult.Success, `is`(true))
    }


}
