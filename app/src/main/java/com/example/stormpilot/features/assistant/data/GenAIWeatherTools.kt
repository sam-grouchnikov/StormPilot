package com.example.stormpilot.features.assistant.data

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.firebase.ai.type.AutoFunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.JsonSchema
import com.google.firebase.ai.type.Tool
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.json.JSONArray
import org.json.JSONObject

class GenAIWeatherTools(private val context: Context) {
    fun tool(): Tool = Tool.functionDeclarations(
        functionDeclarations = null,
        autoFunctionDeclarations = listOf(
            AutoFunctionDeclaration.create(
                functionName = "resolve_location",
                description = "Resolve a user-provided place name into latitude and longitude. Use this before weather tools when the user asks about a specific location.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    resolveLocationResponse(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_current_weather",
                description = "Get latest observed weather conditions for a specific United States location using weather.gov observation stations.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getCurrentWeather(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_weather_forecast",
                description = "Get an NWS daily forecast for a specific United States location. Include location in the answer.",
                inputSchema = forecastInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getWeatherForecast(
                        locationQuery = args.requiredString("location"),
                        days = args.optionalInt("days") ?: 3
                    )
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_active_weather_alerts",
                description = "Get active NWS watches, warnings, and advisories for a specific United States location.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getActiveWeatherAlerts(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_weather_outlook",
                description = "Get a practical storm-chasing weather outlook for a specific United States location, combining NWS forecast periods and active alerts.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getWeatherOutlook(args.requiredString("location"))
                }
            }
        )
    )

    private suspend fun resolveLocationResponse(locationQuery: String): JsonObject {
        val location = resolveLocation(locationQuery)
        return buildJsonObject {
            put("query", locationQuery)
            put("name", location.name)
            put("latitude", location.latitude)
            put("longitude", location.longitude)
        }
    }

    private suspend fun getCurrentWeather(locationQuery: String): JsonObject {
        val location = resolveLocation(locationQuery)
        val point = fetchNwsPoint(location)
        val stationsUrl = point.getJSONObject("properties").getString("observationStations")
        val stations = fetchJson(stationsUrl).getJSONArray("features")
        if (stations.length() == 0) {
            return errorResponse(location, "No nearby NWS observation stations were found.")
        }

        val stationId = stations.getJSONObject(0).getString("id").substringAfterLast("/")
        val observation = fetchJson("https://api.weather.gov/stations/$stationId/observations/latest")
        val props = observation.getJSONObject("properties")

        return buildJsonObject {
            putLocation(location)
            put("stationId", stationId)
            put("timestamp", props.optString("timestamp"))
            put("description", props.optString("textDescription"))
            put("temperatureC", props.optJSONObject("temperature")?.optDoubleOrNull("value"))
            put("dewpointC", props.optJSONObject("dewpoint")?.optDoubleOrNull("value"))
            put("relativeHumidityPercent", props.optJSONObject("relativeHumidity")?.optDoubleOrNull("value"))
            put("windDirectionDegrees", props.optJSONObject("windDirection")?.optDoubleOrNull("value"))
            put("windSpeedKph", props.optJSONObject("windSpeed")?.optDoubleOrNull("value"))
            put("barometricPressurePa", props.optJSONObject("barometricPressure")?.optDoubleOrNull("value"))
        }
    }

    private suspend fun getWeatherForecast(locationQuery: String, days: Int): JsonObject {
        val location = resolveLocation(locationQuery)
        val forecastUrl = fetchNwsPoint(location).getJSONObject("properties").getString("forecast")
        val periods = fetchJson(forecastUrl).getJSONObject("properties").getJSONArray("periods")
        val maxPeriods = days.coerceIn(1, 7) * 2

        return buildJsonObject {
            putLocation(location)
            put("daysRequested", days.coerceIn(1, 7))
            put("periods", periods.toJsonArray(maxPeriods) { period ->
                buildJsonObject {
                    put("name", period.optString("name"))
                    put("startTime", period.optString("startTime"))
                    put("endTime", period.optString("endTime"))
                    put("isDaytime", period.optBoolean("isDaytime"))
                    put("temperature", period.optInt("temperature"))
                    put("temperatureUnit", period.optString("temperatureUnit"))
                    put("windSpeed", period.optString("windSpeed"))
                    put("windDirection", period.optString("windDirection"))
                    put("shortForecast", period.optString("shortForecast"))
                    put("detailedForecast", period.optString("detailedForecast"))
                }
            })
        }
    }

    private suspend fun getActiveWeatherAlerts(locationQuery: String): JsonObject {
        val location = resolveLocation(locationQuery)
        val alerts = fetchJson(
            "https://api.weather.gov/alerts/active?point=${location.latitude},${location.longitude}"
        ).getJSONArray("features")

        return buildJsonObject {
            putLocation(location)
            put("alertCount", alerts.length())
            put("alerts", alerts.toJsonArray(limit = 8) { feature ->
                val props = feature.getJSONObject("properties")
                buildJsonObject {
                    put("event", props.optString("event"))
                    put("headline", props.optString("headline"))
                    put("severity", props.optString("severity"))
                    put("urgency", props.optString("urgency"))
                    put("certainty", props.optString("certainty"))
                    put("areaDescription", props.optString("areaDesc"))
                    put("onset", props.optString("onset"))
                    put("expires", props.optString("expires"))
                    put("description", props.optString("description"))
                    put("instruction", props.optString("instruction"))
                }
            })
        }
    }

    private suspend fun getWeatherOutlook(locationQuery: String): JsonObject {
        val location = resolveLocation(locationQuery)
        val point = fetchNwsPoint(location)
        val props = point.getJSONObject("properties")
        val (forecastPeriods, hourlyPeriods, alerts) = coroutineScope {
            val forecast = async {
                fetchJson(props.getString("forecast"))
                    .getJSONObject("properties")
                    .getJSONArray("periods")
            }
            val hourly = async {
                fetchJson(props.getString("forecastHourly"))
                    .getJSONObject("properties")
                    .getJSONArray("periods")
            }
            val activeAlerts = async {
                fetchJson(
                    "https://api.weather.gov/alerts/active?point=${location.latitude},${location.longitude}"
                ).getJSONArray("features")
            }
            Triple(forecast.await(), hourly.await(), activeAlerts.await())
        }

        return buildJsonObject {
            putLocation(location)
            put("activeAlertCount", alerts.length())
            put("activeAlerts", alerts.toJsonArray(limit = 5) { feature ->
                val alert = feature.getJSONObject("properties")
                buildJsonObject {
                    put("event", alert.optString("event"))
                    put("headline", alert.optString("headline"))
                    put("severity", alert.optString("severity"))
                    put("expires", alert.optString("expires"))
                }
            })
            put("dailyPeriods", forecastPeriods.toJsonArray(limit = 6) { period ->
                buildJsonObject {
                    put("name", period.optString("name"))
                    put("temperature", period.optInt("temperature"))
                    put("temperatureUnit", period.optString("temperatureUnit"))
                    put("windSpeed", period.optString("windSpeed"))
                    put("windDirection", period.optString("windDirection"))
                    put("shortForecast", period.optString("shortForecast"))
                    put("detailedForecast", period.optString("detailedForecast"))
                }
            })
            put("nextTwelveHours", hourlyPeriods.toJsonArray(limit = 12) { period ->
                buildJsonObject {
                    put("startTime", period.optString("startTime"))
                    put("temperature", period.optInt("temperature"))
                    put("temperatureUnit", period.optString("temperatureUnit"))
                    put("windSpeed", period.optString("windSpeed"))
                    put("windDirection", period.optString("windDirection"))
                    put("shortForecast", period.optString("shortForecast"))
                    put("probabilityOfPrecipitationPercent", period.optJSONObject("probabilityOfPrecipitation")?.optDoubleOrNull("value"))
                }
            })
        }
    }

    private suspend fun resolveLocation(locationQuery: String): ResolvedLocation = withContext(Dispatchers.IO) {
        parseCoordinatePair(locationQuery)?.let { return@withContext it }

        val geocoder = Geocoder(context, Locale.US)
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(locationQuery, 1)
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(locationQuery, 1)
        }
        val address = addresses?.firstOrNull()
            ?: throw IllegalArgumentException("Could not resolve location: $locationQuery")
        ResolvedLocation(
            name = listOfNotNull(address.locality, address.adminArea, address.countryName)
                .distinct()
                .joinToString(", ")
                .ifBlank { locationQuery },
            latitude = address.latitude,
            longitude = address.longitude
        )
    }

    private fun parseCoordinatePair(locationQuery: String): ResolvedLocation? {
        val parts = locationQuery
            .split(",", " ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (parts.size < 2) return null

        val latitude = parts[0].toDoubleOrNull() ?: return null
        val longitude = parts[1].toDoubleOrNull() ?: return null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null

        return ResolvedLocation(
            name = "%.4f, %.4f".format(Locale.US, latitude, longitude),
            latitude = latitude,
            longitude = longitude
        )
    }

    private suspend fun fetchNwsPoint(location: ResolvedLocation): JSONObject =
        fetchJson("https://api.weather.gov/points/${location.latitude},${location.longitude}")

    private suspend fun fetchJson(url: String): JSONObject = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", "(StormPilot, sam.grouchnikov@gmail.com)")
            setRequestProperty("Accept", "application/geo+json, application/ld+json, application/json")
        }

        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(responseCode in 200..299) { "Weather request failed with HTTP $responseCode: $body" }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun safeFunctionResponse(block: suspend () -> JsonObject): FunctionResponsePart =
        try {
            FunctionResponsePart.from(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FunctionResponsePart.from(
                buildJsonObject {
                    put("error", e.localizedMessage ?: "Weather data could not be loaded.")
                    put("hint", "Ask the user to confirm the location if it may be outside weather.gov coverage or was misspelled.")
                }
            )
        }

    private fun errorResponse(location: ResolvedLocation, message: String): JsonObject = buildJsonObject {
        putLocation(location)
        put("error", message)
    }

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.contentOrNull()?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Missing required argument: $name")

    private fun JsonObject.optionalInt(name: String): Int? =
        this[name]?.jsonPrimitive?.intOrNull

    private fun JsonPrimitive.contentOrNull(): String? =
        takeUnless { it.toString() == "null" }?.content

    private fun JSONObject.optDoubleOrNull(name: String): Double? =
        opt(name)?.takeUnless { it == JSONObject.NULL }?.let {
            when (it) {
                is Number -> it.toDouble()
                is String -> it.toDoubleOrNull()
                else -> null
            }
        }

    private fun JSONArray.toJsonArray(
        limit: Int,
        transform: (JSONObject) -> JsonElement
    ) = buildJsonArray {
        for (i in 0 until minOf(length(), limit)) {
            add(transform(getJSONObject(i)))
        }
    }

    private fun JsonObjectBuilder.putLocation(location: ResolvedLocation) {
        put("location", buildJsonObject {
            put("name", location.name)
            put("latitude", location.latitude)
            put("longitude", location.longitude)
        })
    }

    private data class ResolvedLocation(
        val name: String,
        val latitude: Double,
        val longitude: Double
    )

    private companion object {
        val locationInputSchema = JsonSchema.obj(
            properties = mapOf(
                "location" to JsonSchema.string(
                    description = "A city, address, landmark, or latitude/longitude pair supplied by the user."
                )
            ),
            description = "Location request"
        )

        val forecastInputSchema = JsonSchema.obj(
            properties = mapOf(
                "location" to JsonSchema.string(
                    description = "A city, address, landmark, or latitude/longitude pair supplied by the user."
                ),
                "days" to JsonSchema.integer(
                    description = "Number of forecast days to retrieve, from 1 to 7.",
                    minimum = 1.0,
                    maximum = 7.0
                )
            ),
            optionalProperties = listOf("days"),
            description = "Forecast request"
        )
    }
}
