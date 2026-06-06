package com.example.stormpilot.features.shared.data.alerts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Retrieves active National Weather Service alerts for a location and converts them into app alert models.
 */
class AlertsRepository @Inject constructor(){

    suspend fun fetchAlerts(latitude: Double, longitude: Double): Result<List<NwsAlert>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.weather.gov/alerts/active?point=$latitude,$longitude"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "(StormPilot, sam.grouchnikov@gmail.com)")
                connection.setRequestProperty("Accept", "application/geo+json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val json = JSONObject(response)
                val features = json.getJSONArray("features")

                val alerts = mutableListOf<NwsAlert>()
                for (i in 0 until features.length()) {
                    val props = features.getJSONObject(i).getJSONObject("properties")
                    val affectedZones = props.optJSONArray("affectedZones")
                    alerts.add(
                        NwsAlert(
                            id = props.optString("id"),
                            event = props.optString("event"),
                            headline = props.optString("headline"),
                            description = props.optString("description"),
                            instruction = props.optString("instruction").takeIf { it.isNotBlank() },
                            severity = props.optString("severity"),
                            urgency = props.optString("urgency"),
                            effective = props.optString("effective").takeIf { it.isNotBlank() },
                            onset = props.optString("onset").takeIf { it.isNotBlank() },
                            expires = props.optString("expires").takeIf { it.isNotBlank() },
                            areaDescription = props.optString("areaDesc").takeIf { it.isNotBlank() },
                            affectedZones = buildList {
                                if (affectedZones != null) {
                                    for (zoneIndex in 0 until affectedZones.length()) {
                                        affectedZones.optString(zoneIndex)
                                            .takeIf { it.isNotBlank() }
                                            ?.let(::add)
                                    }
                                }
                            },
                            senderName = props.optString("senderName"),
                        )
                    )
                }
                Result.success(alerts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchWarningPolygonsGeoJson(): String = withContext(Dispatchers.IO) {
        val where = ALERT_POLYGON_TYPES.joinToString(",") { "'$it'" }.let { "prod_type IN ($it)" }
        val encoded = URLEncoder.encode(where, "UTF-8")
        val url = URL(
            "https://mapservices.weather.noaa.gov/eventdriven/rest/services/WWA/watch_warn_adv/MapServer/1/query" +
                "?where=$encoded&outFields=prod_type&geometryType=esriGeometryPolygon" +
                "&spatialRel=esriSpatialRelIntersects&outSR=4326&f=geojson"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/geo+json")
        }

        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(responseCode in 200..299) { "Alerts request failed with HTTP $responseCode: $body" }
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        val ALERT_POLYGON_TYPES = listOf(
            "Tornado Warning",
            "Severe Thunderstorm Warning",
            "Flash Flood Warning",
        )
    }
}
