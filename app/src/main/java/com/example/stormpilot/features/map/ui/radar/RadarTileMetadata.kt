package com.example.stormpilot.features.map.ui.radar

import android.util.Log
import com.example.stormpilot.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

internal data class RadarScanEvent(
    val site: String,
    val product: RadarProduct,
    val scanId: String,
    val scanTimeUtc: String,
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

internal fun radarScanEvents(
    siteId: String,
    product: RadarProduct,
): Flow<RadarScanEvent> = channelFlow {
    var activeConnection: HttpURLConnection? = null
    val collector = launch(Dispatchers.IO) {
        var reconnectDelayMs = RADAR_SCAN_EVENT_RECONNECT_INITIAL_DELAY_MS
        while (isActive) {
            try {
                val normalizedBaseUrl = BuildConfig.RADAR_TILE_BASE_URL.trimEnd('/')
                val eventsUrl = "$normalizedBaseUrl/radar/$siteId/${product.pathSegment}/events"
                Log.d(RADAR_LOG_TAG, "Listening for radar scan events from $eventsUrl")

                val connection = (URL(eventsUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = RADAR_TILE_CONNECT_TIMEOUT_MS
                    readTimeout = RADAR_SCAN_EVENT_READ_TIMEOUT_MS
                    setRequestProperty("Accept", "text/event-stream")
                    setRequestProperty("Cache-Control", "no-cache")
                    setRequestProperty("User-Agent", "StormPilot Android")
                }
                activeConnection = connection
                connection.readRadarScanEvents(siteId, product) { event ->
                    trySend(event).isSuccess
                }
                reconnectDelayMs = RADAR_SCAN_EVENT_RECONNECT_INITIAL_DELAY_MS
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(
                        RADAR_LOG_TAG,
                        "Radar scan event stream unavailable for site=$siteId, " +
                                "product=${product.pathSegment}",
                        e,
                    )
                }
            } finally {
                activeConnection?.disconnect()
                activeConnection = null
            }

            if (isActive) {
                delay(reconnectDelayMs)
                reconnectDelayMs = (reconnectDelayMs * 2)
                    .coerceAtMost(RADAR_SCAN_EVENT_RECONNECT_MAX_DELAY_MS)
            }
        }
    }

    awaitClose {
        activeConnection?.disconnect()
        collector.cancel()
    }
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

private fun HttpURLConnection.readRadarScanEvents(
    siteId: String,
    product: RadarProduct,
    onEvent: (RadarScanEvent) -> Unit,
) {
    try {
        val responseCode = responseCode
        Log.d(
            RADAR_LOG_TAG,
            "Radar SSE HTTP $responseCode ${requestMethod.orEmpty()} $url " +
                    "contentType=${contentType.orEmpty()}",
        )
        if (responseCode !in 200..299) {
            val body = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            throw IOException(
                "Radar scan event stream failed with HTTP $responseCode from $url: " +
                        body.truncateForLog(),
            )
        }

        val dataLines = mutableListOf<String>()
        inputStream.bufferedReader().use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                when {
                    line.isEmpty() -> {
                        dispatchRadarScanEvent(dataLines, siteId, product, onEvent)
                    }
                    line.startsWith(":") -> Unit
                    line.startsWith("data:") -> {
                        dataLines += line.removePrefix("data:").trimStart()
                    }
                }
            }
        }
        dispatchRadarScanEvent(dataLines, siteId, product, onEvent)
    } finally {
        disconnect()
    }
}

private fun dispatchRadarScanEvent(
    dataLines: MutableList<String>,
    siteId: String,
    product: RadarProduct,
    onEvent: (RadarScanEvent) -> Unit,
) {
    if (dataLines.isEmpty()) return
    val event = dataLines.joinToString(separator = "\n")
        .toRadarScanEventOrNull(siteId, product)
    dataLines.clear()
    if (event != null) {
        onEvent(event)
    }
}

private fun String.toRadarScanEventOrNull(
    siteId: String,
    product: RadarProduct,
): RadarScanEvent? {
    return try {
        val json = JSONObject(this)
        if (json.optString("event") != RADAR_SCAN_READY_EVENT) {
            return null
        }
        RadarScanEvent(
            site = json.optString("site", siteId).ifBlank { siteId }.uppercase(),
            product = json.optString("product", product.pathSegment)
                .toRadarProductOrNull()
                ?: product,
            scanId = json.optString("scanId"),
            scanTimeUtc = json.optString("scanTimeUtc"),
        )
    } catch (e: Exception) {
        Log.w(RADAR_LOG_TAG, "Ignoring malformed radar scan event: ${truncateForLog()}", e)
        null
    }
}

private fun String.toRadarProductOrNull(): RadarProduct? =
    RadarProduct.entries.firstOrNull { product -> product.pathSegment == this }

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
private const val RADAR_SCAN_EVENT_READ_TIMEOUT_MS = 45_000
private const val RADAR_SCAN_EVENT_RECONNECT_INITIAL_DELAY_MS = 1_000L
private const val RADAR_SCAN_EVENT_RECONNECT_MAX_DELAY_MS = 15_000L
private const val RADAR_SCAN_READY_EVENT = "scan_ready"
internal const val RADAR_TILE_WARMUP_POLL_INTERVAL_MS = 2_000L
internal const val RADAR_LOG_TAG = "StormPilotRadar"
private const val RADAR_RASTER_NATIVE_MAX_ZOOM = 10
internal val RADAR_TILE_FADE_DURATION = 300.milliseconds
