package com.example.stormpilot.features.alerts.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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
}
