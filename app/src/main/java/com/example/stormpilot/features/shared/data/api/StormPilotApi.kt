package com.example.stormpilot.features.shared.data.api

import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import com.example.stormpilot.features.shared.data.routing.RouteResult
import com.example.stormpilot.features.shared.data.search.PhotonBoundingBox
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import com.example.stormpilot.features.shared.data.weather.WeatherSnapshot
import kotlinx.serialization.json.JsonObject

interface StormPilotApi {
    suspend fun health(): JsonObject

    suspend fun resolveLocation(query: String): ResolvedLocation

    suspend fun reverseLocation(latitude: Double, longitude: Double): ResolvedLocation

    suspend fun weather(latitude: Double, longitude: Double): WeatherSnapshot

    suspend fun activeAlerts(latitude: Double, longitude: Double): List<NwsAlert>

    suspend fun warningPolygonsGeoJson(): String

    suspend fun search(
        query: String,
        latitude: Double? = null,
        longitude: Double? = null,
        bbox: PhotonBoundingBox? = null,
        includeDrivingMetrics: Boolean = true,
    ): List<PhotonFeature>

    suspend fun route(
        originLatitude: Double,
        originLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        avoidStorms: Boolean = false,
        includeWarnings: Boolean = true,
    ): RouteSelectionResponse

    suspend fun routeCandidates(
        originLatitude: Double,
        originLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
    ): List<RouteResult>

    suspend fun routeVia(request: RouteViaRequest): RouteSelectionResponse

    suspend fun analyzeRouteWarnings(request: RouteWarningAnalyzeRequest): RouteWarningAnalysis

    suspend fun submitAssistantRequest(message: String, location: String): String

    suspend fun assistantResolveLocation(location: String): JsonObject

    suspend fun assistantCurrentWeather(location: String): JsonObject

    suspend fun assistantForecast(location: String, days: Int = 3): JsonObject

    suspend fun assistantHourly(location: String, hours: Int = 12): JsonObject

    suspend fun assistantAlerts(location: String): JsonObject

    suspend fun assistantSpcOutlook(location: String): JsonObject

    suspend fun assistantStormEnvironment(location: String, hours: Int = 12): JsonObject

    suspend fun assistantOutlook(location: String): JsonObject
}
