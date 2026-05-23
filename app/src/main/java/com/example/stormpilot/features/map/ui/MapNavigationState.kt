package com.example.stormpilot.features.map.ui

import com.example.stormpilot.data.MapsUiState
import org.maplibre.spatialk.geojson.Position
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MapsFooterState {
    Hidden,
    Summary,
    Navigation,
}

fun navigationInstruction(uiState: MapsUiState): String {
    val steps = uiState.steps
    if (steps.isEmpty()) return "Continue on route"

    val currentStep = steps.getOrNull(uiState.currentStepIndex) ?: return "Continue on route"
    val destination = uiState.destination
    val origin = uiState.origin
    val onLastStep = uiState.currentStepIndex >= steps.lastIndex
    val closeToDestination =
        destination != null &&
            origin != null &&
            approximateDistanceMeters(origin, destination) <= ARRIVAL_DISTANCE_THRESHOLD_METERS

    if (onLastStep && !closeToDestination) {
        return "Head straight"
    }

    return currentStep.instruction
}

fun approximateDistanceMeters(start: Position, end: Position): Double {
    val latMeters = (end.latitude - start.latitude) * 111_320.0
    val lonMeters =
        (end.longitude - start.longitude) * 111_320.0 * cos(Math.toRadians((start.latitude + end.latitude) / 2.0))
    return sqrt((latMeters * latMeters) + (lonMeters * lonMeters))
}

fun bearingDegrees(from: Position, to: Position): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) -
        sin(lat1) * cos(lat2) * cos(dLon)
    val bearing = Math.toDegrees(atan2(y, x))
    return (bearing + 360.0) % 360.0
}

private const val ARRIVAL_DISTANCE_THRESHOLD_METERS = 30.0
