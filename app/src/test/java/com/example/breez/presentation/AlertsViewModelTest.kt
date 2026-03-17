package com.example.breez.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.AlertType
import com.example.breez.data.notification.AlertScheduler
import com.example.breez.data.notification.NotificationHelper
import com.example.breez.data.repository.BreezRepository
import com.example.breez.presentation.alerts.AlertsUiState
import com.example.breez.presentation.alerts.AlertsViewModel
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
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private lateinit var viewModel: AlertsViewModel

    @MockK
    private lateinit var repository: BreezRepository

    @MockK
    private lateinit var alertScheduler: AlertScheduler

    @MockK
    private lateinit var notificationHelper: NotificationHelper

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val dummyAlerts = listOf(
        AlertEntity(
            id = 1,
            title = "Morning Alert",
            description = "Start your day",
            startTime = Date(System.currentTimeMillis() + 3600000),
            endTime = Date(System.currentTimeMillis() + 7200000),
            alertType = AlertType.NOTIFICATION,
            isActive = true,
            latitude = 30.0444,
            longitude = 31.2357,
            cityName = "Cairo",
            createdAt = System.currentTimeMillis()
        ),
        AlertEntity(
            id = 2,
            title = "Alarm",
            description = "Wake up",
            startTime = Date(System.currentTimeMillis() + 10000),
            endTime = Date(System.currentTimeMillis() + 9200000),
            alertType = AlertType.ALARM,
            isActive = true,
            latitude = 31.2001,
            longitude = 29.9187,
            cityName = "Alexandria",
            createdAt = System.currentTimeMillis()
        ),
        AlertEntity(
            id = 3,
            title = "Disabled Alert",
            description = "Not active",
            startTime = Date(System.currentTimeMillis() + 20000),
            endTime = Date(System.currentTimeMillis() +8200000),
            alertType = AlertType.ALARM,
            isActive = false,
            latitude = 30.0131,
            longitude = 31.2089,
            cityName = "Giza",
            createdAt = System.currentTimeMillis()
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getAllAlerts() } returns flowOf(dummyAlerts)
        coEvery { repository.updateAlert(any()) } just runs
        coEvery { repository.deleteAlert(any()) } just runs

        coEvery { alertScheduler.scheduleAlert(any()) } just runs
        coEvery { alertScheduler.cancelAlert(any()) } just runs
        coEvery { alertScheduler.scheduleTestNotification(any()) } just runs

        coEvery { notificationHelper.hasNotificationPermission() } returns true

        viewModel = AlertsViewModel(repository, alertScheduler, notificationHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAlerts_onInit_loadsFromRepository() = runTest {
        // when viewmodel is initialized
        testDispatcher.scheduler.advanceUntilIdle()

        // then repository is called to get all alerts
        coVerify { repository.getAllAlerts() }
    }

    @Test
    fun loadAlerts_returnsSuccessState() = runTest {
        // when alerts are loaded
        testDispatcher.scheduler.advanceUntilIdle()

        // then state should be success with alerts
        val state = viewModel.uiState.value
        assertTrue(state is AlertsUiState.Success)
        if (state is AlertsUiState.Success) {
            assertThat(state.alerts.size, `is`(dummyAlerts.size))
        }
    }

    @Test
    fun deleteAlert_existingAlert_removedFromRepository() = runTest {
        // given an alert to delete
        val itemToDelete = dummyAlerts[0]

        // when deleting alert
        viewModel.deleteAlert(itemToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // then repository is called to delete
        coVerify { repository.deleteAlert(itemToDelete) }
        // and scheduler is called to cancel
        coVerify { alertScheduler.cancelAlert(itemToDelete.id) }
    }


    @Test
    fun onAddAlertClick_triggered_emitsNavigateToAddAlert() = runTest {
        // when clicking add alert
        viewModel.navigateToAddAlert.test {
            viewModel.onAddAlertClick()
            testDispatcher.scheduler.advanceUntilIdle()

            // then navigate event is emitted
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }


}
