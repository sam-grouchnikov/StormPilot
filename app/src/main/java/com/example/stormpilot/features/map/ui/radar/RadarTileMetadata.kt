package com.example.stormpilot.features.map.ui.radar

import android.util.Log
import com.example.stormpilot.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

internal enum class RadarProduct(
    val pathSegment: String,
    val displayName: String,
) {
    REFLECTIVITY("reflectivity", "Reflectivity"),
    VELOCITY("velocity", "Velocity"),
}

internal data class RadarTileMetadataKey(
    val site: String,
    val product: RadarProduct,
    val changesAgo: Int? = null,
)

internal data class RadarTileMetadata(
    val site: String,
    val product: RadarProduct,
    val changesAgo: Int?,
    val scanTimeUtc: String,
    val tileSize: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val rasterNativeMaxZoom: Int,
    val vectorTileLayer: String,
    val vectorTileUrl: String,
    val rasterTileUrl: String,
    val tilesReady: Boolean = true,
    val warmupStarted: Boolean = false,
) {
    val key: RadarTileMetadataKey
        get() = RadarTileMetadataKey(site, product, changesAgo)

    val layerFrameId: String
        get() = changesAgo?.let { "changes-$it" } ?: "latest"

    val rasterTileRequestUrl: String
        get() {
            val normalizedBaseUrl = BuildConfig.RADAR_TILE_BASE_URL.trimEnd('/')
            return if (rasterTileUrl.startsWith("http://") || rasterTileUrl.startsWith("https://")) {
                rasterTileUrl
            } else {
                "$normalizedBaseUrl/${rasterTileUrl.trimStart('/')}"
            }
        }

    val rasterTileRenderUrl: String
        get() = rasterTileRequestUrl.withRadarScanCacheBuster(scanTimeUtc)
}

internal suspend fun fetchRadarTileMetadata(
    siteId: String,
    product: RadarProduct,
    changesAgo: Int? = null,
): RadarTileMetadata =
    withContext(Dispatchers.IO) {
        val normalizedBaseUrl = BuildConfig.RADAR_TILE_BASE_URL.trimEnd('/')
        val framePath = changesAgo?.let { "changes/$it" } ?: "latest"
        val metadataUrl = "$normalizedBaseUrl/radar/$siteId/${product.pathSegment}/$framePath/metadata"
        Log.d(RADAR_LOG_TAG, "GET $metadataUrl")
        val connection = (URL(metadataUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = RADAR_TILE_CONNECT_TIMEOUT_MS
            readTimeout = RADAR_TILE_READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "StormPilot Android")
        }
        val responseBody = connection.readRadarResponseBody()
        Log.d(RADAR_LOG_TAG, "Radar metadata response body=${responseBody.truncateForLog()}")
        val responseJson = JSONObject(responseBody)
        val normalizedSite = responseJson.optString("site", siteId)
            .ifBlank { siteId }
            .uppercase()

        RadarTileMetadata(
            site = normalizedSite,
            product = product,
            changesAgo = changesAgo,
            scanTimeUtc = responseJson.optString("scanTimeUtc"),
            tileSize = responseJson.optInt("tileSize", 512),
            minZoom = responseJson.optInt("minZoom", 5),
            maxZoom = responseJson.optInt("maxZoom", 15),
            rasterNativeMaxZoom = responseJson.optInt(
                "rasterNativeMaxZoom",
                responseJson.optInt("nativeMaxZoom", RADAR_RASTER_NATIVE_MAX_ZOOM),
            ),
            vectorTileLayer = responseJson.optString("vectorTileLayer", product.pathSegment),
            vectorTileUrl = responseJson.optString(
                "vectorTileUrl",
                "/radar/$normalizedSite/${product.pathSegment}/$framePath/{z}/{x}/{y}.mvt",
            ),
            rasterTileUrl = responseJson.optString(
                "rasterTileUrl",
                "/radar/$normalizedSite/${product.pathSegment}/$framePath/{z}/{x}/{y}.png",
            ),
            tilesReady = responseJson.optBoolean("tilesReady", true),
            warmupStarted = responseJson.optBoolean(
                "warmupStarted",
                responseJson.optBoolean("warming", false),
            ),
        )
    }

private fun HttpURLConnection.readRadarResponseBody(): String {
    try {
        val responseCode = responseCode
        val stream = if (responseCode in 200..299) inputStream else errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        Log.d(
            RADAR_LOG_TAG,
            "Radar HTTP $responseCode ${requestMethod.orEmpty()} $url contentType=${contentType.orEmpty()}",
        )
        if (responseCode !in 200..299) {
            throw IOException(
                "Radar metadata request failed with HTTP $responseCode from $url: ${body.truncateForLog()}",
            )
        }
        return body
    } finally {
        disconnect()
    }
}

private fun String.truncateForLog(maxLength: Int = 600): String =
    if (length <= maxLength) this else take(maxLength) + "...(truncated)"

private fun String.withRadarScanCacheBuster(scanTimeUtc: String): String {
    val revision = scanTimeUtc.filter { it.isLetterOrDigit() }.ifBlank { return this }
    val separator = if ('?' in this) "&" else "?"
    return "$this${separator}scan=$revision"
}

internal fun String.toRadarScanTimeLabel(): String {
    return try {
        val instant = Instant.parse(this)
        val zoneId = ZoneId.of("America/New_York")
        val formatter = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
            .withZone(zoneId)
        val timePart = formatter.format(instant)
        val isDst = zoneId.rules.isDaylightSavings(instant)
        "$timePart ${if (isDst) "EDT" else "EST"}"
    } catch (e: Exception) {
        val time = substringAfter('T', missingDelimiterValue = "")
            .substringBefore('Z')
            .take(5)
        if (time.length == 5) "$time UTC" else this
    }
}

private const val RADAR_TILE_CONNECT_TIMEOUT_MS = 60_000
private const val RADAR_TILE_READ_TIMEOUT_MS = 60_000
internal const val RADAR_TILE_WARMUP_POLL_INTERVAL_MS = 2_000L
internal const val RADAR_SCAN_REFRESH_INTERVAL_MS = 5_000L
internal const val RADAR_LOG_TAG = "StormPilotRadar"
private const val RADAR_RASTER_NATIVE_MAX_ZOOM = 10
internal val RADAR_TILE_FADE_DURATION = 300.milliseconds
