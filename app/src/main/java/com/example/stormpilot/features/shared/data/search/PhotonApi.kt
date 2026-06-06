package com.example.stormpilot.features.shared.data.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Position
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

data class PhotonFeature(
    val name: String,
    val city: String?,
    val state: String?,
    val country: String?,
    val geometry: Position,
    val straightLineDistanceMeters: Double? = null,
    val driveDistanceMeters: Double? = null,
    val driveDurationSeconds: Double? = null,
)

data class PhotonBoundingBox(
    val minLon: Double,
    val minLat: Double,
    val maxLon: Double,
    val maxLat: Double
)

class PhotonApiClient @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun search(
        query: String,
        locationBias: Position? = null,
        bbox: PhotonBoundingBox? = null
    ): List<PhotonFeature> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                var urlString = "https://photon.komoot.io/api/?q=$encodedQuery&limit=10"
                if (locationBias != null) {
                    urlString += "&lat=${locationBias.latitude}&lon=${locationBias.longitude}"
                }
                if (bbox != null) {
                    urlString += "&bbox=${bbox.minLon},${bbox.minLat},${bbox.maxLon},${bbox.maxLat}"
                }
                val url = URL(urlString)
                
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3_500
                    readTimeout = 3_500
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "StormPilot/1.0")
                }

                try {
                    val responseCode = connection.responseCode
                    if (responseCode !in 200..299) {
                        return@withContext emptyList()
                    }

                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    parsePhotonResponse(responseBody)
                } finally {
                    connection.disconnect()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun enrichWithDrivingMetrics(
        origin: Position,
        results: List<PhotonFeature>,
    ): List<PhotonFeature> {
        if (results.isEmpty()) return results

        return withContext(Dispatchers.IO) {
            try {
                val coordinates = buildString {
                    append("${origin.longitude},${origin.latitude}")
                    results.forEach { result ->
                        append(";${result.geometry.longitude},${result.geometry.latitude}")
                    }
                }
                val destinations = (1..results.size).joinToString(";")
                val url = URL(
                    "https://routing.openstreetmap.de/routed-car/table/v1/driving/$coordinates" +
                            "?sources=0&destinations=$destinations&annotations=distance,duration"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3_500
                    readTimeout = 3_500
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "StormPilot/1.0")
                }

                try {
                    val responseCode = connection.responseCode
                    if (responseCode !in 200..299) {
                        return@withContext results
                    }

                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    applyRouteMetrics(results, responseBody)
                } finally {
                    connection.disconnect()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                results
            }
        }
    }

    private fun parsePhotonResponse(payload: String): List<PhotonFeature> {
        val root = json.parseToJsonElement(payload).jsonObject
        val features = root["features"]?.jsonArray.orEmpty()
        
        return features.mapNotNull { featureElement ->
            try {
                val feature = featureElement.jsonObject
                val properties = feature["properties"]?.jsonObject ?: return@mapNotNull null
                val geometry = feature["geometry"]?.jsonObject ?: return@mapNotNull null
                
                val coords = geometry["coordinates"]?.jsonArray ?: return@mapNotNull null
                if (coords.size < 2) return@mapNotNull null
                
                val position = Position(
                    longitude = coords[0].jsonPrimitive.double,
                    latitude = coords[1].jsonPrimitive.double
                )

                val name = properties["name"]?.jsonPrimitive?.contentOrNull
                
                // If there's no name, we might skip or fallback to city, but standard is to require name.
                if (name == null) return@mapNotNull null

                PhotonFeature(
                    name = name,
                    city = properties["city"]?.jsonPrimitive?.contentOrNull,
                    state = properties["state"]?.jsonPrimitive?.contentOrNull,
                    country = properties["country"]?.jsonPrimitive?.contentOrNull,
                    geometry = position
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun applyRouteMetrics(results: List<PhotonFeature>, payload: String): List<PhotonFeature> {
        val root = json.parseToJsonElement(payload).jsonObject
        val distances = root["distances"]?.jsonArray?.firstOrNull()?.jsonArray
        val durations = root["durations"]?.jsonArray?.firstOrNull()?.jsonArray

        return results.mapIndexed { index, result ->
            result.copy(
                driveDistanceMeters = distances
                    ?.getOrNull(index)
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.toDoubleOrNull(),
                driveDurationSeconds = durations
                    ?.getOrNull(index)
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.toDoubleOrNull(),
            )
        }
    }
}
