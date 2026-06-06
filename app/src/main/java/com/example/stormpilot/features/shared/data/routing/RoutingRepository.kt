package com.example.stormpilot.features.shared.data.routing

import com.example.stormpilot.features.shared.data.api.RouteSelectionResponse
import com.example.stormpilot.features.shared.data.api.RouteWarningAnalysis
import org.maplibre.spatialk.geojson.Position

/**
 * Abstraction for route retrieval from an origin to destination.
 */
interface RoutingRepository {
    /**
     * Fetches a route between [origin] and [destination].
     */
    suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult>

    /**
     * Fetches the backend route selection response, including warning analysis when requested.
     */
    suspend fun fetchRouteSelection(
        origin: Position,
        destination: Position,
        avoidStorms: Boolean = false,
        includeWarnings: Boolean = true,
    ): Result<RouteSelectionResponse>

    /**
     * Fetches route candidates between [origin] and [destination].
     */
    suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>>

    /**
     * Fetches a route between [origin] and [destination] through explicit intermediate waypoints.
     */
    suspend fun fetchRouteVia(
        origin: Position,
        destination: Position,
        waypoints: List<Position>,
    ): Result<RouteResult>

    /**
     * Counts current warning polygons intersecting the route.
     */
    suspend fun analyzeRouteWarnings(
        routePolyline: List<Position>,
        destination: Position?,
        alertsGeoJson: String? = null,
    ): Result<RouteWarningAnalysis>
}
