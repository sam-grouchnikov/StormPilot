package com.example.stormpilot.features.map.ui.radar

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.RasterResampling
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.TileSetOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.util.ClickResult

@Composable
internal fun RadarRasterLayers(
    activeMetadata: RadarTileMetadata?,
    playbackMetadata: List<RadarTileMetadata>,
) {
    val activeKey = activeMetadata?.key
    val frameMetadata = remember(activeMetadata, playbackMetadata) {
        buildList {
            val seenKeys = mutableSetOf<RadarTileMetadataKey>()
            playbackMetadata.forEach { metadata ->
                if (seenKeys.add(metadata.key)) {
                    add(metadata)
                }
            }
            activeMetadata?.let { metadata ->
                if (seenKeys.add(metadata.key)) {
                    add(metadata)
                }
            }
        }
    }

    frameMetadata.forEach { metadata ->
        val layerId = metadata.radarLayerId()
        key(layerId) {
            val targetOpacity = if (metadata.key == activeKey) {
                RADAR_RASTER_OPACITY
            } else {
                RADAR_RASTER_PRELOAD_OPACITY
            }
            val opacity = animateFloatAsState(
                targetValue = targetOpacity,
                animationSpec = tween(durationMillis = RADAR_PLAYBACK_FRAME_FADE_MS),
                label = "radar_frame_opacity",
            )
            RadarRasterLayer(
                id = layerId,
                metadata = metadata,
                opacity = opacity.value,
            )
        }
    }
}

@Composable
private fun RadarRasterLayer(
    id: String,
    metadata: RadarTileMetadata,
    opacity: Float,
) {
    val radarSource = rememberRasterSource(
        tiles = listOf(metadata.rasterTileRequestUrl),
        options = TileSetOptions(
            minZoom = metadata.minZoom,
            maxZoom = metadata.rasterNativeMaxZoom,
        ),
        tileSize = metadata.tileSize,
    )
    RasterLayer(
        id = id,
        source = radarSource,
        opacity = const(opacity),
        resampling = const(RasterResampling.Linear),
        fadeDuration = const(RADAR_TILE_FADE_DURATION),
    )
}

private fun RadarTileMetadata.radarLayerId(): String =
    "radar-$site-${product.pathSegment}-$layerFrameId-layer"

@Composable
internal fun RadarSiteLayers(
    visible: Boolean,
    selectedSite: NexradSite?,
    onSiteSelected: (NexradSite) -> Unit,
) {
    if (!visible) {
        return
    }

    val radarSitesFeatureCollection = remember {
        GeoJsonData.JsonString(nexradSitesGeoJson(NexradSites))
    }
    val radarSitesSource = rememberGeoJsonSource(data = radarSitesFeatureCollection)
    CircleLayer(
        id = "radar-site-markers",
        source = radarSitesSource,
        minZoom = 2.0f,
        color = const(Color(0xFFEAF9FF)),
        radius = const(7.dp),
        strokeColor = const(Color(0xFF1D252B)),
        strokeWidth = const(1.5.dp),
        onClick = { features ->
            val siteId = features.firstOrNull()
                ?.properties
                ?.get("id")
                ?.jsonPrimitive
                ?.contentOrNull
            val site = siteId?.let { tappedId ->
                NexradSites.firstOrNull { it.id == tappedId }
            }

            if (site != null) {
                Log.d(
                    RADAR_LOG_TAG,
                    "Radar site selected id=${site.id}, lat=${site.latitude}, lon=${site.longitude}",
                )
                onSiteSelected(site)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
    )

    selectedSite?.let { site ->
        val selectedRadarSiteFeatureCollection = remember(site) {
            GeoJsonData.JsonString(nexradSitesGeoJson(listOf(site)))
        }
        val selectedRadarSiteSource = rememberGeoJsonSource(
            data = selectedRadarSiteFeatureCollection,
        )
        CircleLayer(
            id = "selected-radar-site-marker",
            source = selectedRadarSiteSource,
            minZoom = 2.0f,
            color = const(MaterialTheme.colorScheme.primary),
            radius = const(7.dp),
            strokeColor = const(MaterialTheme.colorScheme.onPrimary),
            strokeWidth = const(2.dp),
        )
    }
}

private const val RADAR_RASTER_OPACITY = 0.62f
private const val RADAR_RASTER_PRELOAD_OPACITY = 0.0001f
private const val RADAR_PLAYBACK_FRAME_FADE_MS = 120
