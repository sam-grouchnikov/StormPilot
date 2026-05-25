package com.example.stormpilot.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class RoutingParsingExtendedTest {

    @Test
    fun testToGeoJsonLineString() {
        val points = listOf(
            Position(longitude = -122.0, latitude = 37.0),
            Position(longitude = -121.0, latitude = 36.0)
        )
        val geoJson = RoutingParsing.toGeoJsonLineString(points)
        
        // Assert coordinates are formatted correctly in JSON
        assertTrue(geoJson.contains("LineString"))
        assertTrue(geoJson.contains("[-122.0,37.0]"))
        assertTrue(geoJson.contains("[-121.0,36.0]"))
    }

    @Test
    fun testParseOsrmRouteEmptyRoutesThrowsException() {
        val payload = """
            {
              "routes": []
            }
        """.trimIndent()

        try {
            RoutingParsing.parseOsrmRoute(payload)
            fail("Should have thrown IllegalArgumentException due to empty routes")
        } catch (e: IllegalArgumentException) {
            assertEquals("No route returned", e.message)
        }
    }

    @Test
    fun testParseOsrmRouteFallbackInstructions() {
        // Test buildInstruction with maneuver types
        val payload = """
            {
              "routes": [
                {
                  "distance": 500.0,
                  "duration": 120.0,
                  "geometry": {
                    "coordinates": [[-122.1,37.4]]
                  },
                  "legs": [
                    {
                      "steps": [
                        {
                          "distance": 500.0,
                          "duration": 120.0,
                          "name": "Main St",
                          "maneuver": {
                            "type": "turn",
                            "modifier": "left",
                            "location": [-122.1,37.4],
                            "bearing_before": 0,
                            "bearing_after": 90
                          }
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val result = RoutingParsing.parseOsrmRoute(payload)
        val step = result.steps.first()
        // Instruction should be built: "Turn left onto Main St"
        assertEquals("Turn left onto Main St", step.instruction)
    }
}
