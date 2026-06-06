package com.example.stormpilot.features.shared.data.routing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class RouteWarningAnalysis(
    val warningCount: Int,
    val destinationInsideWarning: Boolean,
)

data class RouteWarningBounds(
    val minLongitude: Double,
    val minLatitude: Double,
    val maxLongitude: Double,
    val maxLatitude: Double,
)

object RouteWarningCounter {
    private val json = Json { ignoreUnknownKeys = true }

    fun countWarningsIntersectingRoute(alertsGeoJson: String, routePolyline: List<Position>): Int {
        return analyzeWarnings(alertsGeoJson, routePolyline).warningCount
    }

    fun analyzeWarnings(
        alertsGeoJson: String,
        routePolyline: List<Position>,
        destination: Position? = null,
    ): RouteWarningAnalysis {
        val features = json.parseToJsonElement(alertsGeoJson).jsonObject["features"]?.asArrayOrNull()
            ?: return RouteWarningAnalysis(warningCount = 0, destinationInsideWarning = false)
        var count = 0
        var destinationInsideWarning = false
        for (feature in features) {
            val geometry = feature.asObjectOrNull()?.get("geometry")?.asObjectOrNull() ?: continue
            val intersectsRoute = routePolyline.size >= 2 && geometryIntersectsRoute(geometry, routePolyline)
            val containsDestination = destination?.let { geometryContainsPoint(geometry, it.toPoint()) } ?: false
            if (containsDestination) {
                destinationInsideWarning = true
            }
            if (intersectsRoute || containsDestination) {
                count += 1
            }
        }
        return RouteWarningAnalysis(
            warningCount = count,
            destinationInsideWarning = destinationInsideWarning,
        )
    }

    fun warningBoundsIntersectingRoute(
        alertsGeoJson: String,
        routePolyline: List<Position>,
    ): List<RouteWarningBounds> {
        if (routePolyline.size < 2) return emptyList()

        val features = json.parseToJsonElement(alertsGeoJson).jsonObject["features"]?.asArrayOrNull()
            ?: return emptyList()
        val bounds = mutableListOf<RouteWarningBounds>()
        for (feature in features) {
            val geometry = feature.asObjectOrNull()?.get("geometry")?.asObjectOrNull() ?: continue
            bounds += warningBoundsIntersectingRoute(geometry, routePolyline)
        }
        return bounds
    }

    private fun geometryIntersectsRoute(geometry: JsonObject, routePolyline: List<Position>): Boolean {
        val coordinates = geometry["coordinates"]?.asArrayOrNull() ?: return false
        return when (geometry["type"]?.jsonPrimitive?.contentOrNull) {
            "Polygon" -> polygonIntersectsRoute(coordinates, routePolyline)
            "MultiPolygon" -> {
                for (polygon in coordinates) {
                    if (polygonIntersectsRoute(polygon.asArrayOrNull() ?: continue, routePolyline)) {
                        return true
                    }
                }
                false
            }
            else -> false
        }
    }

    private fun warningBoundsIntersectingRoute(
        geometry: JsonObject,
        routePolyline: List<Position>,
    ): List<RouteWarningBounds> {
        val coordinates = geometry["coordinates"]?.asArrayOrNull() ?: return emptyList()
        return when (geometry["type"]?.jsonPrimitive?.contentOrNull) {
            "Polygon" -> {
                if (polygonIntersectsRoute(coordinates, routePolyline)) {
                    listOfNotNull(coordinates.toBoundsOrNull())
                } else {
                    emptyList()
                }
            }
            "MultiPolygon" -> {
                val bounds = mutableListOf<RouteWarningBounds>()
                for (polygon in coordinates) {
                    val polygonCoordinates = polygon.asArrayOrNull() ?: continue
                    if (polygonIntersectsRoute(polygonCoordinates, routePolyline)) {
                        polygonCoordinates.toBoundsOrNull()?.let(bounds::add)
                    }
                }
                bounds
            }
            else -> emptyList()
        }
    }

    private fun geometryContainsPoint(geometry: JsonObject, point: Point): Boolean {
        val coordinates = geometry["coordinates"]?.asArrayOrNull() ?: return false
        return when (geometry["type"]?.jsonPrimitive?.contentOrNull) {
            "Polygon" -> polygonContainsPoint(coordinates, point)
            "MultiPolygon" -> {
                for (polygon in coordinates) {
                    if (polygonContainsPoint(polygon.asArrayOrNull() ?: continue, point)) {
                        return true
                    }
                }
                false
            }
            else -> false
        }
    }

    private fun polygonIntersectsRoute(polygonCoordinates: JsonArray, routePolyline: List<Position>): Boolean {
        val rings = polygonCoordinates.toRings()
        if (rings.firstOrNull().isNullOrEmpty()) return false

        val routePoints = routePolyline.map { it.toPoint() }
        if (routePoints.any { pointInPolygon(it, rings) }) return true

        return routePoints.windowed(2).any { segment ->
            rings.any { ring ->
                ringSegments(ring).any { edge ->
                    segmentsIntersect(segment[0], segment[1], edge.first, edge.second)
                }
            }
        }
    }

    private fun polygonContainsPoint(polygonCoordinates: JsonArray, point: Point): Boolean {
        val rings = polygonCoordinates.toRings()
        if (rings.firstOrNull().isNullOrEmpty()) return false
        return pointInPolygon(point, rings)
    }

    private fun JsonArray.toBoundsOrNull(): RouteWarningBounds? {
        val points = toRings().flatten()
        if (points.isEmpty()) return null
        return RouteWarningBounds(
            minLongitude = points.minOf { it.x },
            minLatitude = points.minOf { it.y },
            maxLongitude = points.maxOf { it.x },
            maxLatitude = points.maxOf { it.y },
        )
    }

    private fun JsonArray.toRings(): List<List<Point>> {
        val rings = mutableListOf<List<Point>>()
        for (rawRingElement in this) {
            val rawRing = rawRingElement.asArrayOrNull() ?: continue
            val ring = mutableListOf<Point>()
            for (rawPoint in rawRing) {
                rawPoint.asArrayOrNull()?.toPointOrNull()?.let(ring::add)
            }
            if (ring.size >= 3) rings.add(ring)
        }
        return rings
    }

    private fun JsonArray.toPointOrNull(): Point? {
        if (size < 2) return null
        return Point(
            x = this[0].jsonPrimitive.doubleOrNull ?: return null,
            y = this[1].jsonPrimitive.doubleOrNull ?: return null,
        )
    }

    private fun JsonElement.asArrayOrNull(): JsonArray? = this as? JsonArray

    private fun JsonElement.asObjectOrNull(): JsonObject? = this as? JsonObject

    private fun Position.toPoint(): Point = Point(x = longitude, y = latitude)

    private fun pointInPolygon(point: Point, rings: List<List<Point>>): Boolean {
        val exterior = rings.firstOrNull() ?: return false
        if (!pointInRing(point, exterior)) return false
        return rings.drop(1).none { pointInRing(point, it) }
    }

    private fun pointInRing(point: Point, ring: List<Point>): Boolean {
        var inside = false
        var previousIndex = ring.lastIndex
        for (index in ring.indices) {
            val current = ring[index]
            val previous = ring[previousIndex]
            if (pointOnSegment(point, previous, current)) return true
            val crossesRay = (current.y > point.y) != (previous.y > point.y)
            if (crossesRay) {
                val intersectionX = (previous.x - current.x) * (point.y - current.y) /
                        (previous.y - current.y) + current.x
                if (point.x < intersectionX) inside = !inside
            }
            previousIndex = index
        }
        return inside
    }

    private fun ringSegments(ring: List<Point>): Sequence<Pair<Point, Point>> = sequence {
        if (ring.size < 2) return@sequence
        for (index in ring.indices) {
            yield(ring[index] to ring[(index + 1) % ring.size])
        }
    }

    private fun segmentsIntersect(a: Point, b: Point, c: Point, d: Point): Boolean {
        val abC = cross(a, b, c)
        val abD = cross(a, b, d)
        val cdA = cross(c, d, a)
        val cdB = cross(c, d, b)

        if (abC.hasOppositeSign(abD) && cdA.hasOppositeSign(cdB)) return true
        if (abs(abC) <= EPSILON && pointOnSegment(c, a, b)) return true
        if (abs(abD) <= EPSILON && pointOnSegment(d, a, b)) return true
        if (abs(cdA) <= EPSILON && pointOnSegment(a, c, d)) return true
        if (abs(cdB) <= EPSILON && pointOnSegment(b, c, d)) return true

        return false
    }

    private fun pointOnSegment(point: Point, segmentStart: Point, segmentEnd: Point): Boolean {
        if (abs(cross(segmentStart, segmentEnd, point)) > EPSILON) return false
        return point.x >= min(segmentStart.x, segmentEnd.x) - EPSILON &&
                point.x <= max(segmentStart.x, segmentEnd.x) + EPSILON &&
                point.y >= min(segmentStart.y, segmentEnd.y) - EPSILON &&
                point.y <= max(segmentStart.y, segmentEnd.y) + EPSILON
    }

    private fun cross(a: Point, b: Point, c: Point): Double =
        (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

    private fun Double.hasOppositeSign(other: Double): Boolean =
        (this > EPSILON && other < -EPSILON) || (this < -EPSILON && other > EPSILON)

    private data class Point(val x: Double, val y: Double)

    private const val EPSILON = 1e-12
}
