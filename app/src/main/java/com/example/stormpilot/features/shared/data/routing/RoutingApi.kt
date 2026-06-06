package com.example.stormpilot.features.shared.data.routing

import com.example.stormpilot.features.shared.data.api.RouteSelectionResponse
import com.example.stormpilot.features.shared.data.api.RouteViaRequest
import com.example.stormpilot.features.shared.data.api.RouteWarningAnalysis
import com.example.stormpilot.features.shared.data.api.RouteWarningAnalyzeRequest
import com.example.stormpilot.features.shared.data.api.StormPilotApi
import com.example.stormpilot.features.shared.data.api.toApiPosition
import javax.inject.Inject
import org.maplibre.spatialk.geojson.Position

/**
 * Coordinates route lookup through the StormPilot Spring Boot API.
 */
class RoutingRepositoryImpl @Inject constructor(
    private val api: StormPilotApi,
) : RoutingRepository {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> =
        fetchRouteSelection(
            origin = origin,
            destination = destination,
            avoidStorms = false,
            includeWarnings = true,
        ).map { selection -> selection.route }

    override suspend fun fetchRouteSelection(
        origin: Position,
        destination: Position,
        avoidStorms: Boolean,
        includeWarnings: Boolean,
    ): Result<RouteSelectionResponse> =
        runCatching {
            api.route(
                originLatitude = origin.latitude,
                originLongitude = origin.longitude,
                destinationLatitude = destination.latitude,
                destinationLongitude = destination.longitude,
                avoidStorms = avoidStorms,
                includeWarnings = includeWarnings,
            )
        }

    override suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>> =
        runCatching {
            api.routeCandidates(
                originLatitude = origin.latitude,
                originLongitude = origin.longitude,
                destinationLatitude = destination.latitude,
                destinationLongitude = destination.longitude,
            )
        }

    override suspend fun fetchRouteVia(
        origin: Position,
        destination: Position,
        waypoints: List<Position>,
    ): Result<RouteResult> =
        runCatching {
            api.routeVia(
                RouteViaRequest(
                    origin = origin.toApiPosition(),
                    destination = destination.toApiPosition(),
                    waypoints = waypoints.map { it.toApiPosition() },
                    includeWarnings = true,
                ),
            ).route
        }

    override suspend fun analyzeRouteWarnings(
        routePolyline: List<Position>,
        destination: Position?,
        alertsGeoJson: String?,
    ): Result<RouteWarningAnalysis> =
        runCatching {
            api.analyzeRouteWarnings(
                RouteWarningAnalyzeRequest(
                    routePolyline = routePolyline.map { it.toApiPosition() },
                    destination = destination?.toApiPosition(),
                    alertsGeoJson = alertsGeoJson,
                ),
            )
        }
}
