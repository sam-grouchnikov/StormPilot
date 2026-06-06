package com.example.stormpilot.features.shared.data.routing

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class RouteWarningCounterTest : StormPilotUnitTest() {
    @Test
    fun analyzeWarnings_returnsZeroWhenFeatureCollectionHasNoFeatures() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = """{"type":"FeatureCollection"}""",
            routePolyline = listOf(position(-1.0, -1.0), position(1.0, 1.0)),
            destination = position(0.0, 0.0),
        )

        assertEquals(0, analysis.warningCount)
        assertFalse(analysis.destinationInsideWarning)
    }

    @Test
    fun analyzeWarnings_ignoresUnsupportedGeometryTypesAndMalformedFeatures() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = """
                {
                  "type": "FeatureCollection",
                  "features": [
                    { "type": "Feature", "geometry": { "type": "Point", "coordinates": [0, 0] } },
                    { "type": "Feature", "properties": {} },
                    { "type": "Feature", "geometry": { "type": "Polygon", "coordinates": [[[0]]] } }
                  ]
                }
            """.trimIndent(),
            routePolyline = listOf(position(-1.0, -1.0), position(1.0, 1.0)),
        )

        assertEquals(0, analysis.warningCount)
        assertFalse(analysis.destinationInsideWarning)
    }

    @Test
    fun analyzeWarnings_countsRoutePointInsidePolygon() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = squareWarningGeoJson(),
            routePolyline = listOf(position(0.5, 0.5), position(2.0, 2.0)),
        )

        assertEquals(1, analysis.warningCount)
        assertFalse(analysis.destinationInsideWarning)
    }

    @Test
    fun analyzeWarnings_countsRouteSegmentCrossingPolygonBoundary() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = squareWarningGeoJson(),
            routePolyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
        )

        assertEquals(1, analysis.warningCount)
    }

    @Test
    fun analyzeWarnings_countsRouteSegmentTouchingPolygonBoundary() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = squareWarningGeoJson(),
            routePolyline = listOf(position(-1.0, 0.0), position(2.0, 0.0)),
        )

        assertEquals(1, analysis.warningCount)
    }

    @Test
    fun analyzeWarnings_excludesDestinationInsidePolygonHole() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = polygonWithHoleGeoJson(),
            routePolyline = listOf(position(-2.0, -2.0), position(-1.0, -1.0)),
            destination = position(1.0, 1.0),
        )

        assertEquals(0, analysis.warningCount)
        assertFalse(analysis.destinationInsideWarning)
    }

    @Test
    fun analyzeWarnings_countsMultiPolygonContainingDestination() {
        val analysis = RouteWarningCounter.analyzeWarnings(
            alertsGeoJson = multiPolygonGeoJson(),
            routePolyline = listOf(position(-2.0, -2.0), position(-1.0, -1.0)),
            destination = position(4.5, 4.5),
        )

        assertEquals(1, analysis.warningCount)
        assertTrue(analysis.destinationInsideWarning)
    }

    @Test
    fun countWarningsIntersectingRoute_returnsOnlyRouteIntersections() {
        val count = RouteWarningCounter.countWarningsIntersectingRoute(
            alertsGeoJson = twoWarningGeoJson(),
            routePolyline = listOf(position(-1.0, 0.5), position(2.0, 0.5)),
        )

        assertEquals(1, count)
    }

    @Test
    fun warningBoundsIntersectingRoute_returnsEmptyForShortRoute() {
        assertTrue(
            RouteWarningCounter.warningBoundsIntersectingRoute(
                alertsGeoJson = squareWarningGeoJson(),
                routePolyline = listOf(position(0.0, 0.0)),
            ).isEmpty(),
        )
    }

    @Test
    fun warningBoundsIntersectingRoute_returnsBoundsForEachIntersectedMultiPolygonPart() {
        val bounds = RouteWarningCounter.warningBoundsIntersectingRoute(
            alertsGeoJson = multiPolygonGeoJson(),
            routePolyline = listOf(
                position(-1.0, 0.5),
                position(2.0, 0.5),
                position(4.5, 3.0),
                position(4.5, 6.0),
            ),
        )

        assertEquals(2, bounds.size)
        assertEquals(RouteWarningBounds(0.0, 0.0, 1.0, 1.0), bounds[0])
        assertEquals(RouteWarningBounds(4.0, 4.0, 5.0, 5.0), bounds[1])
    }

    private fun position(longitude: Double, latitude: Double): Position {
        return Position(longitude = longitude, latitude = latitude)
    }

    private fun squareWarningGeoJson(): String {
        return """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[0,0],[1,0],[1,1],[0,1],[0,0]]]
                  }
                }
              ]
            }
        """.trimIndent()
    }

    private fun twoWarningGeoJson(): String {
        return """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[0,0],[1,0],[1,1],[0,1],[0,0]]]
                  }
                },
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[3,3],[4,3],[4,4],[3,4],[3,3]]]
                  }
                }
              ]
            }
        """.trimIndent()
    }

    private fun polygonWithHoleGeoJson(): String {
        return """
            {
              "type": "FeatureCollection",
              "features": [{
                "type": "Feature",
                "geometry": {
                  "type": "Polygon",
                  "coordinates": [
                    [[0,0],[3,0],[3,3],[0,3],[0,0]],
                    [[0.5,0.5],[1.5,0.5],[1.5,1.5],[0.5,1.5],[0.5,0.5]]
                  ]
                }
              }]
            }
        """.trimIndent()
    }

    private fun multiPolygonGeoJson(): String {
        return """
            {
              "type": "FeatureCollection",
              "features": [{
                "type": "Feature",
                "geometry": {
                  "type": "MultiPolygon",
                  "coordinates": [
                    [[[0,0],[1,0],[1,1],[0,1],[0,0]]],
                    [[[4,4],[5,4],[5,5],[4,5],[4,4]]]
                  ]
                }
              }]
            }
        """.trimIndent()
    }
}
