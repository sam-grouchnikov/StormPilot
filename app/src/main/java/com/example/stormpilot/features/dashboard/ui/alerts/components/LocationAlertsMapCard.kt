package com.example.stormpilot.features.dashboard.ui.alerts.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.core.AppSettings
import kotlin.time.Duration.Companion.milliseconds
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Position
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun LocationAlertsMapCard(
    cityName: String,
    position: Position?,
    locationGeoJson: GeoJsonData,
    alertsGeoJson: GeoJsonData?,
    onAlertPolygonClick: (Position, String) -> Unit,
) {
    var showAlerts by remember { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.primaryContainer.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.50f)),
        tonalElevation = 2.dp,
    ) {

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.surface.copy(alpha = 0.44f),
                            border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(17.dp),
                                )
                                Text(
                                    text = cityName,
                                    color = colorScheme.onSurface,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.W700,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 6.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Text(
                            text = "Live Alert Map",
                            color = colorScheme.onSurface,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            fontSize = 28.sp,
                            lineHeight = 31.sp,
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (showAlerts) colorScheme.primary else colorScheme.surface.copy(alpha = 0.60f),
                        border = BorderStroke(
                            1.dp,
                            if (showAlerts) colorScheme.primary else colorScheme.outlineVariant,
                        ),
                    ) {
                        IconButton(
                            onClick = { showAlerts = !showAlerts },
                            modifier = Modifier.size(46.dp),
                        ) {
                            Icon(
                                imageVector = if (showAlerts) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showAlerts) "Hide alerts" else "Show alerts",
                                tint = if (showAlerts) colorScheme.onPrimary else colorScheme.primary,
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = showAlerts,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "alerts_visibility_status",
                ) { visible ->
                    Text(
                        text = if (visible) "Warning polygons are visible" else "Warning polygons hidden",
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 9.dp, top = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LocationAlertsMap(
                    position = position,
                    locationGeoJson = locationGeoJson,
                    alertsGeoJson = alertsGeoJson,
                    onAlertPolygonClick = onAlertPolygonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(238.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 0.5.dp,
                            color = colorScheme.outlineVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(18.dp),
                        ),
                    showAlerts = showAlerts,
                )
            }

    }
}

@Composable
private fun LocationAlertsMap(
    position: Position?,
    locationGeoJson: GeoJsonData,
    alertsGeoJson: GeoJsonData?,
    onAlertPolygonClick: (Position, String) -> Unit,
    modifier: Modifier = Modifier,
    showAlerts: Boolean,
) {
    var radarRefreshKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastTapPosition by remember { mutableStateOf<Position?>(null) }
    val mapTarget = position ?: Position(latitude = 39.8283, longitude = -98.5795)
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = mapTarget,
            zoom = 9.0,
        ),
    )

    LaunchedEffect(position) {
        position?.let { target ->
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = target,
                    zoom = 8.0,
                    tilt = 0.0,
                    bearing = 0.0,
                ),
                duration = 650.milliseconds,
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(300_000L)
            radarRefreshKey = System.currentTimeMillis()
        }
    }

    val mapStyle = if (AppSettings.isDarkMode) {
        "https://api.protomaps.com/styles/v5/dark/en.json?key=64a5f0a9c35b4ca1"
    } else {
        "https://api.protomaps.com/styles/v5/white/en.json?key=64a5f0a9c35b4ca1"
    }

    MaplibreMap(
        baseStyle = BaseStyle.Uri(mapStyle),
        cameraState = cameraState,
        onMapClick = { point, _ ->
            lastTapPosition = point
            ClickResult.Pass
        },
        modifier = modifier,
        options = MapOptions(
            gestureOptions = GestureOptions.AllDisabled,
            ornamentOptions = OrnamentOptions(
                padding = PaddingValues(0.dp),
                isLogoEnabled = false,
                isAttributionEnabled = false,
                isCompassEnabled = false,
                isScaleBarEnabled = false,
            ),
        ),
    ) {
        val fallbackJson = """{"type":"FeatureCollection","features":[]}"""
        val safeGeoJson = alertsGeoJson ?: GeoJsonData.JsonString(fallbackJson)
        val alertsSource = rememberGeoJsonSource(data = safeGeoJson)

        if (showAlerts) {
            FillLayer(
                id = "location-alerts-fill",
                source = alertsSource,
                color = switch(
                    condition(Feature[const("prod_type")].asString() eq const("Tornado Warning"), const(Color(0x22FF0000))),
                    condition(Feature[const("prod_type")].asString() eq const("Severe Thunderstorm Warning"), const(Color(0x22FFD700))),
                    condition(Feature[const("prod_type")].asString() eq const("Flash Flood Warning"), const(Color(0x2200BB00))),
                    fallback = const(Color.Transparent),
                ),
                onClick = { features ->
                    val eventType = features.firstOrNull()
                        ?.properties
                        ?.get("prod_type")
                        ?.jsonPrimitive
                        ?.contentOrNull
                    val tappedPosition = lastTapPosition

                    if (eventType != null && tappedPosition != null) {
                        onAlertPolygonClick(tappedPosition, eventType)
                        ClickResult.Consume
                    } else {
                        ClickResult.Pass
                    }
                },
            )

            LineLayer(
                id = "location-alerts-outline",
                source = alertsSource,
                color = switch(
                    condition(Feature[const("prod_type")].asString() eq const("Tornado Warning"), const(Color(0xC8FF3030))),
                    condition(Feature[const("prod_type")].asString() eq const("Severe Thunderstorm Warning"), const(Color(0xFFFFB020))),
                    condition(Feature[const("prod_type")].asString() eq const("Flash Flood Warning"), const(Color(0xC800E676))),
                    fallback = const(Color.Transparent),
                ),
                width = const(2.dp),
                onClick = { features ->
                    val eventType = features.firstOrNull()
                        ?.properties
                        ?.get("prod_type")
                        ?.jsonPrimitive
                        ?.contentOrNull
                    val tappedPosition = lastTapPosition

                    if (eventType != null && tappedPosition != null) {
                        onAlertPolygonClick(tappedPosition, eventType)
                        ClickResult.Consume
                    } else {
                        ClickResult.Pass
                    }
                },
            )
        }

        val locationSource = rememberGeoJsonSource(data = locationGeoJson)
        CircleLayer(
            id = "location-current-point",
            source = locationSource,
            color = const(MaterialTheme.colorScheme.onPrimary),
            radius = const(5.dp),
            strokeColor = const(MaterialTheme.colorScheme.primary),
            strokeWidth = const(3.dp),
        )
    }
}
