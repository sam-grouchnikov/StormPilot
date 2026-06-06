package com.example.stormpilot.features.shared.data.assistant

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.stormpilot.features.shared.data.location.LocationRepository
import com.google.firebase.ai.type.AutoFunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.JsonSchema
import com.google.firebase.ai.type.Tool
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

class GenAIWeatherTools(
    private val context: Context,
    private val locationRepository: LocationRepository,
) {
    fun tool(): Tool = Tool.functionDeclarations(
        functionDeclarations = null,
        autoFunctionDeclarations = listOf(
            AutoFunctionDeclaration.create(
                functionName = "get_user_location",
                description = "Get the user's current device location as latitude and longitude when location permission is granted. Use this when the user says here, near me, my location, or asks for local weather without naming a place.",
                inputSchema = emptyInputSchema,
            ) { _: JsonObject ->
                safeFunctionResponse {
                    getUserLocation()
                }
            },
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
                functionName = "get_hourly_weather",
                description = "Get NWS hourly forecast timing for a specific United States location, including temperature, wind, precipitation chance, and short conditions.",
                inputSchema = hourlyInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getHourlyWeather(
                        locationQuery = args.requiredString("location"),
                        hours = args.optionalInt("hours") ?: 12
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
                functionName = "get_spc_convective_outlook",
                description = "Get SPC categorical convective outlook risk for days 1 through 5 at a specific United States location.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getSpcConvectiveOutlook(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_storm_environment",
                description = "Get storm-relevant forecast ingredients from Open-Meteo for a location: CAPE, lifted index, CIN, dew point, humidity, gusts, low-level shear, and estimated storm-relative helicity.",
                inputSchema = hourlyInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    getStormEnvironment(
                        locationQuery = args.requiredString("location"),
                        hours = args.optionalInt("hours") ?: 12
                    )
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

    private suspend fun getUserLocation(): JsonObject {
        val location = locationRepository.location.value ?: locationRepository.fetchCurrentLocation()
        return if (location == null) {
            buildJsonObject {
                put("error", "Current device location is unavailable.")
                put("hint", "Ask the user to grant location permission or provide a city, address, landmark, or latitude/longitude pair.")
            }
        } else {
            buildJsonObject {
                put("location", buildJsonObject {
                    put("name", "Current location")
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                })
                put("weatherLocationQuery", "%.5f, %.5f".format(Locale.US, location.latitude, location.longitude))
                put("bearingDegrees", location.bearing.toDouble())
                put("speedMetersPerSecond", location.speed.toDouble())
            }
        }
    }

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

    private suspend fun getHourlyWeather(locationQuery: String, hours: Int): JsonObject {
        val location = resolveLocation(locationQuery)
        val point = fetchNwsPoint(location)
        val periods = fetchJson(
            point.getJSONObject("properties").getString("forecastHourly")
        ).getJSONObject("properties").getJSONArray("periods")
        val maxPeriods = hours.coerceIn(1, 24)

        return buildJsonObject {
            putLocation(location)
            put("hoursRequested", maxPeriods)
            put("periods", periods.toJsonArray(maxPeriods) { period ->
                buildJsonObject {
                    put("startTime", period.optString("startTime"))
                    put("endTime", period.optString("endTime"))
                    put("temperature", period.optInt("temperature"))
                    put("temperatureUnit", period.optString("temperatureUnit"))
                    put("windSpeed", period.optString("windSpeed"))
                    put("windDirection", period.optString("windDirection"))
                    put("shortForecast", period.optString("shortForecast"))
                    put("probabilityOfPrecipitationPercent", period.optJSONObject("probabilityOfPrecipitation")?.optDoubleOrNull("value"))
                    put("relativeHumidityPercent", period.optJSONObject("relativeHumidity")?.optDoubleOrNull("value"))
                    put("dewpointC", period.optJSONObject("dewpoint")?.optDoubleOrNull("value"))
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

    private suspend fun getSpcConvectiveOutlook(locationQuery: String): JsonObject {
        val location = resolveLocation(locationQuery)
        val outlooks = coroutineScope {
            spcUrls.mapIndexed { index, url ->
                async {
                    val risk = runCatching {
                        riskForPoint(fetchJson(url), location.latitude, location.longitude)
                    }.getOrDefault("Not issued")

                    buildJsonObject {
                        put("day", index + 1)
                        put("risk", risk)
                    }
                }
            }.awaitAll()
        }

        return buildJsonObject {
            putLocation(location)
            put("outlooks", buildJsonArray {
                outlooks.forEach { add(it) }
            })
        }
    }

    private suspend fun getStormEnvironment(locationQuery: String, hours: Int): JsonObject {
        val location = resolveLocation(locationQuery)
        val json = fetchJson(openMeteoStormUrl(location))
        val hourly = json.getJSONObject("hourly")
        val currentTime = json.optJSONObject("current")?.optString("time").orEmpty()
        val times = hourly.getJSONArray("time")
        val startIndex = firstForecastIndex(times, currentTime)
        val maxPeriods = hours.coerceIn(1, 24)
        val endIndex = minOf(times.length(), startIndex + maxPeriods)
        val hourlyEnvironments = (startIndex until endIndex).map { index ->
            stormEnvironmentAt(hourly, index)
        }

        return buildJsonObject {
            putLocation(location)
            put("hoursRequested", maxPeriods)
            put("units", buildJsonObject {
                put("temperature", "F")
                put("dewPoint", "F")
                put("windSpeed", "mph")
                put("cape", "J/kg")
                put("liftedIndex", "C")
                put("cin", "J/kg")
                put("lowLevelShear", "mph")
                put("stormRelativeHelicity", "m2/s2")
            })
            put("peakSignals", buildPeakStormSignals(hourlyEnvironments))
            put("hours", buildJsonArray {
                hourlyEnvironments.forEach { environment ->
                    add(environment.toJson())
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

    private fun openMeteoStormUrl(location: ResolvedLocation): String {
        val current = listOf("temperature_2m").joinToString(",")
        val hourly = listOf(
            "temperature_2m",
            "precipitation_probability",
            "cape",
            "lifted_index",
            "convective_inhibition",
            "dew_point_2m",
            "relative_humidity_2m",
            "wind_speed_10m",
            "wind_direction_10m",
            "wind_speed_80m",
            "wind_direction_80m",
            "wind_gusts_10m",
            "wind_speed_1000hPa",
            "wind_direction_1000hPa",
            "wind_speed_925hPa",
            "wind_direction_925hPa",
            "wind_speed_850hPa",
            "wind_direction_850hPa",
            "wind_speed_700hPa",
            "wind_direction_700hPa",
        ).joinToString(",")

        return "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${location.latitude}" +
            "&longitude=${location.longitude}" +
            "&current=$current" +
            "&hourly=$hourly" +
            "&temperature_unit=fahrenheit" +
            "&wind_speed_unit=mph" +
            "&timezone=auto" +
            "&forecast_days=2"
    }

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

    private fun JSONArray.optDoubleOrNull(index: Int): Double? =
        opt(index)?.takeUnless { it == JSONObject.NULL }?.let {
            when (it) {
                is Number -> it.toDouble()
                is String -> it.toDoubleOrNull()
                else -> null
            }
        }

    private fun JSONObject.valueAt(name: String, index: Int): Double? =
        optJSONArray(name)?.optDoubleOrNull(index)

    private fun firstForecastIndex(times: JSONArray, currentTime: String): Int {
        if (currentTime.isBlank()) return 0
        for (i in 0 until times.length()) {
            if (times.optString(i) >= currentTime) return i
        }
        return 0
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

    private fun stormEnvironmentAt(hourly: JSONObject, index: Int): StormEnvironment {
        val srh = stormRelativeHelicity(hourly, index)
        val shear = lowLevelShear(hourly, index)
        val cape = hourly.valueAt("cape", index)
        val liftedIndex = hourly.valueAt("lifted_index", index)
        val cin = hourly.valueAt("convective_inhibition", index)
        val dewPoint = hourly.valueAt("dew_point_2m", index)
        val humidity = hourly.valueAt("relative_humidity_2m", index)
        val windGust = hourly.valueAt("wind_gusts_10m", index)

        return StormEnvironment(
            time = hourly.getJSONArray("time").optString(index),
            temperature = hourly.valueAt("temperature_2m", index),
            precipitationChance = hourly.valueAt("precipitation_probability", index),
            cape = cape,
            liftedIndex = liftedIndex,
            cin = cin,
            dewPoint = dewPoint,
            relativeHumidity = humidity,
            windSpeed = hourly.valueAt("wind_speed_10m", index),
            windDirection = hourly.valueAt("wind_direction_10m", index),
            windGust = windGust,
            lowLevelShear = shear,
            stormRelativeHelicity = srh,
            stormSupport = stormSupportLabel(cape, liftedIndex, cin, dewPoint, windGust, shear, srh)
        )
    }

    private fun buildPeakStormSignals(environments: List<StormEnvironment>): JsonObject = buildJsonObject {
        put("maxCape", environments.maxValueOf { it.cape })
        put("minLiftedIndex", environments.minValueOf { it.liftedIndex })
        put("maxDewPoint", environments.maxValueOf { it.dewPoint })
        put("maxWindGust", environments.maxValueOf { it.windGust })
        put("maxLowLevelShear", environments.maxValueOf { it.lowLevelShear })
        put("maxStormRelativeHelicity", environments.maxValueOf { it.stormRelativeHelicity })
        put("strongestStormSupport", environments.maxByOrNull { it.stormSupport.rank }?.stormSupport?.label)
    }

    private fun List<StormEnvironment>.maxValueOf(selector: (StormEnvironment) -> Double?): Double? =
        mapNotNull(selector).maxOrNull()?.roundOneDecimal()

    private fun List<StormEnvironment>.minValueOf(selector: (StormEnvironment) -> Double?): Double? =
        mapNotNull(selector).minOrNull()?.roundOneDecimal()

    private fun stormSupportLabel(
        cape: Double?,
        liftedIndex: Double?,
        cin: Double?,
        dewPoint: Double?,
        windGust: Double?,
        shear: Double?,
        srh: Double?,
    ): StormSupport {
        var score = 0
        if ((cape ?: 0.0) >= 1000.0) score += 2 else if ((cape ?: 0.0) >= 500.0) score += 1
        if ((liftedIndex ?: 99.0) <= -4.0) score += 2 else if ((liftedIndex ?: 99.0) <= -2.0) score += 1
        if ((dewPoint ?: 0.0) >= 65.0) score += 2 else if ((dewPoint ?: 0.0) >= 58.0) score += 1
        if ((shear ?: 0.0) >= 25.0 || (srh ?: 0.0) >= 150.0) score += 2
        if ((windGust ?: 0.0) >= 40.0) score += 1
        if ((cin ?: 0.0) <= -200.0) score -= 1

        return when {
            score >= 7 -> StormSupport("High", 3)
            score >= 4 -> StormSupport("Moderate", 2)
            score >= 2 -> StormSupport("Low", 1)
            else -> StormSupport("Minimal", 0)
        }
    }

    private fun lowLevelShear(hourly: JSONObject, index: Int): Double? {
        val speed10 = hourly.valueAt("wind_speed_10m", index) ?: return null
        val dir10 = hourly.valueAt("wind_direction_10m", index) ?: return null
        val speed80 = hourly.valueAt("wind_speed_80m", index) ?: return null
        val dir80 = hourly.valueAt("wind_direction_80m", index) ?: return null
        val low = windVector(speed10, dir10)
        val high = windVector(speed80, dir80)
        return vectorMagnitude(high.first - low.first, high.second - low.second)
    }

    private fun stormRelativeHelicity(hourly: JSONObject, index: Int): Double? {
        val levels = listOf("1000hPa", "925hPa", "850hPa", "700hPa").map { level ->
            val speed = hourly.valueAt("wind_speed_$level", index) ?: return null
            val direction = hourly.valueAt("wind_direction_$level", index) ?: return null
            windVector(speed * MPH_TO_MPS, direction)
        }
        val meanU = levels.map { it.first }.average()
        val meanV = levels.map { it.second }.average()
        val stormMotion = Pair(meanU * 0.75, meanV * 0.75)

        return levels.zipWithNext().sumOf { (lower, upper) ->
            ((upper.first - stormMotion.first) * (lower.second - stormMotion.second)) -
                ((lower.first - stormMotion.first) * (upper.second - stormMotion.second))
        }.let { abs(it) }
    }

    private fun riskForPoint(geoJson: JSONObject, latitude: Double, longitude: Double): String {
        val features = geoJson.optJSONArray("features") ?: return "Not issued"
        var bestDn = -1
        var bestLabel = "No SPC risk"

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val geometry = feature.optJSONObject("geometry") ?: continue
            if (!geometryContainsPoint(geometry, longitude, latitude)) continue

            val properties = feature.optJSONObject("properties") ?: continue
            val dn = properties.optInt("DN", 0)
            if (dn >= bestDn) {
                bestDn = dn
                bestLabel = properties.optString("LABEL2")
                    .ifBlank { properties.optString("LABEL") }
                    .ifBlank { "SPC risk" }
            }
        }

        return bestLabel
    }

    private fun geometryContainsPoint(geometry: JSONObject, longitude: Double, latitude: Double): Boolean {
        val coordinates = geometry.optJSONArray("coordinates") ?: return false
        return when (geometry.optString("type")) {
            "Polygon" -> polygonContainsPoint(coordinates, longitude, latitude)
            "MultiPolygon" -> {
                for (i in 0 until coordinates.length()) {
                    if (polygonContainsPoint(coordinates.getJSONArray(i), longitude, latitude)) return true
                }
                false
            }
            else -> false
        }
    }

    private fun polygonContainsPoint(polygon: JSONArray, longitude: Double, latitude: Double): Boolean {
        if (polygon.length() == 0) return false
        if (!ringContainsPoint(polygon.getJSONArray(0), longitude, latitude)) return false
        for (i in 1 until polygon.length()) {
            if (ringContainsPoint(polygon.getJSONArray(i), longitude, latitude)) return false
        }
        return true
    }

    private fun ringContainsPoint(ring: JSONArray, longitude: Double, latitude: Double): Boolean {
        var inside = false
        var j = ring.length() - 1
        for (i in 0 until ring.length()) {
            val current = ring.getJSONArray(i)
            val previous = ring.getJSONArray(j)
            val xi = current.getDouble(0)
            val yi = current.getDouble(1)
            val xj = previous.getDouble(0)
            val yj = previous.getDouble(1)
            val intersects = ((yi > latitude) != (yj > latitude)) &&
                (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi)
            if (intersects) inside = !inside
            j = i
        }
        return inside
    }

    private fun windVector(speed: Double, directionDegrees: Double): Pair<Double, Double> {
        val radians = Math.toRadians(directionDegrees)
        return Pair(-speed * sin(radians), -speed * cos(radians))
    }

    private fun vectorMagnitude(u: Double, v: Double): Double =
        sqrt(u * u + v * v)

    private fun Double.roundOneDecimal(): Double =
        (this * 10.0).roundToInt() / 10.0

    private fun Double?.rounded(): Double? =
        this?.roundOneDecimal()

    private fun StormEnvironment.toJson(): JsonObject = buildJsonObject {
        put("time", time)
        put("temperature", temperature.rounded())
        put("precipitationChancePercent", precipitationChance.rounded())
        put("cape", cape.rounded())
        put("liftedIndex", liftedIndex.rounded())
        put("cin", cin.rounded())
        put("dewPoint", dewPoint.rounded())
        put("relativeHumidityPercent", relativeHumidity.rounded())
        put("windSpeed", windSpeed.rounded())
        put("windDirectionDegrees", windDirection.rounded())
        put("windGust", windGust.rounded())
        put("lowLevelShear", lowLevelShear.rounded())
        put("stormRelativeHelicity", stormRelativeHelicity.rounded())
        put("stormSupport", stormSupport.label)
    }

    private data class ResolvedLocation(
        val name: String,
        val latitude: Double,
        val longitude: Double
    )

    private data class StormSupport(
        val label: String,
        val rank: Int,
    )

    private data class StormEnvironment(
        val time: String,
        val temperature: Double?,
        val precipitationChance: Double?,
        val cape: Double?,
        val liftedIndex: Double?,
        val cin: Double?,
        val dewPoint: Double?,
        val relativeHumidity: Double?,
        val windSpeed: Double?,
        val windDirection: Double?,
        val windGust: Double?,
        val lowLevelShear: Double?,
        val stormRelativeHelicity: Double?,
        val stormSupport: StormSupport,
    )

    private companion object {
        const val MPH_TO_MPS = 0.44704

        val emptyInputSchema = JsonSchema.obj(
            properties = emptyMap(),
            description = "No arguments"
        )

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

        val hourlyInputSchema = JsonSchema.obj(
            properties = mapOf(
                "location" to JsonSchema.string(
                    description = "A city, address, landmark, or latitude/longitude pair supplied by the user."
                ),
                "hours" to JsonSchema.integer(
                    description = "Number of hourly forecast periods to retrieve, from 1 to 24.",
                    minimum = 1.0,
                    maximum = 24.0
                )
            ),
            optionalProperties = listOf("hours"),
            description = "Hourly weather request"
        )

        val spcUrls = listOf(
            "https://www.spc.noaa.gov/products/outlook/day1otlk_cat.nolyr.geojson",
            "https://www.spc.noaa.gov/products/outlook/day2otlk_cat.nolyr.geojson",
            "https://www.spc.noaa.gov/products/outlook/day3otlk_cat.nolyr.geojson",
            "https://www.spc.noaa.gov/products/exper/day4-8/day4prob.lyr.geojson",
            "https://www.spc.noaa.gov/products/exper/day4-8/day5prob.lyr.geojson",
        )
    }
}
