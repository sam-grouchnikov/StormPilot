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
}

class ValhallaRoutingApiClient @Inject constructor() : RoutingApiClient {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return Result.failure(UnsupportedOperationException("Valhalla endpoint not configured"))
    }

    override suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>> {
        return fetchRoute(origin, destination).map { listOf(it) }
    }
}

class OsrmRoutingApiClient @Inject constructor() : RoutingApiClient {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return fetchRouteCandidates(origin, destination).map { it.first() }
    }

    override suspend fun fetchRouteCandidates(origin: Position, destination: Position): Result<List<RouteResult>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val coordinates = "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}"
                val url = URL(
                    "https://routing.openstreetmap.de/routed-car/route/v1/driving/$coordinates" +
                            "?overview=full&geometries=geojson&steps=true&alternatives=true"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20_000
                    readTimeout = 20_000
                    setRequestProperty("Accept", "application/json")
                }
                connection.inputStream.bufferedReader().use { reader ->
                    RoutingParsing.parseOsrmRoutes(reader.readText())
                }
            }
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
}
