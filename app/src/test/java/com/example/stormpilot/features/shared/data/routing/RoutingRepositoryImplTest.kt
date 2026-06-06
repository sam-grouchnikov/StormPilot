package com.example.stormpilot.features.shared.data.routing

import com.example.stormpilot.testing.StormPilotUnitTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class RoutingRepositoryImplTest : StormPilotUnitTest() {
    private val origin = Position(longitude = -97.0, latitude = 35.0)
    private val destination = Position(longitude = -96.0, latitude = 36.0)

    @Test
    fun valhallaClient_returnsUnsupportedUntilEndpointIsConfigured() = runTest {
        val client = ValhallaRoutingApiClient()

        val route = client.fetchRoute(origin, destination)
        val candidates = client.fetchRouteCandidates(origin, destination)
        val via = client.fetchRouteVia(origin, destination, listOf(Position(-96.5, 35.5)))

        assertTrue(route.exceptionOrNull() is UnsupportedOperationException)
        assertTrue(candidates.exceptionOrNull() is UnsupportedOperationException)
        assertTrue(via.exceptionOrNull() is UnsupportedOperationException)
    }

    @Test
    fun fetchRoute_usesValhallaSuccessWithoutFallback() = runTest {
        val expected = route(100.0)
        val valhalla = mockk<ValhallaRoutingApiClient>()
        val osrm = mockk<OsrmRoutingApiClient>(relaxed = true)
        coEvery { valhalla.fetchRoute(origin, destination) } returns Result.success(expected)

        val result = RoutingRepositoryImpl(valhalla, osrm).fetchRoute(origin, destination)

        assertSame(expected, result.getOrThrow())
        coVerify(exactly = 0) { osrm.fetchRoute(any(), any()) }
    }

    @Test
    fun fetchRoute_fallsBackToOsrmWhenValhallaFails() = runTest {
        val expected = route(200.0)
        val valhalla = mockk<ValhallaRoutingApiClient>()
        val osrm = mockk<OsrmRoutingApiClient>()
        coEvery { valhalla.fetchRoute(origin, destination) } returns Result.failure(IllegalStateException("nope"))
        coEvery { osrm.fetchRoute(origin, destination) } returns Result.success(expected)

        val result = RoutingRepositoryImpl(valhalla, osrm).fetchRoute(origin, destination)

        assertSame(expected, result.getOrThrow())
        coVerify(exactly = 1) { osrm.fetchRoute(origin, destination) }
    }

    @Test
    fun fetchRouteCandidates_fallsBackAndPreservesCandidateList() = runTest {
        val candidates = listOf(route(100.0), route(200.0))
        val valhalla = mockk<ValhallaRoutingApiClient>()
        val osrm = mockk<OsrmRoutingApiClient>()
        coEvery { valhalla.fetchRouteCandidates(origin, destination) } returns Result.failure(IllegalStateException("nope"))
        coEvery { osrm.fetchRouteCandidates(origin, destination) } returns Result.success(candidates)

        val result = RoutingRepositoryImpl(valhalla, osrm).fetchRouteCandidates(origin, destination)

        assertEquals(candidates, result.getOrThrow())
    }

    @Test
    fun fetchRouteVia_fallsBackWithProvidedWaypoints() = runTest {
        val waypoint = Position(longitude = -96.5, latitude = 35.5)
        val expected = route(300.0)
        val valhalla = mockk<ValhallaRoutingApiClient>()
        val osrm = mockk<OsrmRoutingApiClient>()
        coEvery {
            valhalla.fetchRouteVia(origin, destination, listOf(waypoint))
        } returns Result.failure(IllegalStateException("nope"))
        coEvery {
            osrm.fetchRouteVia(origin, destination, listOf(waypoint))
        } returns Result.success(expected)

        val result = RoutingRepositoryImpl(valhalla, osrm).fetchRouteVia(
            origin = origin,
            destination = destination,
            waypoints = listOf(waypoint),
        )

        assertSame(expected, result.getOrThrow())
    }

    private fun route(durationSeconds: Double): RouteResult {
        return RouteResult(
            polyline = listOf(origin, destination),
            distanceMeters = 1_000.0,
            durationSeconds = durationSeconds,
            steps = emptyList(),
        )
    }
}
