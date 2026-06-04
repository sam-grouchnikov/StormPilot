package com.example.stormpilot.features.map.data.routing

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
}
