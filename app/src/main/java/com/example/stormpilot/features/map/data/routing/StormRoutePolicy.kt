package com.example.stormpilot.features.map.data.routing

import org.maplibre.spatialk.geojson.Position

data class StormAwareRouteSelection(
    val route: RouteResult,
    val warningCount: Int,
    val warningMessage: String?,
)

object StormRoutePolicy {
    const val DESTINATION_IN_WARNING_MESSAGE =
        "Destination is inside a storm warning polygon. This route enters the warning area to reach it."
    const val NO_STORM_FREE_ROUTE_MESSAGE =
        "No storm-free road route was found. This route passes through a storm warning polygon."

    fun selectRoute(
        candidates: List<RouteResult>,
        alertsGeoJson: String,
        destination: Position,
    ): StormAwareRouteSelection {
        require(candidates.isNotEmpty()) { "No route returned" }

        val scoredRoutes = candidates.map { route ->
            val analysis = RouteWarningCounter.analyzeWarnings(
                alertsGeoJson = alertsGeoJson,
                routePolyline = route.polyline,
                destination = destination,
            )
            ScoredStormRoute(route = route, analysis = analysis)
        }
        val destinationInsideWarning = scoredRoutes.any { it.analysis.destinationInsideWarning }

        val selectedRoute = if (destinationInsideWarning) {
            scoredRoutes.minWith(warnedRouteComparator)
        } else {
            scoredRoutes
                .filter { it.analysis.warningCount == 0 }
                .minWithOrNull(clearRouteComparator)
                ?: scoredRoutes.minWith(warnedRouteComparator)
        }

        val warningMessage = when {
            selectedRoute.analysis.destinationInsideWarning -> DESTINATION_IN_WARNING_MESSAGE
            selectedRoute.analysis.warningCount > 0 -> NO_STORM_FREE_ROUTE_MESSAGE
            else -> null
        }

        return StormAwareRouteSelection(
            route = selectedRoute.route,
            warningCount = selectedRoute.analysis.warningCount,
            warningMessage = warningMessage,
        )
    }

    private val clearRouteComparator =
        compareBy<ScoredStormRoute> { it.route.durationSeconds }
            .thenBy { it.route.distanceMeters }

    private val warnedRouteComparator =
        compareBy<ScoredStormRoute> { it.analysis.warningCount }
            .thenBy { it.route.durationSeconds }
            .thenBy { it.route.distanceMeters }

    private data class ScoredStormRoute(
        val route: RouteResult,
        val analysis: RouteWarningAnalysis,
    )
}
