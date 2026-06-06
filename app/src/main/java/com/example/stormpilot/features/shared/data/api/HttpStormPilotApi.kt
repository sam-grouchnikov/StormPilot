package com.example.stormpilot.features.shared.data.api

import com.example.stormpilot.BuildConfig
import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import com.example.stormpilot.features.shared.data.routing.Maneuver
import com.example.stormpilot.features.shared.data.routing.RouteResult
import com.example.stormpilot.features.shared.data.routing.RouteStep
import com.example.stormpilot.features.shared.data.search.PhotonBoundingBox
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import com.example.stormpilot.features.shared.data.weather.CurrentWeather
import com.example.stormpilot.features.shared.data.weather.DailyWeatherOutlook
import com.example.stormpilot.features.shared.data.weather.HourlyForecast
import com.example.stormpilot.features.shared.data.weather.StormSpec
import com.example.stormpilot.features.shared.data.weather.WeatherSnapshot
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.spatialk.geojson.Position

@Singleton
class HttpStormPilotApi @Inject constructor() : StormPilotApi {
    private val baseUrl = BuildConfig.STORMPILOT_API_BASE_URL.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun health(): JsonObject =
        getJsonObject("health")

    override suspend fun resolveLocation(query: String): ResolvedLocation =
        parseResolvedLocation(getJSONObject("locations/resolve", params(param("query", query))))

    override suspend fun reverseLocation(latitude: Double, longitude: Double): ResolvedLocation =
        parseResolvedLocation(
            getJSONObject(
                "locations/reverse",
                params(
                    param("latitude", latitude),
                    param("longitude", longitude),
                ),
            ),
        )

    override suspend fun weather(latitude: Double, longitude: Double): WeatherSnapshot =
        parseWeatherSnapshot(
            getJSONObject(
                "weather",
                params(
                    param("latitude", latitude),
                    param("longitude", longitude),
                ),
            ),
        )

    override suspend fun activeAlerts(latitude: Double, longitude: Double): List<NwsAlert> =
        parseAlerts(
            getJSONArray(
                "alerts/active",
                params(
                    param("latitude", latitude),
                    param("longitude", longitude),
                ),
            ),
        )

    override suspend fun warningPolygonsGeoJson(): String =
        getBody("alerts/warning-polygons")

    override suspend fun search(
        query: String,
        latitude: Double?,
        longitude: Double?,
        bbox: PhotonBoundingBox?,
        includeDrivingMetrics: Boolean,
    ): List<PhotonFeature> =
        parseSearchResults(
            getJSONArray(
                "search",
                params(
                    param("query", query),
                    param("latitude", latitude),
                    param("longitude", longitude),
                    param("minLon", bbox?.minLon),
                    param("minLat", bbox?.minLat),
                    param("maxLon", bbox?.maxLon),
                    param("maxLat", bbox?.maxLat),
                    param("includeDrivingMetrics", includeDrivingMetrics),
                ),
            ),
        )

    override suspend fun route(
        originLatitude: Double,
        originLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        avoidStorms: Boolean,
        includeWarnings: Boolean,
    ): RouteSelectionResponse =
        parseRouteSelection(
            getJSONObject(
                "routes",
                params(
                    param("originLatitude", originLatitude),
                    param("originLongitude", originLongitude),
                    param("destinationLatitude", destinationLatitude),
                    param("destinationLongitude", destinationLongitude),
                    param("avoidStorms", avoidStorms),
                    param("includeWarnings", includeWarnings),
                ),
            ),
        )

    override suspend fun routeCandidates(
        originLatitude: Double,
        originLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
    ): List<RouteResult> =
        parseRouteResults(
            getJSONArray(
                "routes/candidates",
                params(
                    param("originLatitude", originLatitude),
                    param("originLongitude", originLongitude),
                    param("destinationLatitude", destinationLatitude),
                    param("destinationLongitude", destinationLongitude),
                ),
            ),
        )

    override suspend fun routeVia(request: RouteViaRequest): RouteSelectionResponse =
        parseRouteSelection(postJSONObject("routes/via", request.toJson()))

    override suspend fun analyzeRouteWarnings(request: RouteWarningAnalyzeRequest): RouteWarningAnalysis =
        parseRouteWarningAnalysis(postJSONObject("routes/warnings/analyze", request.toJson()))

    override suspend fun assistantResolveLocation(location: String): JsonObject =
        getJsonObject("assistant/weather/resolve-location", params(param("location", location)))

    override suspend fun assistantCurrentWeather(location: String): JsonObject =
        getJsonObject("assistant/weather/current", params(param("location", location)))

    override suspend fun assistantForecast(location: String, days: Int): JsonObject =
        getJsonObject(
            "assistant/weather/forecast",
            params(
                param("location", location),
                param("days", days),
            ),
        )

    override suspend fun assistantHourly(location: String, hours: Int): JsonObject =
        getJsonObject(
            "assistant/weather/hourly",
            params(
                param("location", location),
                param("hours", hours),
            ),
        )

    override suspend fun assistantAlerts(location: String): JsonObject =
        getJsonObject("assistant/weather/alerts", params(param("location", location)))

    override suspend fun assistantSpcOutlook(location: String): JsonObject =
        getJsonObject("assistant/weather/spc-outlook", params(param("location", location)))

    override suspend fun assistantStormEnvironment(location: String, hours: Int): JsonObject =
        getJsonObject(
            "assistant/weather/storm-environment",
            params(
                param("location", location),
                param("hours", hours),
            ),
        )

    override suspend fun assistantOutlook(location: String): JsonObject =
        getJsonObject("assistant/weather/outlook", params(param("location", location)))

    private suspend fun getJSONObject(path: String, params: List<Pair<String, Any?>> = emptyList()): JSONObject =
        JSONObject(getBody(path, params))

    private suspend fun getJSONArray(path: String, params: List<Pair<String, Any?>> = emptyList()): JSONArray =
        JSONArray(getBody(path, params))

    private suspend fun getJsonObject(path: String, params: List<Pair<String, Any?>> = emptyList()): JsonObject =
        json.parseToJsonElement(getBody(path, params)).jsonObject

    private suspend fun getBody(path: String, params: List<Pair<String, Any?>> = emptyList()): String =
        withContext(Dispatchers.IO) {
            val connection = (buildUrl(path, params).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json, application/geo+json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            connection.readResponseBody()
        }

    private suspend fun postJSONObject(path: String, payload: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = (buildUrl(path).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(payload.toString())
            }
            JSONObject(connection.readResponseBody())
        }

    private fun HttpURLConnection.readResponseBody(): String {
        try {
            val responseCode = responseCode
            val stream = if (responseCode in 200..299) inputStream else errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                throw IOException(parseApiErrorMessage(responseCode, body))
            }
            return body
        } finally {
            disconnect()
        }
    }

    private fun parseApiErrorMessage(responseCode: Int, body: String): String {
        if (body.isBlank()) return "StormPilot API request failed with HTTP $responseCode."

        return runCatching {
            val error = JSONObject(body)
            error.optNullableString("message")
                ?: error.optNullableString("error")
                ?: "StormPilot API request failed with HTTP $responseCode."
        }.getOrDefault("StormPilot API request failed with HTTP $responseCode: $body")
    }

    private fun buildUrl(path: String, params: List<Pair<String, Any?>> = emptyList()): URL {
        val query = params.mapNotNull { (name, value) ->
            value ?: return@mapNotNull null
            "${name.urlEncode()}=${value.toString().urlEncode()}"
        }.joinToString("&")
        val normalizedPath = path.trimStart('/')
        val suffix = if (query.isBlank()) "" else "?$query"
        return URL("$baseUrl/$normalizedPath$suffix")
    }

    private fun parseResolvedLocation(json: JSONObject): ResolvedLocation =
        ResolvedLocation(
            name = json.optString("name"),
            latitude = json.optDouble("latitude"),
            longitude = json.optDouble("longitude"),
        )

    private fun parseWeatherSnapshot(json: JSONObject): WeatherSnapshot =
        WeatherSnapshot(
            current = parseCurrentWeather(json.getJSONObject("current")),
            hourly = json.optJSONArray("hourly").orEmpty().mapObjects(::parseHourlyForecast),
            daily = json.optJSONArray("daily").orEmpty().mapObjects(::parseDailyWeatherOutlook),
            stormSpecs = json.optJSONArray("stormSpecs").orEmpty().mapObjects(::parseStormSpec),
        )

    private fun parseCurrentWeather(json: JSONObject): CurrentWeather =
        CurrentWeather(
            temperature = json.optNullableInt("temperature"),
            conditions = json.optString("conditions").ifBlank { "Unknown" },
            dewPoint = json.optNullableInt("dewPoint"),
            humidity = json.optNullableInt("humidity"),
            windSpeed = json.optNullableInt("windSpeed"),
            windGust = json.optNullableInt("windGust"),
        )

    private fun parseHourlyForecast(json: JSONObject): HourlyForecast =
        HourlyForecast(
            time = json.optString("time"),
            temperature = json.optNullableInt("temperature"),
            conditions = json.optString("conditions").ifBlank { "Unknown" },
            precipitationChance = json.optNullableInt("precipitationChance"),
        )

    private fun parseDailyWeatherOutlook(json: JSONObject): DailyWeatherOutlook =
        DailyWeatherOutlook(
            day = json.optString("day"),
            conditions = json.optString("conditions").ifBlank { "Unknown" },
            high = json.optNullableInt("high"),
            low = json.optNullableInt("low"),
            spcOutlook = json.optString("spcOutlook").ifBlank { "Not issued" },
        )

    private fun parseStormSpec(json: JSONObject): StormSpec =
        StormSpec(
            label = json.optString("label"),
            value = json.optString("value"),
            detail = json.optString("detail"),
        )

    private fun parseAlerts(json: JSONArray): List<NwsAlert> =
        json.mapObjects { alert ->
            NwsAlert(
                id = alert.optString("id"),
                event = alert.optString("event"),
                headline = alert.optString("headline"),
                description = alert.optString("description"),
                instruction = alert.optNullableString("instruction")?.takeIf { it.isNotBlank() },
                severity = alert.optString("severity"),
                urgency = alert.optString("urgency"),
                effective = alert.optNullableString("effective")?.takeIf { it.isNotBlank() },
                onset = alert.optNullableString("onset")?.takeIf { it.isNotBlank() },
                expires = alert.optNullableString("expires")?.takeIf { it.isNotBlank() },
                areaDescription = alert.optNullableString("areaDescription")?.takeIf { it.isNotBlank() },
                affectedZones = alert.optJSONArray("affectedZones")
                    .orEmpty()
                    .mapStrings(),
                senderName = alert.optString("senderName"),
            )
        }

    private fun parseSearchResults(json: JSONArray): List<PhotonFeature> =
        json.mapObjects { feature ->
            PhotonFeature(
                name = feature.optString("name"),
                city = feature.optNullableString("city")?.takeIf { it.isNotBlank() },
                state = feature.optNullableString("state")?.takeIf { it.isNotBlank() },
                country = feature.optNullableString("country")?.takeIf { it.isNotBlank() },
                geometry = parsePosition(feature.getJSONObject("geometry")),
                straightLineDistanceMeters = feature.optNullableDouble("straightLineDistanceMeters"),
                driveDistanceMeters = feature.optNullableDouble("driveDistanceMeters"),
                driveDurationSeconds = feature.optNullableDouble("driveDurationSeconds"),
            )
        }

    private fun parseRouteSelection(json: JSONObject): RouteSelectionResponse =
        RouteSelectionResponse(
            route = parseRouteResult(json.getJSONObject("route")),
            routeGeoJson = json.optString("routeGeoJson"),
            warningCount = json.optNullableInt("warningCount"),
            warningError = json.optNullableString("warningError")?.takeIf { it.isNotBlank() },
            stormRouteAlertMessage = json.optNullableString("stormRouteAlertMessage")?.takeIf { it.isNotBlank() },
        )

    private fun parseRouteResults(json: JSONArray): List<RouteResult> =
        json.mapObjects(::parseRouteResult)

    private fun parseRouteResult(json: JSONObject): RouteResult =
        RouteResult(
            polyline = json.optJSONArray("polyline").orEmpty().mapObjects(::parsePosition),
            distanceMeters = json.optDouble("distanceMeters"),
            durationSeconds = json.optDouble("durationSeconds"),
            steps = json.optJSONArray("steps").orEmpty().mapObjects(::parseRouteStep),
        )

    private fun parseRouteStep(json: JSONObject): RouteStep =
        RouteStep(
            instruction = json.optString("instruction"),
            distanceMeters = json.optDouble("distanceMeters"),
            durationSeconds = json.optDouble("durationSeconds"),
            maneuverLocation = json.optJSONObject("maneuverLocation")?.let(::parsePosition),
            maneuver = json.optJSONObject("maneuver")?.let(::parseManeuver),
        )

    private fun parseManeuver(json: JSONObject): Maneuver =
        Maneuver(
            type = json.optString("type").ifBlank { "unknown" },
            modifier = json.optNullableString("modifier")?.takeIf { it.isNotBlank() },
            exit = json.optNullableInt("exit"),
            bearingBefore = json.optInt("bearingBefore"),
            bearingAfter = json.optInt("bearingAfter"),
            location = parsePosition(json.getJSONObject("location")),
        )

    private fun parseRouteWarningAnalysis(json: JSONObject): RouteWarningAnalysis =
        RouteWarningAnalysis(
            warningCount = json.optInt("warningCount"),
            destinationInsideWarning = json.optBoolean("destinationInsideWarning"),
        )

    private fun parsePosition(json: JSONObject): Position =
        Position(
            longitude = json.optDouble("longitude"),
            latitude = json.optDouble("latitude"),
        )

    private fun RouteViaRequest.toJson(): JSONObject =
        JSONObject()
            .put("origin", origin.toJson())
            .put("destination", destination.toJson())
            .put("waypoints", JSONArray().apply {
                waypoints.forEach { put(it.toJson()) }
            })
            .put("includeWarnings", includeWarnings)

    private fun RouteWarningAnalyzeRequest.toJson(): JSONObject =
        JSONObject()
            .put("routePolyline", JSONArray().apply {
                routePolyline.forEach { put(it.toJson()) }
            })
            .put("destination", destination?.toJson() ?: JSONObject.NULL)
            .put("alertsGeoJson", alertsGeoJson ?: JSONObject.NULL)

    private fun ApiPosition.toJson(): JSONObject =
        JSONObject()
            .put("latitude", latitude)
            .put("longitude", longitude)

    private fun JSONArray?.orEmpty(): JSONArray =
        this ?: JSONArray()

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        buildList {
            for (index in 0 until length()) {
                add(transform(getJSONObject(index)))
            }
        }

    private fun JSONArray.mapStrings(): List<String> =
        buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }

    private fun JSONObject.optNullableString(name: String): String? =
        opt(name)?.takeUnless { it == JSONObject.NULL }?.toString()

    private fun JSONObject.optNullableInt(name: String): Int? =
        opt(name)?.takeUnless { it == JSONObject.NULL }?.let { value ->
            when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        }

    private fun JSONObject.optNullableDouble(name: String): Double? =
        opt(name)?.takeUnless { it == JSONObject.NULL }?.let { value ->
            when (value) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }
        }

    private fun param(name: String, value: Any?): Pair<String, Any?> =
        name to value

    private fun params(vararg params: Pair<String, Any?>): List<Pair<String, Any?>> =
        params.toList()

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, "UTF-8")

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 20_000
        const val USER_AGENT = "StormPilot/1.0"
    }
}
