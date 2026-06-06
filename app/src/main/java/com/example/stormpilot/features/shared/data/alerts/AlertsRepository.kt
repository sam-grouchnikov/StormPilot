package com.example.stormpilot.features.shared.data.alerts

import com.example.stormpilot.features.shared.data.api.StormPilotApi
import javax.inject.Inject

/**
 * Retrieves active alerts and warning polygons from the StormPilot Spring Boot API.
 */
class AlertsRepository @Inject constructor(
    private val api: StormPilotApi,
) {
    suspend fun fetchAlerts(latitude: Double, longitude: Double): Result<List<NwsAlert>> =
        runCatching {
            api.activeAlerts(latitude, longitude)
        }

    suspend fun fetchWarningPolygonsGeoJson(): String =
        api.warningPolygonsGeoJson()
}
