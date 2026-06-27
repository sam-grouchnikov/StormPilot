package com.example.stormpilot.features.shared.data.weather

import android.content.Context
import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class DashboardWeatherPlaceholder(
    val cityName: String,
    val snapshot: WeatherSnapshot,
    val alerts: List<NwsAlert> = emptyList(),
)

class DashboardWeatherPlaceholderRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun load(assetName: String): Result<DashboardWeatherPlaceholder> =
        runCatching {
            val normalizedAssetName = assetName.trim()
            require(
                normalizedAssetName.endsWith(".json", ignoreCase = true) ||
                    normalizedAssetName.endsWith(".txt", ignoreCase = true),
            ) {
                "Dashboard placeholder weather files must be .json or .txt."
            }

            val content = withContext(Dispatchers.IO) {
                context.assets.open(normalizedAssetName).bufferedReader().use { it.readText() }
            }

            if (normalizedAssetName.endsWith(".txt", ignoreCase = true)) {
                parseText(content)
            } else {
                parseJson(JSONObject(content))
            }
        }

    private fun parseJson(root: JSONObject): DashboardWeatherPlaceholder {
        val weatherRoot = root.optJSONObject("weather") ?: root
        val cityName = root.optNullableString("cityName")
            ?: weatherRoot.optNullableString("cityName")
            ?: "Offline Sample"

        return DashboardWeatherPlaceholder(
            cityName = cityName,
            alerts = (root.optJSONArray("alerts") ?: weatherRoot.optJSONArray("alerts"))
                .orEmpty()
                .mapObjects(::parseAlert),
            snapshot = WeatherSnapshot(
                current = parseCurrentWeather(weatherRoot.getJSONObject("current")),
                hourly = weatherRoot.optJSONArray("hourly").orEmpty().mapObjects(::parseHourlyForecast),
                daily = weatherRoot.optJSONArray("daily").orEmpty().mapObjects(::parseDailyWeatherOutlook),
                stormSpecs = weatherRoot.optJSONArray("stormSpecs").orEmpty().mapObjects(::parseStormSpec),
            ),
        )
    }

    private fun parseText(content: String): DashboardWeatherPlaceholder {
        val sections = linkedMapOf<String, MutableList<String>>()
        var currentSection = "meta"

        content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .forEach { line ->
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.removePrefix("[").removeSuffix("]").trim().lowercase()
                } else {
                    sections.getOrPut(currentSection) { mutableListOf() }.add(line)
                }
            }

        val meta = parseKeyValues(sections["meta"].orEmpty())
        val current = parseKeyValues(sections["current"].orEmpty())

        return DashboardWeatherPlaceholder(
            cityName = meta["cityName"] ?: meta["location"] ?: "Offline Sample",
            alerts = parseTable(sections["alerts"].orEmpty()).map(::parseAlert),
            snapshot = WeatherSnapshot(
                current = CurrentWeather(
                    temperature = current["temperature"].toNullableInt(),
                    conditions = current["conditions"].orUnknown(),
                    dewPoint = current["dewPoint"].toNullableInt(),
                    humidity = current["humidity"].toNullableInt(),
                    windSpeed = current["windSpeed"].toNullableInt(),
                    windGust = current["windGust"].toNullableInt(),
                ),
                hourly = parseTable(sections["hourly"].orEmpty()).map { row ->
                    HourlyForecast(
                        time = row["time"].orEmpty(),
                        temperature = row["temperature"].toNullableInt(),
                        conditions = row["conditions"].orUnknown(),
                        precipitationChance = row["precipitationChance"].toNullableInt(),
                    )
                },
                daily = parseTable(sections["daily"].orEmpty()).map { row ->
                    DailyWeatherOutlook(
                        day = row["day"].orEmpty(),
                        conditions = row["conditions"].orUnknown(),
                        high = row["high"].toNullableInt(),
                        low = row["low"].toNullableInt(),
                        spcOutlook = row["spcOutlook"] ?: "Not issued",
                    )
                },
                stormSpecs = parseTable(sections["stormSpecs"].orEmpty()).map { row ->
                    StormSpec(
                        label = row["label"].orEmpty(),
                        value = row["value"].orEmpty(),
                        detail = row["detail"].orEmpty(),
                    )
                },
            ),
        )
    }

    private fun parseCurrentWeather(json: JSONObject): CurrentWeather =
        CurrentWeather(
            temperature = json.optNullableInt("temperature"),
            conditions = json.optNullableString("conditions").orUnknown(),
            dewPoint = json.optNullableInt("dewPoint"),
            humidity = json.optNullableInt("humidity"),
            windSpeed = json.optNullableInt("windSpeed"),
            windGust = json.optNullableInt("windGust"),
        )

    private fun parseHourlyForecast(json: JSONObject): HourlyForecast =
        HourlyForecast(
            time = json.optString("time"),
            temperature = json.optNullableInt("temperature"),
            conditions = json.optNullableString("conditions").orUnknown(),
            precipitationChance = json.optNullableInt("precipitationChance"),
        )

    private fun parseDailyWeatherOutlook(json: JSONObject): DailyWeatherOutlook =
        DailyWeatherOutlook(
            day = json.optString("day"),
            conditions = json.optNullableString("conditions").orUnknown(),
            high = json.optNullableInt("high"),
            low = json.optNullableInt("low"),
            spcOutlook = json.optNullableString("spcOutlook") ?: "Not issued",
        )

    private fun parseStormSpec(json: JSONObject): StormSpec =
        StormSpec(
            label = json.optString("label"),
            value = json.optString("value"),
            detail = json.optString("detail"),
        )

    private fun parseAlert(json: JSONObject): NwsAlert =
        NwsAlert(
            id = json.optString("id").ifBlank { json.optString("event") },
            event = json.optString("event"),
            headline = json.optNullableString("headline") ?: json.optString("event"),
            description = json.optNullableString("description").orEmpty(),
            instruction = json.optNullableString("instruction"),
            severity = json.optNullableString("severity") ?: "Unknown",
            urgency = json.optNullableString("urgency") ?: "Unknown",
            effective = json.optNullableString("effective"),
            onset = json.optNullableString("onset"),
            expires = json.optNullableString("expires"),
            areaDescription = json.optNullableString("areaDescription"),
            affectedZones = json.optJSONArray("affectedZones").orEmpty().mapStrings(),
            senderName = json.optNullableString("senderName") ?: "StormPilot sample data",
        )

    private fun parseAlert(row: Map<String, String>): NwsAlert =
        NwsAlert(
            id = row["id"].orEmpty().ifBlank { row["event"].orEmpty() },
            event = row["event"].orEmpty(),
            headline = row["headline"] ?: row["event"].orEmpty(),
            description = row["description"].orEmpty(),
            instruction = row["instruction"].takeUnless { it.isNullOrBlank() },
            severity = row["severity"] ?: "Unknown",
            urgency = row["urgency"] ?: "Unknown",
            effective = row["effective"].takeUnless { it.isNullOrBlank() },
            onset = row["onset"].takeUnless { it.isNullOrBlank() },
            expires = row["expires"].takeUnless { it.isNullOrBlank() },
            areaDescription = row["areaDescription"].takeUnless { it.isNullOrBlank() },
            affectedZones = row["affectedZones"]
                ?.split(';')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                .orEmpty(),
            senderName = row["senderName"] ?: "StormPilot sample data",
        )

    private fun parseKeyValues(lines: List<String>): Map<String, String> =
        lines.mapNotNull { line ->
            val separatorIndex = line.indexOf('=')
            if (separatorIndex <= 0) {
                null
            } else {
                line.substring(0, separatorIndex).trim() to
                    line.substring(separatorIndex + 1).trim()
            }
        }.toMap()

    private fun parseTable(lines: List<String>): List<Map<String, String>> {
        val header = lines.firstOrNull()
            ?.split('|')
            ?.map { it.trim() }
            ?.takeIf { it.isNotEmpty() }
            ?: return emptyList()

        return lines.drop(1).map { line ->
            val values = line.split('|').map { it.trim() }
            header.mapIndexed { index, column -> column to values.getOrElse(index) { "" } }.toMap()
        }
    }

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
        opt(name)?.takeUnless { it == JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }

    private fun JSONObject.optNullableInt(name: String): Int? =
        opt(name)?.takeUnless { it == JSONObject.NULL }?.let { value ->
            when (value) {
                is Number -> value.toInt()
                is String -> value.toNullableInt()
                else -> null
            }
        }

    private fun String?.toNullableInt(): Int? =
        this
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) && it != "--" }
            ?.toIntOrNull()

    private fun String?.orUnknown(): String =
        this?.takeIf { it.isNotBlank() } ?: "Unknown"
}
