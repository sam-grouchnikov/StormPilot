package com.example.stormpilot.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject

class AlertsRepository @Inject constructor(){

    suspend fun fetchAlerts(latitude: Double, longitude: Double): Result<List<NwsAlert>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.weather.gov/alerts/active?point=$latitude,$longitude"
                val response = URL(url).readText()
                val json = JSONObject(response)
                val features = json.getJSONArray("features")

                val alerts = mutableListOf<NwsAlert>()
                for (i in 0 until features.length()) {
                    val props = features.getJSONObject(i).getJSONObject("properties")
                    alerts.add(
                        NwsAlert(
                            id = props.optString("id"),
                            event = props.optString("event"),
                            headline = props.optString("headline"),
                            description = props.optString("description"),
                            instruction = props.optString("instruction").takeIf { it.isNotBlank() },
                            severity = props.optString("severity"),
                            urgency = props.optString("urgency"),
                            onset = props.optString("onset").takeIf { it.isNotBlank() },
                            expires = props.optString("expires").takeIf { it.isNotBlank() },
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