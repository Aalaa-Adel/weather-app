package com.example.breez.data.datasource.local


import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.breez.data.db.BreezDatabase
import com.example.breez.data.db.dao.AlertDao
import com.example.breez.data.db.dao.FavoriteDao
import com.example.breez.data.db.dao.WeatherCacheDao
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.AlertType
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.db.entity.WeatherCacheEntity
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
@MediumTest
class LocalDataSourceTest {

    private lateinit var localDataSource: LocalDataSource
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var alertDao: AlertDao
    private lateinit var weatherCacheDao: WeatherCacheDao
    private lateinit var db: BreezDatabase

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(
            application,
            BreezDatabase::class.java,
        ).build()

        favoriteDao = db.favoriteDao()
        alertDao = db.alertDao()
        weatherCacheDao = db.weatherCacheDao()
        localDataSource = LocalDataSourceImpl(favoriteDao, alertDao, weatherCacheDao)
    }

    @After
    fun tearDown() {
        db.close()
    }


    @Test
    fun insertFavorite_getFavoriteById_returnsFavorite() = runTest {
        // Given a favorite to save
        val favorite = FavoriteEntity(
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357,
            countryCode = "EG",
            state = "Cairo Governorate"
        )

        // When saving the favorite
        localDataSource.insertFavorite(favorite)

        // Then the favorite should be retrievable by ID
        val retrievedFavorite = localDataSource.getFavoriteById(1)
        assertThat(retrievedFavorite, notNullValue())
        assertThat(retrievedFavorite?.cityName, `is`("Cairo"))
        assertThat(retrievedFavorite?.latitude, `is`(30.0444))
    }



    @Test
    fun deleteFavorite_getFavoriteById_returnsNull() = runTest {
        // Given a saved favorite
        val favorite = FavoriteEntity(
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357
        )
        localDataSource.insertFavorite(favorite)

        // When deleting the favorite
        val savedFavorite = localDataSource.getFavoriteById(1)
        assertThat(savedFavorite, notNullValue())
        localDataSource.deleteFavorite(savedFavorite!!)

        // Then the favorite should not be retrievable
        val deletedFavorite = localDataSource.getFavoriteById(1)
        assertThat(deletedFavorite, `is`(null as FavoriteEntity?))
    }

    @Test
    fun isFavorite_withExistingLocation_returnsTrue() = runTest {
        // Given a favorite at specific coordinates
        val favorite = FavoriteEntity(
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357
        )
        localDataSource.insertFavorite(favorite)

        // When checking if location is favorite
        val isFavorite = localDataSource.isFavorite(30.0444, 31.2357)

        // Then it should return true
        assertThat(isFavorite, `is`(true))
    }

    @Test
    fun isFavorite_withNonExistingLocation_returnsFalse() = runTest {
        // When checking if non-existing location is favorite
        val isFavorite = localDataSource.isFavorite(40.7128, -74.0060)

        // Then it should return false
        assertThat(isFavorite, `is`(false))
    }



}
