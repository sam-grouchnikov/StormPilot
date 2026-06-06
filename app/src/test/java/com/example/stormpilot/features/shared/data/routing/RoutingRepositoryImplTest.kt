package com.example.stormpilot.features.shared.data.routing

import com.example.stormpilot.features.shared.data.api.RouteSelectionResponse
import com.example.stormpilot.features.shared.data.api.RouteViaRequest
import com.example.stormpilot.features.shared.data.api.RouteWarningAnalysis
import com.example.stormpilot.features.shared.data.api.RouteWarningAnalyzeRequest
import com.example.stormpilot.features.shared.data.api.StormPilotApi
import com.example.stormpilot.features.shared.data.api.toApiPosition
import com.example.stormpilot.testing.StormPilotUnitTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class RoutingRepositoryImplTest : StormPilotUnitTest() {
    private val api = mockk<StormPilotApi>()
    private val repository = RoutingRepositoryImpl(api)
    private val origin = Position(longitude = -97.0, latitude = 35.0)
    private val destination = Position(longitude = -96.0, latitude = 36.0)

    @Test
    fun fetchRoute_returnsRouteFromBackendSelection() = runTest {
        val expected = route(100.0)
        coEvery {
            api.route(
                originLatitude = 35.0,
                originLongitude = -97.0,
                destinationLatitude = 36.0,
                destinationLongitude = -96.0,
                avoidStorms = false,
                includeWarnings = true,
            )
        } returns selection(expected)

        val result = repository.fetchRoute(origin, destination)

        assertSame(expected, result.getOrThrow())
    }

    @Test
    fun fetchRouteSelection_forwardsStormAwareFlagsAndPreservesResponse() = runTest {
        val response = selection(
            route = route(200.0),
            warningCount = 1,
            warningError = "No storm-free road route was found. This route passes through a storm warning polygon.",
            stormRouteAlertMessage = "Heads up",
        )
        coEvery {
            api.route(
                originLatitude = 35.0,
                originLongitude = -97.0,
                destinationLatitude = 36.0,
                destinationLongitude = -96.0,
                avoidStorms = true,
                includeWarnings = true,
            )
        } returns response

        val result = repository.fetchRouteSelection(
            origin = origin,
            destination = destination,
            avoidStorms = true,
            includeWarnings = true,
        )

        assertSame(response, result.getOrThrow())
    }

    @Test
    fun fetchRouteCandidates_delegatesToBackendCandidatesEndpoint() = runTest {
        val candidates = listOf(route(100.0), route(200.0))
        coEvery {
            api.routeCandidates(
                originLatitude = 35.0,
                originLongitude = -97.0,
                destinationLatitude = 36.0,
                destinationLongitude = -96.0,
            )
        } returns candidates

        val result = repository.fetchRouteCandidates(origin, destination)

        assertEquals(candidates, result.getOrThrow())
    }

    @Test
    fun fetchRouteVia_sendsApiPositionsAndReturnsSelectedRoute() = runTest {
        val waypoint = Position(longitude = -96.5, latitude = 35.5)
        val expected = route(300.0)
        val request = RouteViaRequest(
            origin = origin.toApiPosition(),
            destination = destination.toApiPosition(),
            waypoints = listOf(waypoint.toApiPosition()),
            includeWarnings = true,
        )
        coEvery { api.routeVia(request) } returns selection(expected)

        val result = repository.fetchRouteVia(
            origin = origin,
            destination = destination,
            waypoints = listOf(waypoint),
        )

        assertSame(expected, result.getOrThrow())
        coVerify(exactly = 1) { api.routeVia(request) }
    }

    @Test
    fun analyzeRouteWarnings_sendsPolylineDestinationAndOptionalGeoJson() = runTest {
        val expected = RouteWarningAnalysis(
            warningCount = 1,
            destinationInsideWarning = false,
        )
        val request = RouteWarningAnalyzeRequest(
            routePolyline = listOf(origin.toApiPosition(), destination.toApiPosition()),
            destination = destination.toApiPosition(),
            alertsGeoJson = """{"type":"FeatureCollection","features":[]}""",
        )
        coEvery { api.analyzeRouteWarnings(request) } returns expected

        val result = repository.analyzeRouteWarnings(
            routePolyline = listOf(origin, destination),
            destination = destination,
            alertsGeoJson = """{"type":"FeatureCollection","features":[]}""",
        )

        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { api.analyzeRouteWarnings(request) }
    }

    private fun selection(
        route: RouteResult,
        warningCount: Int? = 0,
        warningError: String? = null,
        stormRouteAlertMessage: String? = null,
    ): RouteSelectionResponse =
        RouteSelectionResponse(
            route = route,
            routeGeoJson = """{"type":"FeatureCollection","features":[]}""",
            warningCount = warningCount,
            warningError = warningError,
            stormRouteAlertMessage = stormRouteAlertMessage,
        )

    private fun route(durationSeconds: Double): RouteResult =
        RouteResult(
            polyline = listOf(origin, destination),
            distanceMeters = 1_000.0,
            durationSeconds = durationSeconds,
            steps = emptyList(),
        )
}
