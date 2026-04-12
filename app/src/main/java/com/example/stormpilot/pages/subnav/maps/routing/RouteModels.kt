package com.example.stormpilot.pages.subnav.maps.routing

import org.maplibre.spatialk.geojson.Position

/**
 * A single navigation step in a route.
 */
data class RouteStep(
    val instruction: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val maneuverLocation: Position? = null,
)

/**
 * Normalized route response consumed by the UI layer.
 */
data class RouteResult(
    val polyline: List<Position>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val steps: List<RouteStep>,
)
