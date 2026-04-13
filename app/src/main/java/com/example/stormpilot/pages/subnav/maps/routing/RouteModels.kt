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
    val maneuver: Maneuver?,
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

data class Maneuver(
    val type: String,
    val modifier: String?,
    val exit: Int?,
    val bearingBefore: Int,
    val bearingAfter: Int,
    val location: Position,
)
