package com.example.stormpilot.features.shared.viewmodels

import com.example.stormpilot.features.shared.data.routing.RouteStep
import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class MapNavigationStateTest : StormPilotUnitTest() {
    @Test
    fun approximateDistanceMeters_handlesLatitudeAndLongitudeChanges() {
        assertEquals(
            111_320.0,
            approximateDistanceMeters(position(0.0, 0.0), position(0.0, 1.0)),
            1.0,
        )
        assertEquals(
            111_320.0,
            approximateDistanceMeters(position(0.0, 0.0), position(1.0, 0.0)),
            1.0,
        )
    }

    @Test
    fun bearingDegrees_returnsCompassBearings() {
        assertEquals(0.0, bearingDegrees(position(0.0, 0.0), position(0.0, 1.0)), 0.0001)
        assertEquals(90.0, bearingDegrees(position(0.0, 0.0), position(1.0, 0.0)), 0.0001)
        assertEquals(180.0, bearingDegrees(position(0.0, 0.0), position(0.0, -1.0)), 0.0001)
        assertEquals(270.0, bearingDegrees(position(0.0, 0.0), position(-1.0, 0.0)), 0.0001)
    }

    @Test
    fun routeBearingDegrees_returnsNearestForwardSegmentBearing() {
        val route = listOf(
            position(0.0, 0.0),
            position(0.01, 0.0),
            position(0.01, 0.01),
        )

        assertEquals(90.0, routeBearingDegrees(position(0.004, 0.0001), route)!!, 0.0001)
        assertEquals(0.0, routeBearingDegrees(position(0.0101, 0.006), route)!!, 0.0001)
    }

    @Test
    fun navigationCameraBearingDegrees_prefersRouteBearingWhileOnRoute() {
        val route = listOf(
            position(0.0, 0.0),
            position(0.01, 0.0),
            position(0.01, 0.01),
        )

        assertEquals(
            90.0,
            navigationCameraBearingDegrees(
                userPosition = position(0.002, 0.0),
                routePolyline = route,
                isOffRoute = false,
                userBearingDegrees = 12.0,
            )!!,
            0.0001,
        )
    }

    @Test
    fun navigationCameraBearingDegrees_usesUserBearingWhenOffRoute() {
        val route = listOf(
            position(0.0, 0.0),
            position(0.01, 0.0),
        )

        assertEquals(
            225.0,
            navigationCameraBearingDegrees(
                userPosition = position(0.0, 0.01),
                routePolyline = route,
                isOffRoute = true,
                userBearingDegrees = 225.0,
                previousBearingDegrees = 90.0,
            )!!,
            0.0001,
        )
    }

    @Test
    fun navigationCameraBearingDegrees_keepsPreviousBearingOffRouteWhenUserBearingMissing() {
        val route = listOf(
            position(0.0, 0.0),
            position(0.01, 0.0),
        )

        assertEquals(
            90.0,
            navigationCameraBearingDegrees(
                userPosition = position(0.0, 0.01),
                routePolyline = route,
                isOffRoute = true,
                userBearingDegrees = null,
                previousBearingDegrees = 90.0,
            )!!,
            0.0001,
        )
    }

    @Test
    fun navigationInstruction_returnsFallbackWhenNoStepIsAvailable() {
        assertEquals("Continue on route", navigationInstruction(MapsUiState()))
        assertEquals(
            "Continue on route",
            navigationInstruction(
                MapsUiState(
                    steps = listOf(step("Turn left")),
                    currentStepIndex = 9,
                ),
            ),
        )
    }

    @Test
    fun navigationInstruction_returnsCurrentStepInstructionBeforeLastStep() {
        val state = MapsUiState(
            steps = listOf(step("Turn left"), step("Arrive")),
            currentStepIndex = 0,
        )

        assertEquals("Turn left", navigationInstruction(state))
    }

    @Test
    fun navigationInstruction_tellsUserToHeadStraightOnLastStepUntilCloseToDestination() {
        val state = MapsUiState(
            origin = position(0.0, 0.0),
            destination = position(0.0, 0.01),
            steps = listOf(step("Turn left"), step("Arrive")),
            currentStepIndex = 1,
        )

        assertEquals("Head straight", navigationInstruction(state))
    }

    @Test
    fun navigationInstruction_returnsLastStepWhenCloseToDestination() {
        val state = MapsUiState(
            origin = position(0.0, 0.0),
            destination = position(0.0, 0.0001),
            steps = listOf(step("Turn left"), step("Arrive")),
            currentStepIndex = 1,
        )

        assertEquals("Arrive", navigationInstruction(state))
    }

    private fun step(instruction: String): RouteStep {
        return RouteStep(
            instruction = instruction,
            distanceMeters = 100.0,
            durationSeconds = 60.0,
            maneuverLocation = null,
            maneuver = null,
        )
    }

    private fun position(longitude: Double, latitude: Double): Position {
        return Position(longitude = longitude, latitude = latitude)
    }
}
