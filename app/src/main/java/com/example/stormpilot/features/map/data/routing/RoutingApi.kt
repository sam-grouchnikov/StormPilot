package com.example.stormpilot.features.map.data.routing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.geojson.Position
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

interface RoutingApiClient {
    suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult>
    suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>>
    suspend fun fetchRouteVia(
        origin: Position,
        destination: Position,
        waypoints: List<Position>,
    ): Result<RouteResult>
}

class ValhallaRoutingApiClient @Inject constructor() : RoutingApiClient {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return Result.failure(UnsupportedOperationException("Valhalla endpoint not configured"))
    }

    override suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>> {
        return fetchRoute(origin, destination).map { listOf(it) }
    }

    override suspend fun fetchRouteVia(
        origin: Position,
        destination: Position,
        waypoints: List<Position>,
    ): Result<RouteResult> {
        return Result.failure(UnsupportedOperationException("Valhalla endpoint not configured"))
    }
}

class OsrmRoutingApiClient @Inject constructor() : RoutingApiClient {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return fetchRouteCandidates(origin, destination).map { it.first() }
    }

    override suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                fetchOsrmRoutes(listOf(origin, destination), alternatives = true)
            }
        }
    }

    override suspend fun fetchRouteVia(
        origin: Position,
        destination: Position,
        waypoints: List<Position>,
    ): Result<RouteResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                fetchOsrmRoutes(listOf(origin) + waypoints + listOf(destination), alternatives = false).first()
            }
        }
    }

    private fun fetchOsrmRoutes(coordinates: List<Position>, alternatives: Boolean): List<RouteResult> {
        val encodedCoordinates = coordinates.joinToString(separator = ";") { position ->
            "${position.longitude},${position.latitude}"
        }
        val url = URL(
            "https://routing.openstreetmap.de/routed-car/route/v1/driving/$encodedCoordinates" +
                    "?overview=full&geometries=geojson&steps=true&alternatives=$alternatives"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            return connection.inputStream.bufferedReader().use { reader ->
                RoutingParsing.parseOsrmRoutes(reader.readText())
            }
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Coordinates route lookup through the preferred provider and falls back to OSRM when needed.
 */
class RoutingRepositoryImpl @Inject constructor(
    private val valhallaClient: ValhallaRoutingApiClient,
    private val osrmClient: OsrmRoutingApiClient,
) : RoutingRepository {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return valhallaClient.fetchRoute(origin, destination)
            .recoverCatching {
                osrmClient.fetchRoute(origin, destination).getOrThrow()
            }
    }

    override suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>> {
        return valhallaClient.fetchRouteCandidates(origin, destination)
            .recoverCatching {
                osrmClient.fetchRouteCandidates(origin, destination).getOrThrow()
            }
    }

    override suspend fun fetchRouteVia(
        origin: Position,
        destination: Position,
        waypoints: List<Position>,
    ): Result<RouteResult> {
        return valhallaClient.fetchRouteVia(origin, destination, waypoints)
            .recoverCatching {
                osrmClient.fetchRouteVia(origin, destination, waypoints).getOrThrow()
            }
    }
}
