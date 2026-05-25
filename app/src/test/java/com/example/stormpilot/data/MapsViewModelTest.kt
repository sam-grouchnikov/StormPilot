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
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalCoroutinesApi::class)
class MapsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockContext: Context
    private lateinit var mockRoutingRepository: RoutingRepository
    private lateinit var viewModel: MapsViewModel

    @Before
    fun setup() {
        mockContext = mockk()
        mockRoutingRepository = mockk()
        mockkConstructor(Geocoder::class)
        viewModel = MapsViewModel(mockRoutingRepository, mockContext)
    }

    @After
    fun teardown() {
        unmockkAll()
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun testInitialState() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.origin)
            assertNull(state.destination)
            assertFalse(state.isLoadingRoute)
            // It launches fetchAlerts on init, so we might just cancel here
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testClearRouteResetsState() = runTest {
        viewModel.clearRoute()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.destination)
            assertNull(state.routeGeoJson)
            assertNull(state.distanceMeters)
            assertFalse(state.isLoadingRoute)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testOnDestinationSelectedSetsDestinationAndRequestsRoute() = runTest {
        val origin = Position(longitude = -122.0, latitude = 37.0)
        val destination = Position(longitude = -121.0, latitude = 36.0)
        
        val routeResult = RouteResult(
            polyline = listOf(origin, destination),
            distanceMeters = 1000.0,
            durationSeconds = 60.0,
            steps = emptyList()
        )
        
        coEvery { mockRoutingRepository.fetchRoute(any(), any()) } returns Result.success(routeResult)

        // Set origin first
        viewModel.onUserLocationUpdated(origin)
        
        // Select destination
        viewModel.onDestinationSelected(destination)

        viewModel.uiState.test {
            var finalState = awaitItem()
            // Wait until route loading finishes
            while (finalState.routeGeoJson == null && finalState.routeError == null) {
                finalState = awaitItem()
            }
            
            assertNotNull(finalState.destination)
            assertNotNull(finalState.routeGeoJson)
            assertEquals(1000.0, finalState.distanceMeters)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
