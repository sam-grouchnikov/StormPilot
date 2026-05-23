package com.example.stormpilot.features.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class RouteWarningCounterTest {
    @Test
    fun countsWarningsCrossedByRoute() {
        val count = RouteWarningCounter.countWarningsIntersectingRoute(
            alertsGeoJson = featureCollection(
                polygon(
                    listOf(
                        listOf(0.0, 0.0),
                        listOf(1.0, 0.0),
                        listOf(1.0, 1.0),
                        listOf(0.0, 1.0),
                        listOf(0.0, 0.0),
                    )
                ),
                polygon(
                    listOf(
                        listOf(5.0, 5.0),
                        listOf(6.0, 5.0),
                        listOf(6.0, 6.0),
                        listOf(5.0, 6.0),
                        listOf(5.0, 5.0),
                    )
                ),
            ),
            routePolyline = listOf(
                Position(longitude = -1.0, latitude = 0.5),
                Position(longitude = 2.0, latitude = 0.5),
            ),
        )

        assertEquals(1, count)
    }

    @Test
    fun countsWarningsContainingRoute() {
        val count = RouteWarningCounter.countWarningsIntersectingRoute(
            alertsGeoJson = featureCollection(
                polygon(
                    listOf(
                        listOf(0.0, 0.0),
                        listOf(4.0, 0.0),
                        listOf(4.0, 4.0),
                        listOf(0.0, 4.0),
                        listOf(0.0, 0.0),
                    )
                )
            ),
            routePolyline = listOf(
                Position(longitude = 1.0, latitude = 1.0),
                Position(longitude = 2.0, latitude = 2.0),
            ),
        )

        assertEquals(1, count)
    }

    @Test
    fun ignoresRoutesInsidePolygonHoles() {
        val count = RouteWarningCounter.countWarningsIntersectingRoute(
            alertsGeoJson = featureCollection(
                polygon(
                    listOf(
                        listOf(0.0, 0.0),
                        listOf(4.0, 0.0),
                        listOf(4.0, 4.0),
                        listOf(0.0, 4.0),
                        listOf(0.0, 0.0),
                    ),
                    listOf(
                        listOf(1.0, 1.0),
                        listOf(3.0, 1.0),
                        listOf(3.0, 3.0),
                        listOf(1.0, 3.0),
                        listOf(1.0, 1.0),
                    )
                )
            ),
            routePolyline = listOf(
                Position(longitude = 1.5, latitude = 2.0),
                Position(longitude = 2.5, latitude = 2.0),
            ),
        )

        assertEquals(0, count)
    }

    private fun featureCollection(vararg geometries: String): String =
        """
        {
          "type": "FeatureCollection",
          "features": [
            ${geometries.joinToString(",") { """{"type":"Feature","geometry":$it,"properties":{}}""" }}
          ]
        }
        """.trimIndent()

    private fun polygon(vararg rings: List<List<Double>>): String =
        """
        {
          "type": "Polygon",
          "coordinates": [
            ${rings.joinToString(",") { ring -> ring.joinToString(prefix = "[", postfix = "]") { "[${it[0]},${it[1]}]" } }}
          ]
        }
        """.trimIndent()
}
