package com.example.stormpilot.features.shared.data.api

import com.example.stormpilot.features.shared.data.routing.RouteResult
import org.maplibre.spatialk.geojson.Position

data class ApiPosition(
    val latitude: Double,
    val longitude: Double,
)

fun ApiPosition.toPosition(): Position =
    Position(longitude = longitude, latitude = latitude)

fun Position.toApiPosition(): ApiPosition =
    ApiPosition(latitude = latitude, longitude = longitude)

data class ResolvedLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class ApiError(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
)

data class RouteSelectionResponse(
    val route: RouteResult,
    val routeGeoJson: String,
    val warningCount: Int?,
    val warningError: String?,
    val stormRouteAlertMessage: String?,
)

data class RouteViaRequest(
    val origin: ApiPosition,
    val destination: ApiPosition,
    val waypoints: List<ApiPosition> = emptyList(),
    val includeWarnings: Boolean = true,
)

data class RouteWarningAnalyzeRequest(
    val routePolyline: List<ApiPosition>,
    val destination: ApiPosition?,
    val alertsGeoJson: String? = null,
)

data class RouteWarningAnalysis(
    val warningCount: Int,
    val destinationInsideWarning: Boolean,
)
