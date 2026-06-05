package com.example.stormpilot.features.map.data.routing

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class StormRoutePolicyTest : StormPilotUnitTest() {
    @Test
    fun selectRoute_prefersStormFreeCandidate() {
        val stormRoute = route(
            polyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
            durationSeconds = 100.0,
            distanceMeters = 1_000.0,
        )
        val clearRoute = route(
            polyline = listOf(position(-1.0, 2.0), position(2.0, 2.0)),
            durationSeconds = 500.0,
            distanceMeters = 5_000.0,
        )

        val selection = StormRoutePolicy.selectRoute(
            candidates = listOf(stormRoute, clearRoute),
            alertsGeoJson = squareWarningGeoJson(),
            destination = position(2.0, 2.0),
        )

        assertSame(clearRoute, selection.route)
        assertEquals(0, selection.warningCount)
        assertNull(selection.warningMessage)
    }

    @Test
    fun selectRoute_allowsWarningRouteWhenDestinationIsInsidePolygon() {
        val stormRoute = route(
            polyline = listOf(position(-1.0, 0.5), position(0.5, 0.5)),
            durationSeconds = 100.0,
            distanceMeters = 1_000.0,
        )

        val selection = StormRoutePolicy.selectRoute(
            candidates = listOf(stormRoute),
            alertsGeoJson = squareWarningGeoJson(),
            destination = position(0.5, 0.5),
        )

        assertSame(stormRoute, selection.route)
        assertEquals(1, selection.warningCount)
        assertEquals(StormRoutePolicy.DESTINATION_IN_WARNING_MESSAGE, selection.warningMessage)
    }

    @Test
    fun selectRoute_allowsWarningRouteWhenNoStormFreeCandidateExists() {
        val shorterStormRoute = route(
            polyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
            durationSeconds = 100.0,
            distanceMeters = 1_000.0,
        )
        val longerStormRoute = route(
            polyline = listOf(position(0.5, -1.0), position(0.5, 2.0)),
            durationSeconds = 500.0,
            distanceMeters = 5_000.0,
        )

        val selection = StormRoutePolicy.selectRoute(
            candidates = listOf(longerStormRoute, shorterStormRoute),
            alertsGeoJson = squareWarningGeoJson(),
            destination = position(2.0, 0.5),
        )

        assertSame(shorterStormRoute, selection.route)
        assertEquals(1, selection.warningCount)
        assertEquals(StormRoutePolicy.NO_STORM_FREE_ROUTE_MESSAGE, selection.warningMessage)
    }

    @Test
    fun selectRoute_throwsWhenNoCandidatesAreAvailable() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            StormRoutePolicy.selectRoute(
                candidates = emptyList(),
                alertsGeoJson = emptyFeatureCollection(),
                destination = position(0.0, 0.0),
            )
        }

        assertEquals("No route returned", error.message)
    }

    @Test
    fun selectRoute_ordersClearRoutesByDurationThenDistance() {
        val slowerClearRoute = route(
            polyline = listOf(position(-1.0, 2.0), position(2.0, 2.0)),
            durationSeconds = 200.0,
            distanceMeters = 100.0,
        )
        val fasterLongerClearRoute = route(
            polyline = listOf(position(-1.0, 3.0), position(2.0, 3.0)),
            durationSeconds = 100.0,
            distanceMeters = 10_000.0,
        )

        val selection = StormRoutePolicy.selectRoute(
            candidates = listOf(slowerClearRoute, fasterLongerClearRoute),
            alertsGeoJson = squareWarningGeoJson(),
            destination = position(2.0, 2.0),
        )

        assertSame(fasterLongerClearRoute, selection.route)
        assertEquals(0, selection.warningCount)
        assertNull(selection.warningMessage)
    }

    @Test
    fun selectRoute_tieBreaksWarnedRoutesByDurationThenDistance() {
        val longerWarnedRoute = route(
            polyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
            durationSeconds = 100.0,
            distanceMeters = 10_000.0,
        )
        val shorterWarnedRoute = route(
            polyline = listOf(position(0.5, -1.0), position(0.5, 2.0)),
            durationSeconds = 100.0,
            distanceMeters = 1_000.0,
        )

        val selection = StormRoutePolicy.selectRoute(
            candidates = listOf(longerWarnedRoute, shorterWarnedRoute),
            alertsGeoJson = squareWarningGeoJson(),
            destination = position(2.0, 2.0),
        )

        assertSame(shorterWarnedRoute, selection.route)
        assertEquals(1, selection.warningCount)
        assertEquals(StormRoutePolicy.NO_STORM_FREE_ROUTE_MESSAGE, selection.warningMessage)
    }

    @Test
    fun analyzeWarnings_countsDestinationInsidePolygonEvenWhenRouteDoesNotIntersect() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = squareWarningGeoJson(),
            routePolyline = listOf(position(-1.0, 2.0), position(2.0, 2.0)),
            destination = position(0.5, 0.5),
        )

        assertEquals(1, analysis.warningCount)
        assertTrue(analysis.destinationInsideWarning)
    }

    @Test
    fun warningBoundsIntersectingRoute_returnsBoundsForCrossedPolygon() {
        val bounds = RouteWarningCounter.warningBoundsIntersectingRoute(
            alertsGeoJson = squareWarningGeoJson(),
            routePolyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
        )

        assertEquals(1, bounds.size)
        assertEquals(0.0, bounds.first().minLongitude, 0.0)
        assertEquals(0.0, bounds.first().minLatitude, 0.0)
        assertEquals(1.0, bounds.first().maxLongitude, 0.0)
        assertEquals(1.0, bounds.first().maxLatitude, 0.0)
    }

    @Test
    fun buildWaypointSets_generatesDetoursAroundIntersectedWarning() {
        val stormRoute = route(
            polyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
            durationSeconds = 100.0,
            distanceMeters = 1_000.0,
        )

        val waypointSets = StormDetourPlanner.buildWaypointSets(
            alertsGeoJson = squareWarningGeoJson(),
            seedRoutes = listOf(stormRoute),
            origin = position(-1.0, 0.5),
            destination = position(2.0, 0.5),
        )

        assertFalse(waypointSets.isEmpty())
        assertTrue(waypointSets.first().single().latitude > 1.0)
        assertTrue(waypointSets.any { set -> set.any { it.latitude < 0.0 } })
        assertTrue(waypointSets.any { set -> set.size == 2 })
    }

    @Test
    fun buildWaypointSets_returnsEmptyWhenSeedRoutesDoNotCrossWarnings() {
        val clearRoute = route(
            polyline = listOf(position(-1.0, 2.0), position(2.0, 2.0)),
            durationSeconds = 100.0,
            distanceMeters = 1_000.0,
        )

        val waypointSets = StormDetourPlanner.buildWaypointSets(
            alertsGeoJson = squareWarningGeoJson(),
            seedRoutes = listOf(clearRoute),
            origin = position(-1.0, 2.0),
            destination = position(2.0, 2.0),
        )

        assertTrue(waypointSets.isEmpty())
    }

    @Test
    fun buildWaypointSets_capsLargeWarningSearchAtThirtyTwoUniqueSets() {
        val seedRoutes = listOf(
            route(
                polyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
                durationSeconds = 100.0,
                distanceMeters = 1_000.0,
            ),
            route(
                polyline = listOf(position(2.0, 0.5), position(5.0, 0.5)),
                durationSeconds = 100.0,
                distanceMeters = 1_000.0,
            ),
            route(
                polyline = listOf(position(5.0, 0.5), position(8.0, 0.5)),
                durationSeconds = 100.0,
                distanceMeters = 1_000.0,
            ),
        )

        val waypointSets = StormDetourPlanner.buildWaypointSets(
            alertsGeoJson = threeWarningGeoJson(),
            seedRoutes = seedRoutes,
            origin = position(-1.0, 0.5),
            destination = position(8.0, 0.5),
        )

        assertEquals(32, waypointSets.size)
        assertEquals(waypointSets.distinct(), waypointSets)
        assertNotNull(waypointSets.firstOrNull { it.size == 2 })
    }

    private fun route(
        polyline: List<Position>,
        durationSeconds: Double,
        distanceMeters: Double,
    ): RouteResult {
        return RouteResult(
            polyline = polyline,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            steps = emptyList(),
        )
    }

    private fun position(longitude: Double, latitude: Double): Position {
        return Position(longitude = longitude, latitude = latitude)
    }

    private fun emptyFeatureCollection(): String {
        return """{"type":"FeatureCollection","features":[]}"""
    }

    private fun squareWarningGeoJson(): String {
        return """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": { "prod_type": "Tornado Warning" },
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                      [
                        [0.0, 0.0],
                        [1.0, 0.0],
                        [1.0, 1.0],
                        [0.0, 1.0],
                        [0.0, 0.0]
                      ]
                    ]
                  }
                }
              ]
            }
        """.trimIndent()
    }

    private fun threeWarningGeoJson(): String {
        return """
            {
              "type": "FeatureCollection",
              "features": [
                ${warningFeature(0.0, 0.0, 1.0, 1.0)},
                ${warningFeature(3.0, 0.0, 4.0, 1.0)},
                ${warningFeature(6.0, 0.0, 7.0, 1.0)}
              ]
            }
        """.trimIndent()
    }

    private fun warningFeature(minLon: Double, minLat: Double, maxLon: Double, maxLat: Double): String {
        return """
            {
              "type": "Feature",
              "properties": { "prod_type": "Tornado Warning" },
              "geometry": {
                "type": "Polygon",
                "coordinates": [[
                  [$minLon, $minLat],
                  [$maxLon, $minLat],
                  [$maxLon, $maxLat],
                  [$minLon, $maxLat],
                  [$minLon, $minLat]
                ]]
              }
            }
        """.trimIndent()
    }
}
