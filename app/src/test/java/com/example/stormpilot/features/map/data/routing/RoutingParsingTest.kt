package com.example.stormpilot.features.map.data.routing

import com.example.stormpilot.data.RoutingParsing
import com.example.stormpilot.data.formatDistance
import com.example.stormpilot.data.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingParsingTest {

    @Test
    fun `parseOsrmRoute parses summary geometry and steps`() {
        val payload = """
            {
              "routes": [
                {
                  "distance": 1200.5,
                  "duration": 610.0,
                  "geometry": {
                    "coordinates": [[-122.1,37.4],[-122.2,37.5]]
                  },
                  "legs": [
                    {
                      "steps": [
                        {
                          "distance": 300.0,
                          "duration": 180.0,
                          "name": "Main St",
                          "maneuver": {"instruction": "Head north", "location": [-122.1,37.4]}
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val result = RoutingParsing.parseOsrmRoute(payload)

        assertEquals(1200.5, result.distanceMeters, 0.001)
        assertEquals(610.0, result.durationSeconds, 0.001)
        assertEquals(2, result.polyline.size)
        assertEquals("Head north", result.steps.first().instruction)
        assertEquals(-122.1, result.steps.first().maneuverLocation?.longitude ?: 0.0, 0.001)
    }

    @Test
    fun `formatters produce user friendly text`() {
        assertEquals("1.0 mi", formatDistance(1609.344))
        assertEquals("12 min", formatDuration(720.0))
        assertTrue(formatDuration(3900.0).startsWith("1 hr"))
    }
}
