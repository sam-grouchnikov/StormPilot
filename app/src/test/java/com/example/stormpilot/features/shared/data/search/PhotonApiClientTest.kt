package com.example.stormpilot.features.shared.data.search

import com.example.stormpilot.features.shared.data.api.StormPilotApi
import com.example.stormpilot.testing.StormPilotUnitTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class PhotonApiClientTest : StormPilotUnitTest() {
    private val api = mockk<StormPilotApi>()
    private val client = PhotonApiClient(api)

    @Test
    fun search_forwardsLocationBiasBboxAndDrivingMetricsFlag() = runTest {
        val origin = Position(longitude = -97.0, latitude = 35.0)
        val bbox = PhotonBoundingBox(
            minLon = -98.0,
            minLat = 34.0,
            maxLon = -96.0,
            maxLat = 36.0,
        )
        val expected = listOf(feature("Moore"))
        coEvery {
            api.search(
                query = "Moore",
                latitude = 35.0,
                longitude = -97.0,
                bbox = bbox,
                includeDrivingMetrics = true,
            )
        } returns expected

        val results = client.search("Moore", origin, bbox)

        assertSame(expected, results)
        coVerify(exactly = 1) {
            api.search(
                query = "Moore",
                latitude = 35.0,
                longitude = -97.0,
                bbox = bbox,
                includeDrivingMetrics = true,
            )
        }
    }

    @Test
    fun search_withoutLocationBiasDisablesDrivingMetrics() = runTest {
        val expected = listOf(feature("Norman"))
        coEvery {
            api.search(
                query = "Norman",
                latitude = null,
                longitude = null,
                bbox = null,
                includeDrivingMetrics = false,
            )
        } returns expected

        val results = client.search("Norman")

        assertSame(expected, results)
    }

    @Test
    fun enrichWithDrivingMetrics_isNoOpBecauseBackendSearchAlreadyIncludesMetrics() = runTest {
        val results = listOf(feature("One"))

        val enriched = client.enrichWithDrivingMetrics(
            origin = Position(longitude = -97.0, latitude = 35.0),
            results = results,
        )

        assertEquals(results, enriched)
    }

    private fun feature(name: String): PhotonFeature =
        PhotonFeature(
            name = name,
            city = null,
            state = null,
            country = null,
            geometry = Position(0.0, 0.0),
        )
}
