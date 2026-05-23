package com.example.stormpilot.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.geojson.Position
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

interface RoutingApiClient {
    suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult>
}

class ValhallaRoutingApiClient @Inject constructor() : RoutingApiClient {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return Result.failure(UnsupportedOperationException("Valhalla endpoint not configured"))
    }
}

class OsrmRoutingApiClient @Inject constructor() : RoutingApiClient {
    override suspend fun fetchRoute(origin: Position, destination: Position): Result<RouteResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val coordinates = "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}"
                val url = URL(
                    "https://routing.openstreetmap.de/routed-car/route/v1/driving/$coordinates" +
                            "?overview=full&geometries=geojson&steps=true"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 20_000
                    readTimeout = 20_000
                    setRequestProperty("Accept", "application/json")
                }
                connection.inputStream.bufferedReader().use { reader ->
                    RoutingParsing.parseOsrmRoute(reader.readText())
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
}
