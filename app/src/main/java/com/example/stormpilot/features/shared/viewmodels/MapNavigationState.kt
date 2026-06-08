package com.example.stormpilot.features.shared.viewmodels
import com.example.stormpilot.features.shared.data.routing.RouteStep
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

fun routeBearingDegrees(position: Position, routePolyline: List<Position>): Double? {
    return routePolyline
        .zipWithNext()
        .filter { (start, end) -> approximateDistanceMeters(start, end) > 0.5 }
        .minByOrNull { (start, end) -> approximateDistanceToSegmentMeters(position, start, end) }
        ?.let { (start, end) -> bearingDegrees(start, end) }
}

fun routeProgressMeters(position: Position, routePolyline: List<Position>): Double? {
    if (routePolyline.size < 2) return null

    var distanceBeforeSegment = 0.0
    var closestProgressMeters: Double? = null
    var closestDistanceMeters = Double.MAX_VALUE

    routePolyline.zipWithNext().forEach { (start, end) ->
        val segmentLengthMeters = approximateDistanceMeters(start, end)
        if (segmentLengthMeters > 0.5) {
            val projection = projectOntoSegment(position, start, end)
            val distanceMeters = approximateDistanceMeters(position, projection.position)
            if (distanceMeters < closestDistanceMeters) {
                closestDistanceMeters = distanceMeters
                closestProgressMeters = distanceBeforeSegment + (segmentLengthMeters * projection.fraction)
            }
        }
        distanceBeforeSegment += segmentLengthMeters
    }

    return closestProgressMeters
}

fun routeStepIndexForProgress(steps: List<RouteStep>, progressMeters: Double): Int? {
    if (steps.isEmpty()) return null

    var completedDistanceMeters = 0.0
    steps.forEachIndexed { index, step ->
        val stepEndMeters = completedDistanceMeters + step.distanceMeters
        if (progressMeters < stepEndMeters) {
            return index
        }
        completedDistanceMeters = stepEndMeters
    }

    return steps.lastIndex
}

fun initialRouteStepIndex(steps: List<RouteStep>, skipDepartStep: Boolean): Int {
    if (!skipDepartStep || steps.size <= 1) return 0
    return if (steps.first().maneuver?.type == "depart") 1 else 0
}

fun navigationCameraBearingDegrees(
    userPosition: Position,
    routePolyline: List<Position>,
    isOffRoute: Boolean,
    userBearingDegrees: Double?,
    previousBearingDegrees: Double? = null,
): Double? {
    val normalizedUserBearing = userBearingDegrees?.normalizeBearingDegrees()
    val normalizedPreviousBearing = previousBearingDegrees?.normalizeBearingDegrees()
    return if (isOffRoute) {
        normalizedUserBearing ?: normalizedPreviousBearing
    } else {
        routeBearingDegrees(userPosition, routePolyline)
            ?: normalizedUserBearing
            ?: normalizedPreviousBearing
    }
}

fun Double.normalizeBearingDegrees(): Double = ((this % 360.0) + 360.0) % 360.0

private fun approximateDistanceToSegmentMeters(point: Position, start: Position, end: Position): Double {
    return approximateDistanceMeters(point, projectOntoSegment(point, start, end).position)
}

private fun projectOntoSegment(point: Position, start: Position, end: Position): SegmentProjection {
    val lat0 = Math.toRadians(point.latitude)
    val px = (Math.toRadians(point.longitude - start.longitude)) * cos(lat0)
    val py = Math.toRadians(point.latitude - start.latitude)
    val sx = 0.0
    val sy = 0.0
    val ex = (Math.toRadians(end.longitude - start.longitude)) * cos(lat0)
    val ey = Math.toRadians(end.latitude - start.latitude)

    val dx = ex - sx
    val dy = ey - sy
    val lengthSq = dx * dx + dy * dy

    val t = if (lengthSq == 0.0) 0.0 else ((px - sx) * dx + (py - sy) * dy) / lengthSq
    val clampedT = t.coerceIn(0.0, 1.0)

    val closestLongitude = start.longitude + Math.toDegrees((sx + clampedT * dx) / cos(lat0))
    val closestLatitude = start.latitude + Math.toDegrees(sy + clampedT * dy)
    return SegmentProjection(
        position = Position(closestLongitude, closestLatitude),
        fraction = clampedT,
    )
}

private data class SegmentProjection(
    val position: Position,
    val fraction: Double,
)

private const val ARRIVAL_DISTANCE_THRESHOLD_METERS = 30.0
