package com.example.stormpilot.data

import android.content.Context
import android.location.Geocoder
import app.cash.turbine.test
import com.example.stormpilot.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockContext: Context
    private lateinit var mockLocationRepository: LocationRepository
    private lateinit var mockAlertsRepository: AlertsRepository
    private lateinit var locationFlow: MutableStateFlow<LocationData?>
    private lateinit var viewModel: AlertsViewModel

    @Before
    fun setup() {
        mockContext = mockk()
        mockLocationRepository = mockk(relaxed = true)
        mockAlertsRepository = mockk()

        locationFlow = MutableStateFlow(null)
        every { mockLocationRepository.location } returns locationFlow

        mockkConstructor(Geocoder::class)
    }

    @After
    fun teardown() {
        unmockkAll()
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun testInitialState() = runTest {
        viewModel = AlertsViewModel(mockContext, mockLocationRepository, mockAlertsRepository)

        viewModel.uiState.test {
            val initialState = awaitItem()
            // The initial state shouldn't crash and hasAnyAlert should be false without alerts
            assertFalse(initialState.hasAnyAlert)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testLocationUpdateTriggersAlertFetchSuccess() = runTest {
        val alert = NwsAlert(
            id = "1",
            event = "Tornado Warning",
            headline = "Tornado Warning issued",
            description = "Take cover",
            instruction = "Run",
            severity = "Extreme",
            urgency = "Immediate",
            onset = null,
            expires = null,
            senderName = "NWS"
        )
        coEvery { mockAlertsRepository.fetchAlerts(any(), any()) } returns Result.success(listOf(alert))

        viewModel = AlertsViewModel(mockContext, mockLocationRepository, mockAlertsRepository)

        // Trigger a location update
        locationFlow.value = LocationData(latitude = 37.0, longitude = -122.0, bearing = 0f, speed = 0f)

        viewModel.uiState.test {
            var finalState = awaitItem()
            while (finalState.isLoading || finalState.tornadoWarning == null) {
                finalState = awaitItem()
            }

            assertFalse(finalState.isLoading)
            assertNotNull(finalState.tornadoWarning)
            assertEquals("Tornado Warning", finalState.tornadoWarning?.event)
            assertTrue(finalState.hasAnyAlert)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testAlertFetchFailureUpdatesErrorState() = runTest {
        coEvery { mockAlertsRepository.fetchAlerts(any(), any()) } returns Result.failure(Exception("Alerts error"))

        viewModel = AlertsViewModel(mockContext, mockLocationRepository, mockAlertsRepository)

        locationFlow.value = LocationData(latitude = 37.0, longitude = -122.0, bearing = 0f, speed = 0f)

        viewModel.uiState.test {
            var finalState = awaitItem()
            while (finalState.isLoading || finalState.error == null) {
                finalState = awaitItem()
            }

            assertFalse(finalState.isLoading)
            assertEquals("Alerts error", finalState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
