package com.example.stormpilot.features.shared.data.weather

import com.example.stormpilot.features.shared.data.api.StormPilotApi
import javax.inject.Inject

/**
 * Fetches app-shaped weather snapshots from the StormPilot Spring Boot API.
 */
class WeatherRepository @Inject constructor(
    private val api: StormPilotApi,
) {
    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<WeatherSnapshot> =
        runCatching {
            api.weather(latitude, longitude)
        }
}
