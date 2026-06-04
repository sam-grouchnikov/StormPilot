package com.example.stormpilot.features.map.data.routing

import org.maplibre.spatialk.geojson.Position
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max

object StormDetourPlanner {
    private val marginMeters = listOf(3_000.0, 10_000.0, 25_000.0)

    fun buildWaypointSets(
        alertsGeoJson: String,
        seedRoutes: List<RouteResult>,
        origin: Position,
        destination: Position,
    ): List<List<Position>> {
        val warningBounds = seedRoutes
            .flatMap { route ->
                RouteWarningCounter.warningBoundsIntersectingRoute(alertsGeoJson, route.polyline)
            }
            .distinctBy { it.key() }
            .take(MAX_WARNING_BOUNDS)

        if (warningBounds.isEmpty()) return emptyList()

        val boundsToProbe = if (warningBounds.size > 1) {
            warningBounds + listOf(mergeBounds(warningBounds))
        } else {
            warningBounds
        }

        return boundsToProbe
            .flatMap { bounds -> waypointSetsAround(bounds, origin, destination) }
            .distinctBy { waypointSet -> waypointSet.joinToString(separator = "|") { it.key() } }
            .take(MAX_WAYPOINT_SETS)
    }

    private fun waypointSetsAround(
        bounds: RouteWarningBounds,
        origin: Position,
        destination: Position,
    ): List<List<Position>> {
        val movingMostlyEastWest = abs(destination.longitude - origin.longitude) >=
                abs(destination.latitude - origin.latitude)
        val originWestOfDestination = origin.longitude <= destination.longitude
        val originSouthOfDestination = origin.latitude <= destination.latitude

        return marginMeters.flatMap { marginMeters ->
            val centerLatitude = (bounds.minLatitude + bounds.maxLatitude) / 2.0
            val latitudePadding = max(
                metersToLatitudeDegrees(marginMeters),
                (bounds.maxLatitude - bounds.minLatitude) * 0.25,
            )
            val longitudePadding = max(
                metersToLongitudeDegrees(marginMeters, centerLatitude),
                (bounds.maxLongitude - bounds.minLongitude) * 0.25,
            )

            val west = bounds.minLongitude - longitudePadding
            val east = bounds.maxLongitude + longitudePadding
            val south = bounds.minLatitude - latitudePadding
            val north = bounds.maxLatitude + latitudePadding
            val centerLongitude = (bounds.minLongitude + bounds.maxLongitude) / 2.0

            val northPoint = Position(longitude = centerLongitude, latitude = north)
            val southPoint = Position(longitude = centerLongitude, latitude = south)
            val eastPoint = Position(longitude = east, latitude = centerLatitude)
            val westPoint = Position(longitude = west, latitude = centerLatitude)
            val northwest = Position(longitude = west, latitude = north)
            val northeast = Position(longitude = east, latitude = north)
            val southwest = Position(longitude = west, latitude = south)
            val southeast = Position(longitude = east, latitude = south)

            val topPair = if (originWestOfDestination) {
                listOf(northwest, northeast)
            } else {
                listOf(northeast, northwest)
            }
            val bottomPair = if (originWestOfDestination) {
                listOf(southwest, southeast)
            } else {
                listOf(southeast, southwest)
            }
            val leftPair = if (originSouthOfDestination) {
                listOf(southwest, northwest)
            } else {
                listOf(northwest, southwest)
            }
            val rightPair = if (originSouthOfDestination) {
                listOf(southeast, northeast)
            } else {
                listOf(northeast, southeast)
            }

            if (movingMostlyEastWest) {
                listOf(
                    listOf(northPoint),
                    listOf(southPoint),
                    topPair,
                    bottomPair,
                    listOf(northwest),
                    listOf(northeast),
                    listOf(southwest),
                    listOf(southeast),
                    listOf(eastPoint),
                    listOf(westPoint),
                    rightPair,
                    leftPair,
                )
            } else {
                listOf(
                    listOf(eastPoint),
                    listOf(westPoint),
                    rightPair,
                    leftPair,
                    listOf(northeast),
                    listOf(southeast),
                    listOf(northwest),
                    listOf(southwest),
                    listOf(northPoint),
                    listOf(southPoint),
                    topPair,
                    bottomPair,
                )
            }
        }
    }

    private fun mergeBounds(bounds: List<RouteWarningBounds>): RouteWarningBounds {
        return RouteWarningBounds(
            minLongitude = bounds.minOf { it.minLongitude },
            minLatitude = bounds.minOf { it.minLatitude },
            maxLongitude = bounds.maxOf { it.maxLongitude },
            maxLatitude = bounds.maxOf { it.maxLatitude },
        )
    }

    private fun metersToLatitudeDegrees(meters: Double): Double = meters / METERS_PER_DEGREE_LATITUDE

    private fun metersToLongitudeDegrees(meters: Double, latitude: Double): Double {
        val latitudeScale = cos(Math.toRadians(latitude)).coerceAtLeast(0.2)
        return meters / (METERS_PER_DEGREE_LATITUDE * latitudeScale)
    }

    private fun RouteWarningBounds.key(): String {
        return listOf(minLongitude, minLatitude, maxLongitude, maxLatitude)
            .joinToString(separator = ",") { String.format(Locale.US, "%.4f", it) }
    }

    private fun Position.key(): String {
        return String.format(Locale.US, "%.4f,%.4f", longitude, latitude)
    }

    private const val METERS_PER_DEGREE_LATITUDE = 111_320.0
    private const val MAX_WARNING_BOUNDS = 3
    private const val MAX_WAYPOINT_SETS = 32
}
