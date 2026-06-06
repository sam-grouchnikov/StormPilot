package com.example.stormpilot.features.shared.data.search

import com.example.stormpilot.testing.StormPilotUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class PhotonApiClientTest : StormPilotUnitTest() {
    private val client = PhotonApiClient()

    @Test
    fun parsePhotonResponse_mapsValidFeaturesAndSkipsMalformedOnes() {
        val results = client.parsePhotonResponseForTest(
            """
            {
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "name": "National Weather Center",
                    "city": "Norman",
                    "state": "Oklahoma",
                    "country": "United States"
                  },
                  "geometry": { "type": "Point", "coordinates": [-97.438, 35.181] }
                },
                {
                  "type": "Feature",
                  "properties": { "city": "Missing Name" },
                  "geometry": { "type": "Point", "coordinates": [-1, 1] }
                },
                {
                  "type": "Feature",
                  "properties": { "name": "Bad Geometry" },
                  "geometry": { "type": "Point", "coordinates": [-1] }
                }
              ]
            }
            """.trimIndent(),
        )

        assertEquals(1, results.size)
        assertEquals(
            PhotonFeature(
                name = "National Weather Center",
                city = "Norman",
                state = "Oklahoma",
                country = "United States",
                geometry = Position(longitude = -97.438, latitude = 35.181),
            ),
            results.single(),
        )
    }

    @Test
    fun parsePhotonResponse_returnsEmptyWhenFeaturesAreMissing() {
        assertEquals(emptyList<PhotonFeature>(), client.parsePhotonResponseForTest("""{"type":"FeatureCollection"}"""))
    }

    @Test
    fun applyRouteMetrics_copiesDriveDistanceAndDurationByIndex() {
        val results = listOf(
            feature("One"),
            feature("Two"),
            feature("Three"),
        )

        val enriched = client.applyRouteMetricsForTest(
            results = results,
            payload = """
                {
                  "distances": [[1000.5, 2000.25]],
                  "durations": [[90.0, 180.5, "not-a-number"]]
                }
            """.trimIndent(),
        )

        assertEquals(1000.5, enriched[0].driveDistanceMeters!!, 0.0)
        assertEquals(90.0, enriched[0].driveDurationSeconds!!, 0.0)
        assertEquals(2000.25, enriched[1].driveDistanceMeters!!, 0.0)
        assertEquals(180.5, enriched[1].driveDurationSeconds!!, 0.0)
        assertNull(enriched[2].driveDistanceMeters)
        assertNull(enriched[2].driveDurationSeconds)
    }

    private fun feature(name: String): PhotonFeature {
        return PhotonFeature(
            name = name,
            city = null,
            state = null,
            country = null,
            geometry = Position(0.0, 0.0),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun PhotonApiClient.parsePhotonResponseForTest(payload: String): List<PhotonFeature> {
        val method = PhotonApiClient::class.java.getDeclaredMethod("parsePhotonResponse", String::class.java)
        method.isAccessible = true
        return method.invoke(this, payload) as List<PhotonFeature>
    }

    @Suppress("UNCHECKED_CAST")
    private fun PhotonApiClient.applyRouteMetricsForTest(
        results: List<PhotonFeature>,
        payload: String,
    ): List<PhotonFeature> {
        val method = PhotonApiClient::class.java.getDeclaredMethod(
            "applyRouteMetrics",
            List::class.java,
            String::class.java,
        )
        method.isAccessible = true
        return method.invoke(this, results, payload) as List<PhotonFeature>
    }
}
