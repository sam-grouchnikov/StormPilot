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

    @Test
    fun `location updates advance maneuvers and remaining summary`() = runTest {
        val stepOneManeuver = Position(-122.1005, 37.4005)
        val viewModel = MapsViewModel(
            FakeRoutingRepository(
                Result.success(
                    RouteResult(
                        polyline = listOf(Position(-122.1, 37.4), Position(-122.2, 37.5)),
                        distanceMeters = 2000.0,
                        durationSeconds = 600.0,
                        steps = listOf(
                            RouteStep("Head north", 500.0, 120.0, stepOneManeuver),
                            RouteStep("Turn right", 1500.0, 480.0, Position(-122.2, 37.5)),
                        ),
                    )
                )
            )
        )

        viewModel.onUserLocationUpdated(Position(-122.1, 37.4))
        viewModel.onDestinationSelected(Position(-122.2, 37.5))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onUserLocationUpdated(stepOneManeuver)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentStepIndex)
        assertEquals(1500.0, state.remainingDistanceMeters ?: 0.0, 0.01)
        assertEquals(480.0, state.remainingDurationSeconds ?: 0.0, 0.01)
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
