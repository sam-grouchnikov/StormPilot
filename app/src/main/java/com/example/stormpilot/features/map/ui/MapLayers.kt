package com.example.stormpilot.features.map.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.shared.data.search.PhotonFeature
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.ui.theme.extendedColors
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.contains
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature as GeoJsonFeature
import org.maplibre.spatialk.geojson.Position

@Composable
internal fun UserLocationLayer(origin: Position?) {
    val userLocationFeatureCollection = remember(origin) {
        val json = if (origin != null) {
            """
            {
              "type": "FeatureCollection",
              "features": [{
                "type": "Feature",
                "geometry": {
                  "type": "Point",
                  "coordinates": [${origin.longitude}, ${origin.latitude}]
                },
                "properties": {}
              }]
            }
            """.trimIndent()
        } else {
            """{"type": "FeatureCollection", "features": []}"""
        }
        GeoJsonData.JsonString(json)
    }

    val colors = MaterialTheme.extendedColors
    val userLocationSource = rememberGeoJsonSource(data = userLocationFeatureCollection)
    CircleLayer(
        id = "user-location-shadow",
        source = userLocationSource,
        color = const(Color.Black.copy(alpha = 0.65f)),
        radius = const(12.5.dp),
        blur = const(0.85f),
        translate = const(DpOffset(0.dp, 1.dp)),
    )
    CircleLayer(
        id = "user-location",
        source = userLocationSource,
        color = const(colors.locationCircle),
        radius = const(6.5.dp),
        strokeColor = const(Color.White),
        strokeWidth = const(2.5.dp),
    )
}

@Composable
internal fun DestinationLayer(destination: Position?) {
    val destinationFeatureCollection = remember(destination) {
        val json = if (destination != null) {
            """
            {
              "type": "FeatureCollection",
              "features": [{
                "type": "Feature",
                "geometry": {
                  "type": "Point",
                  "coordinates": [${destination.longitude}, ${destination.latitude}]
                },
                "properties": {}
              }]
            }
            """.trimIndent()
        } else {
            """{"type": "FeatureCollection", "features": []}"""
        }
        GeoJsonData.JsonString(json)
    }

    val destinationSource = rememberGeoJsonSource(data = destinationFeatureCollection)
    CircleLayer(
        id = "destination-location",
        source = destinationSource,
        color = const(MaterialTheme.colorScheme.error),
        radius = const(6.dp),
        strokeColor = const(MaterialTheme.colorScheme.onError),
        strokeWidth = const(3.dp),
    )
}

@Composable
internal fun RouteLayer(
    routeGeoJson: GeoJsonData?,
    isNavigationMode: Boolean,
    colors: ExtendedColors,
) {
    val routeSource = rememberGeoJsonSource(
        data = routeGeoJson ?: GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}"""),
    )

    LineLayer(
        id = "route-line",
        source = routeSource,
        color = const(colors.routingLine),
        width = const(if (isNavigationMode) 12.dp else 5.dp),
    )
}

@Composable
internal fun SevereAlertsLayers(
    visible: Boolean,
    alertsGeoJson: GeoJsonData?,
    opacity: Float,
    lastMapTapPosition: Position?,
    onAlertTapped: (Position, String) -> Unit,
) {
    val alertsSource = rememberGeoJsonSource(
        data = alertsGeoJson ?: GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}"""),
    )

    if (!visible) {
        return
    }

    val alertEventType = Feature[const("prod_type")].asString(
        Feature[const("event")],
        Feature[const("headline")],
        const(""),
    )
    FillLayer(
        id = "alerts-fill",
        source = alertsSource,
        opacity = const(opacity),
        color = switch(
            condition(
                alertEventType.contains("Tornado Warning"),
                const(Color(0x1AFF0000)),
            ),
            condition(
                alertEventType.contains("Severe Thunderstorm Warning"),
                const(Color(0x1AFF9F15)),
            ),
            condition(
                alertEventType.contains("Flash Flood Warning"),
                const(Color(0x1A00BB00)),
            ),
            fallback = const(Color.Transparent),
        ),
        onClick = { features ->
            handleAlertLayerClick(features, lastMapTapPosition, onAlertTapped)
        },
    )

    LineLayer(
        id = "alerts-outline-border",
        source = alertsSource,
        color = const(Color.Black),
        width = const(4.dp), // 2dp line + 1dp border on each side
        onClick = { features ->
            handleAlertLayerClick(features, lastMapTapPosition, onAlertTapped)
        },
    )

    LineLayer(
        id = "alerts-outline",
        source = alertsSource,
        color = switch(
            condition(
                alertEventType.contains("Tornado Warning"),
                const(Color(0x80FF0000)),
            ),
            condition(
                alertEventType.contains("Severe Thunderstorm Warning"),
                const(Color(0xFFD26D03)),
            ),
            condition(
                alertEventType.contains("Flash Flood Warning"),
                const(Color(0x8000FF00)),
            ),
            fallback = const(Color.Transparent),
        ),
        width = const(2.dp),
        onClick = { features ->
            handleAlertLayerClick(features, lastMapTapPosition, onAlertTapped)
        },
    )


}

@Composable
internal fun SearchResultLayers(
    mapSearchResults: List<PhotonFeature>,
    focusedSearchResult: PhotonFeature?,
) {
    val visibleSearchResults = mapSearchResults.take(5)
    val focusedSearchResultForMarkers = focusedSearchResult
        ?.takeIf { focused -> visibleSearchResults.any { it == focused } }
    val unfocusedSearchResults = visibleSearchResults.filterNot {
        it == focusedSearchResultForMarkers
    }
    val searchResultsFeatureCollection = remember(unfocusedSearchResults) {
        GeoJsonData.JsonString(searchResultsGeoJson(unfocusedSearchResults))
    }
    val searchResultsSource = rememberGeoJsonSource(data = searchResultsFeatureCollection)
    CircleLayer(
        id = "search-result-marker",
        source = searchResultsSource,
        color = const(MaterialTheme.colorScheme.error.copy(alpha = 0.80f)),
        radius = const(6.dp),
        strokeColor = const(MaterialTheme.colorScheme.onError.copy(alpha = 0.85f)),
        strokeWidth = const(2.dp),
    )

    val focusedSearchResultFeatureCollection = remember(focusedSearchResultForMarkers) {
        GeoJsonData.JsonString(
            searchResultsGeoJson(listOfNotNull(focusedSearchResultForMarkers)),
        )
    }
    val focusedSearchResultSource = rememberGeoJsonSource(
        data = focusedSearchResultFeatureCollection,
    )
    CircleLayer(
        id = "focused-search-result-marker",
        source = focusedSearchResultSource,
        color = const(MaterialTheme.colorScheme.error),
        radius = const(9.dp),
        strokeColor = const(MaterialTheme.colorScheme.onError),
        strokeWidth = const(3.dp),
    )
}

private fun handleAlertLayerClick(
    features: List<GeoJsonFeature<*, JsonObject?>>,
    tappedPosition: Position?,
    onAlertTapped: (Position, String) -> Unit,
): ClickResult {
    val eventType = features.firstOrNull()?.alertEventType()
    return if (eventType != null && tappedPosition != null) {
        onAlertTapped(tappedPosition, eventType)
        ClickResult.Consume
    } else {
        ClickResult.Pass
    }
}

private fun searchResultsGeoJson(results: List<PhotonFeature>): String {
    val features = results.take(5).joinToString(",") { result ->
        """
        {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [${result.geometry.longitude}, ${result.geometry.latitude}]
          },
          "properties": {}
        }
        """.trimIndent()
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private val alertEventPropertyKeys = listOf("prod_type", "event", "headline")

private fun GeoJsonFeature<*, JsonObject?>.alertEventType(): String? {
    return alertEventPropertyKeys.firstNotNullOfOrNull { key ->
        properties
            ?.get(key)
            ?.jsonPrimitive
            ?.contentOrNull
            ?.takeIf { it.isNotBlank() }
    }
}
