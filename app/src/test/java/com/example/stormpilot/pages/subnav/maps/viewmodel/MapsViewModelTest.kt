package com.example.stormpilot.pages.subnav.maps.viewmodel

import com.example.stormpilot.pages.subnav.maps.routing.RouteResult
import com.example.stormpilot.pages.subnav.maps.routing.RouteStep
import com.example.stormpilot.pages.subnav.maps.routing.RoutingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalCoroutinesApi::class)
class MapsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `request route success updates state`() = runTest {
        val viewModel = MapsViewModel(FakeRoutingRepository(Result.success(routeResult())))

        viewModel.onUserLocationUpdated(Position(-122.1, 37.4))
        viewModel.onDestinationSelected(Position(-122.2, 37.5))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRoute)
        assertNotNull(state.routeGeoJson)
        assertEquals(2000.0, state.distanceMeters ?: 0.0, 0.01)
        assertEquals(1, state.steps.size)
    }

    @Test
    fun `request route failure surfaces error`() = runTest {
        val viewModel = MapsViewModel(FakeRoutingRepository(Result.failure(IllegalStateException("boom"))))

        viewModel.onUserLocationUpdated(Position(-122.1, 37.4))
        viewModel.onDestinationSelected(Position(-122.2, 37.5))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRoute)
        assertEquals("boom", state.routeError)
        assertTrue(state.steps.isEmpty())
    }

    private fun routeResult(): RouteResult = RouteResult(
        polyline = listOf(Position(-122.1, 37.4), Position(-122.2, 37.5)),
        distanceMeters = 2000.0,
        durationSeconds = 720.0,
        steps = listOf(RouteStep("Head north", 200.0, 60.0)),
    )
}

private class FakeRoutingRepository(
    private val result: Result<RouteResult>,
) : RoutingRepository {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> = result
}
