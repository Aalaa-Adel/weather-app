package com.example.breez.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.breez.data.db.entity.FavoriteEntity
import com.example.breez.data.repository.BreezRepository
import com.example.breez.presentation.favorites.FavoritesViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private lateinit var viewModel: FavoritesViewModel

    @MockK
    private lateinit var repository: BreezRepository

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val dummyFavorites = listOf(
        FavoriteEntity(
            id = 1,
            cityName = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357,
            countryCode = "EG"
        ),
        FavoriteEntity(
            id = 2,
            cityName = "Alexandria",
            latitude = 31.2001,
            longitude = 29.9187,
            countryCode = "EG"
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getAllFavorites() } returns flowOf(dummyFavorites)
        coEvery { repository.insertFavorite(any()) } just runs
        coEvery { repository.deleteFavorite(any()) } just runs

        viewModel = FavoritesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFavorites_onInit_loadsFromRepository() = runTest {
        // when viewmodel is initialized
        testDispatcher.scheduler.advanceUntilIdle()

        // then repository is called to get all favorites
        coVerify { repository.getAllFavorites() }
    }

    @Test
    fun addFavorite_validFavorite_insertsToRepository() = runTest {
        // given a new favorite
        val newFavorite = FavoriteEntity(
            id = 3,
            cityName = "Giza",
            latitude = 30.0131,
            longitude = 31.2089,
            countryCode = "EG"
        )

        // when adding favorite
        viewModel.addFavorite(newFavorite)
        testDispatcher.scheduler.advanceUntilIdle()

        // then repository is called to insert
        coVerify { repository.insertFavorite(newFavorite) }
    }

    @Test
    fun deleteFavorite_existingFavorite_removedFromRepository() = runTest {
        // given a favorite to delete
        val itemToDelete = dummyFavorites[0]

        // when deleting favorite
        viewModel.deleteFavorite(itemToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // then repository is called to delete
        coVerify { repository.deleteFavorite(itemToDelete) }
    }
}
