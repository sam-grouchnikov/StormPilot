package com.example.stormpilot.features.shared.data.search

import com.example.stormpilot.features.shared.data.api.StormPilotApi
import org.maplibre.spatialk.geojson.Position
import javax.inject.Inject

data class PhotonFeature(
    val name: String,
    val city: String?,
    val state: String?,
    val country: String?,
    val geometry: Position,
    val address: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val postcode: String? = null,
    val category: String? = null,
    val type: String? = null,
    val straightLineDistanceMeters: Double? = null,
    val driveDistanceMeters: Double? = null,
    val driveDurationSeconds: Double? = null,
)

data class PhotonBoundingBox(
    val minLon: Double,
    val minLat: Double,
    val maxLon: Double,
    val maxLat: Double,
)

class PhotonApiClient @Inject constructor(
    private val api: StormPilotApi,
) {
    suspend fun search(
        query: String,
        locationBias: Position? = null,
        bbox: PhotonBoundingBox? = null,
    ): List<PhotonFeature> =
        api.search(
            query = query,
            latitude = locationBias?.latitude,
            longitude = locationBias?.longitude,
            bbox = bbox,
            includeDrivingMetrics = locationBias != null,
        )

    suspend fun enrichWithDrivingMetrics(
        origin: Position,
        results: List<PhotonFeature>,
    ): List<PhotonFeature> = results
}
