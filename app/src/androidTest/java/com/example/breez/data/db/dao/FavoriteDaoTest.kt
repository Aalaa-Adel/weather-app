package com.example.breez.data.db.dao

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.breez.data.db.BreezDatabase
import com.example.breez.data.db.entity.FavoriteEntity
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var favoriteDao: FavoriteDao
    private lateinit var db: BreezDatabase


    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(
            application,
            BreezDatabase::class.java,
        ).build()

        favoriteDao = db.favoriteDao()
    }


    @After
    fun tearDown() {
        db.close()
    }


    @Test
    fun insertFavorite_getFavoriteById_returnsFavorite() = runTest {
        // Prepare test data
        val favorite = FavoriteEntity(
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357,
            countryCode = "EG",
            state = "Cairo Governorate"
        )

        // Insert the favorite into database
        favoriteDao.insertFavorite(favorite)

        // Retrieve and verify the data
        val retrievedFavorite = favoriteDao.getFavoriteById(1)
        assertThat(retrievedFavorite, notNullValue())
        assertThat(retrievedFavorite?.cityName, `is`("Cairo"))
        assertThat(retrievedFavorite?.latitude, `is`(30.0444))
        assertThat(retrievedFavorite?.longitude, `is`(31.2357))
    }


    @Test
    fun deleteFavorite_getFavoriteById_returnsNull() = runTest {
        // Prepare a favorite
        val favorite = FavoriteEntity(
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357
        )

        // Insert and then delete the favorite
        favoriteDao.insertFavorite(favorite)
        val savedFavorite = favoriteDao.getFavoriteById(1)
        assertThat(savedFavorite, notNullValue())

        favoriteDao.deleteFavorite(savedFavorite!!)

        // Verify the favorite is deleted
        val deletedFavorite = favoriteDao.getFavoriteById(1)
        assertThat(deletedFavorite, `is`(null as FavoriteEntity?))
    }

    @Test
    fun isFavorite_withExistingLocation_returnsOne() = runTest {
        // Prepare a favorite at specific coordinates
        val favorite = FavoriteEntity(
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357
        )

        // Insert the favorite
        favoriteDao.insertFavorite(favorite)

        // Verify the location exists
        val count = favoriteDao.isFavorite(30.0444, 31.2357)
        assertThat(count, `is`(1))
    }


    @Test
    fun isFavorite_withNonExistingLocation_returnsZero() = runTest {
        // Verify non-existent location returns 0
        val count = favoriteDao.isFavorite(40.7128, -74.0060)
        assertThat(count, `is`(0))
    }

}
