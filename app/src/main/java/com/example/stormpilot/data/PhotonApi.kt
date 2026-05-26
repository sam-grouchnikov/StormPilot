package com.example.stormpilot.data

import kotlinx.coroutines.Dispatchers
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
    val geometry: Position
)

class PhotonApiClient @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun search(query: String, locationBias: Position? = null): List<PhotonFeature> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                var urlString = "https://photon.komoot.io/api/?q=$encodedQuery&limit=10"
                if (locationBias != null) {
                    urlString += "&lat=${locationBias.latitude}&lon=${locationBias.longitude}"
                }
                val url = URL(urlString)
                
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    return@withContext emptyList()
                }

                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                parsePhotonResponse(responseBody)
            } catch (e: Exception) {
                emptyList()
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
}
