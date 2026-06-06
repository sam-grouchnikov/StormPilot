package com.example.stormpilot.features.shared.data.location

import com.example.stormpilot.features.shared.data.api.ResolvedLocation
import com.example.stormpilot.features.shared.data.api.StormPilotApi
import javax.inject.Inject

class LocationLookupRepository @Inject constructor(
    private val api: StormPilotApi,
) {
    suspend fun resolveLocation(query: String): Result<ResolvedLocation> =
        runCatching { api.resolveLocation(query) }

    suspend fun reverseLocation(latitude: Double, longitude: Double): Result<ResolvedLocation> =
        runCatching { api.reverseLocation(latitude, longitude) }
}
