package com.example.stormpilot.features.shared.data.routing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Position

private val json = Json { ignoreUnknownKeys = true }

object RoutingParsing {
    fun parseOsrmRoute(payload: String): RouteResult {
        return parseOsrmRoutes(payload).first()
    }

    fun parseOsrmRoutes(payload: String): List<RouteResult> {
        val root = json.parseToJsonElement(payload).jsonObject
        val routes = root["routes"]?.jsonArray.orEmpty()
        require(routes.isNotEmpty()) { "No route returned" }

        return routes.map { routeElement ->
            parseOsrmRouteObject(routeElement.jsonObject)
        }
    }

    private fun parseOsrmRouteObject(route: kotlinx.serialization.json.JsonObject): RouteResult {
        val geometry = route["geometry"]?.jsonObject
            ?.get("coordinates")?.jsonArray.orEmpty()
            .map { coordinate ->
                val values = coordinate.jsonArray
                Position(
                    longitude = values[0].jsonPrimitive.double,
                    latitude = values[1].jsonPrimitive.double,
                )
            }

        val legs = route["legs"]?.jsonArray.orEmpty()
        val steps = legs.flatMap { leg ->
            leg.jsonObject["steps"]?.jsonArray.orEmpty().map { step ->
                val stepObj = step.jsonObject
                val maneuverObj = stepObj["maneuver"]?.jsonObject

                val maneuverLocation = maneuverObj
                    ?.get("location")?.jsonArray
                    ?.let { raw ->
                        Position(
                            longitude = raw[0].jsonPrimitive.double,
                            latitude = raw[1].jsonPrimitive.double,
                        )
                    }

                val maneuver = maneuverObj?.let { m ->
                    val loc = m["location"]?.jsonArray
                    Maneuver(
                        type = m["type"]?.jsonPrimitive?.content ?: "unknown",
                        modifier = m["modifier"]?.jsonPrimitive?.contentOrNull,
                        exit = m["exit"]?.jsonPrimitive?.intOrNull,
                        bearingBefore = m["bearing_before"]?.jsonPrimitive?.int ?: 0,
                        bearingAfter = m["bearing_after"]?.jsonPrimitive?.int ?: 0,
                        location = Position(
                            longitude = loc?.get(0)?.jsonPrimitive?.double ?: 0.0,
                            latitude = loc?.get(1)?.jsonPrimitive?.double ?: 0.0,
                        ),
                    )
                }

                RouteStep(
                    instruction = maneuverObj
                        ?.get("instruction")
                        ?.jsonPrimitive
                        ?.contentOrNull
                        ?: buildInstruction(maneuver, stepObj["name"]?.jsonPrimitive?.content),
                    distanceMeters = stepObj["distance"]?.jsonPrimitive?.double ?: 0.0,
                    durationSeconds = stepObj["duration"]?.jsonPrimitive?.double ?: 0.0,
                    maneuverLocation = maneuverLocation,
                    maneuver = maneuver,
                )
            }
        }

        return RouteResult(
            polyline = geometry,
            distanceMeters = route["distance"]?.jsonPrimitive?.double ?: 0.0,
            durationSeconds = route["duration"]?.jsonPrimitive?.double ?: 0.0,
            steps = steps,
        )
    }

    private fun buildInstruction(maneuver: Maneuver?, streetName: String?): String {
        if (maneuver == null) return streetName.orEmpty()
        val name = streetName?.takeIf { it.isNotBlank() }?.let { " onto $it" }.orEmpty()
        return when (maneuver.type) {
            "depart"      -> "Head ${maneuver.modifier ?: ""}$name".trim()
            "arrive"      -> "You have arrived at your destination"
            "turn"        -> "Turn ${maneuver.modifier ?: ""}$name"
            "new name"    -> "Continue$name"
            "merge"       -> "Merge ${maneuver.modifier ?: ""}$name"
            "on ramp"     -> "Take the ramp on the ${maneuver.modifier ?: ""}$name"
            "off ramp"    -> "Take the exit on the ${maneuver.modifier ?: ""}$name"
            "fork"        -> "Keep ${maneuver.modifier ?: ""} at the fork$name"
            "end of road" -> "Turn ${maneuver.modifier ?: ""} at the end of the road$name"
            "roundabout",
            "rotary"      -> "Enter the roundabout and take exit ${maneuver.exit ?: ""}$name"
            "continue"    -> "Continue ${maneuver.modifier ?: "straight"}$name"
            "use lane"    -> "Use the ${maneuver.modifier ?: ""} lane$name"
            else          -> "${maneuver.type} ${maneuver.modifier ?: ""}$name".trim()
        }
    }

    fun toGeoJsonLineString(points: List<Position>): String {
        val coordinates = points.joinToString(separator = ",") {
            "[${it.longitude},${it.latitude}]"
        }
        return """
            {
              "type":"FeatureCollection",
              "features":[{
                "type":"Feature",
                "geometry":{
                  "type":"LineString",
                  "coordinates":[${coordinates}]
                },
                "properties":{}
              }]
            }
        """.trimIndent()
    }
}
