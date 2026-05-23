package com.example.stormpilot.data

import org.maplibre.spatialk.geojson.Position

/**
 * Abstraction for route retrieval from an origin to destination.
 */
interface RoutingRepository {
    /**
     * Fetches a route between [origin] and [destination].
     */
    suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult>
}
