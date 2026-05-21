package com.example.stormpilot.data

import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class WeatherSnapshot(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyWeatherOutlook>,
    val stormSpecs: List<StormSpec>,
)

data class CurrentWeather(
    val temperature: Int,
    val conditions: String,
    val dewPoint: Int?,
    val humidity: Int?,
    val windSpeed: Int?,
    val windGust: Int?,
)

data class HourlyForecast(
    val time: String,
    val temperature: Int,
    val conditions: String,
    val precipitationChance: Int?,
)

data class DailyWeatherOutlook(
    val day: String,
    val conditions: String,
    val high: Int,
    val low: Int,
    val spcOutlook: String,
)

data class StormSpec(
    val label: String,
    val value: String,
    val detail: String,
)

class WeatherRepository @Inject constructor() {
    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<WeatherSnapshot> =
        withContext(Dispatchers.IO) {
            runCatching {
                coroutineScope {
                    val forecast = async { fetchJson(openMeteoUrl(latitude, longitude)) }
                    val spcOutlooks = async { fetchSpcOutlooks(latitude, longitude) }
                    parseWeather(forecast.await(), spcOutlooks.await())
                }
            }
        }

    private fun openMeteoUrl(latitude: Double, longitude: Double): String {
        val current = listOf(
            "temperature_2m",
            "weather_code",
            "relative_humidity_2m",
            "dew_point_2m",
            "wind_speed_10m",
            "wind_gusts_10m",
        ).joinToString(",")
        val hourly = listOf(
            "temperature_2m",
            "weather_code",
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
        val daily = listOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
        ).joinToString(",")

        return "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$latitude" +
            "&longitude=$longitude" +
            "&current=$current" +
            "&hourly=$hourly" +
            "&daily=$daily" +
            "&temperature_unit=fahrenheit" +
            "&wind_speed_unit=mph" +
            "&timezone=auto" +
            "&forecast_days=5"
    }

    private suspend fun fetchSpcOutlooks(latitude: Double, longitude: Double): List<String> =
        coroutineScope {
            spcUrls.map { url ->
                async {
                    runCatching {
                        val outlook = fetchJson(url)
                        riskForPoint(outlook, latitude, longitude)
                    }.getOrDefault("Not issued")
                }
            }.awaitAll()
        }

    private fun parseWeather(json: JSONObject, spcOutlooks: List<String>): WeatherSnapshot {
        val currentJson = json.getJSONObject("current")
        val hourlyJson = json.getJSONObject("hourly")
        val dailyJson = json.getJSONObject("daily")
        val currentTime = currentJson.optString("time")
        val hourlyTimes = hourlyJson.getJSONArray("time")
        val currentIndex = firstForecastIndex(hourlyTimes, currentTime)

        return WeatherSnapshot(
            current = CurrentWeather(
                temperature = currentJson.optDouble("temperature_2m").roundInt(),
                conditions = weatherCodeLabel(currentJson.optInt("weather_code")),
                dewPoint = currentJson.optNullableDouble("dew_point_2m")?.roundInt(),
                humidity = currentJson.optNullableDouble("relative_humidity_2m")?.roundInt(),
                windSpeed = currentJson.optNullableDouble("wind_speed_10m")?.roundInt(),
                windGust = currentJson.optNullableDouble("wind_gusts_10m")?.roundInt(),
            ),
            hourly = parseHourly(hourlyJson, currentIndex),
            daily = parseDaily(dailyJson, spcOutlooks),
            stormSpecs = parseStormSpecs(hourlyJson, currentIndex),
        )
    }

    private fun parseHourly(hourly: JSONObject, startIndex: Int): List<HourlyForecast> {
        val times = hourly.getJSONArray("time")
        val temps = hourly.getJSONArray("temperature_2m")
        val codes = hourly.getJSONArray("weather_code")
        val precipitation = hourly.optJSONArray("precipitation_probability")

        return (startIndex until minOf(times.length(), startIndex + 18)).map { index ->
            HourlyForecast(
                time = times.getString(index).substringAfter("T"),
                temperature = temps.optDouble(index).roundInt(),
                conditions = weatherCodeLabel(codes.optInt(index)),
                precipitationChance = precipitation?.optNullableDouble(index)?.roundInt(),
            )
        }
    }

    private fun parseDaily(daily: JSONObject, spcOutlooks: List<String>): List<DailyWeatherOutlook> {
        val days = daily.getJSONArray("time")
        val codes = daily.getJSONArray("weather_code")
        val highs = daily.getJSONArray("temperature_2m_max")
        val lows = daily.getJSONArray("temperature_2m_min")

        return (0 until minOf(days.length(), 5)).map { index ->
            DailyWeatherOutlook(
                day = formatDay(days.getString(index), index),
                conditions = weatherCodeLabel(codes.optInt(index)),
                high = highs.optDouble(index).roundInt(),
                low = lows.optDouble(index).roundInt(),
                spcOutlook = spcOutlooks.getOrNull(index) ?: "Not issued",
            )
        }
    }

    private fun parseStormSpecs(hourly: JSONObject, index: Int): List<StormSpec> {
        val cape = hourly.valueAt("cape", index)
        val liftedIndex = hourly.valueAt("lifted_index", index)
        val cin = hourly.valueAt("convective_inhibition", index)
        val dewPoint = hourly.valueAt("dew_point_2m", index)
        val humidity = hourly.valueAt("relative_humidity_2m", index)
        val windGust = hourly.valueAt("wind_gusts_10m", index)
        val shear = lowLevelShear(hourly, index)
        val srh = stormRelativeHelicity(hourly, index)

        return listOf(
            StormSpec("SRH", srh?.let { "${it.roundInt()} m²/s²" } ?: "--", "0-3 km estimate"),
            StormSpec("CAPE", cape?.let { "${it.roundInt()} J/kg" } ?: "--", "Instability"),
            StormSpec("Lifted", liftedIndex?.let { "${it.roundInt()}°" } ?: "--", "Negative favors storms"),
            StormSpec("CIN", cin?.let { "${it.roundInt()} J/kg" } ?: "--", "Convective cap"),
            StormSpec("Dew Pt", dewPoint?.let { "${it.roundInt()}°" } ?: "--", "Low-level moisture"),
            StormSpec("RH", humidity?.let { "${it.roundInt()}%" } ?: "--", "Surface humidity"),
            StormSpec("LL Shear", shear?.let { "${it.roundInt()} mph" } ?: "--", "10-80 m vector"),
            StormSpec("Gust", windGust?.let { "${it.roundInt()} mph" } ?: "--", "Surface gust"),
        )
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
        }.let { kotlin.math.abs(it) }
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

    private fun fetchJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", "(StormPilot, sam.grouchnikov@gmail.com)")
            setRequestProperty("Accept", "application/geo+json, application/json")
        }

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(responseCode in 200..299) { "Weather request failed with HTTP $responseCode: $body" }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun firstForecastIndex(times: JSONArray, currentTime: String): Int {
        if (currentTime.isBlank()) return 0
        for (i in 0 until times.length()) {
            if (times.optString(i) >= currentTime) return i
        }
        return 0
    }

    private fun JSONObject.valueAt(name: String, index: Int): Double? =
        optJSONArray(name)?.optNullableDouble(index)

    private fun JSONObject.optNullableDouble(name: String): Double? =
        opt(name)?.takeUnless { it == JSONObject.NULL }?.let {
            when (it) {
                is Number -> it.toDouble()
                is String -> it.toDoubleOrNull()
                else -> null
            }
        }

    private fun JSONArray.optNullableDouble(index: Int): Double? =
        opt(index)?.takeUnless { it == JSONObject.NULL }?.let {
            when (it) {
                is Number -> it.toDouble()
                is String -> it.toDoubleOrNull()
                else -> null
            }
        }

    private fun Double.roundInt(): Int = roundToInt()

    private fun windVector(speed: Double, directionDegrees: Double): Pair<Double, Double> {
        val radians = Math.toRadians(directionDegrees)
        return Pair(-speed * kotlin.math.sin(radians), -speed * kotlin.math.cos(radians))
    }

    private fun vectorMagnitude(u: Double, v: Double): Double =
        kotlin.math.sqrt(u * u + v * v)

    private fun formatDay(value: String, index: Int): String =
        when (index) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> value.substring(5).replace("-", "/")
        }

    private fun weatherCodeLabel(code: Int): String = when (code) {
        0 -> "Clear"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorms"
        96, 99 -> "Severe storms"
        else -> "Weather code $code"
    }

    private companion object {
        const val MPH_TO_MPS = 0.44704

        val spcUrls = listOf(
            "https://www.spc.noaa.gov/products/outlook/day1otlk_cat.nolyr.geojson",
            "https://www.spc.noaa.gov/products/outlook/day2otlk_cat.nolyr.geojson",
            "https://www.spc.noaa.gov/products/outlook/day3otlk_cat.nolyr.geojson",
            "https://www.spc.noaa.gov/products/exper/day4-8/day4prob.lyr.geojson",
            "https://www.spc.noaa.gov/products/exper/day4-8/day5prob.lyr.geojson",
        )
    }
}
