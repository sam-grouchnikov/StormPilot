package com.example.stormpilot.features.shared.data.routing

import org.maplibre.spatialk.geojson.Position

data class RouteStep(
    val instruction: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val maneuverLocation: Position? = null,
    val maneuver: Maneuver?,
)

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
