package com.example.stormpilot.pages.subnav.maps.routing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Position

private val json = Json { ignoreUnknownKeys = true }

object RoutingParsing {
    fun parseOsrmRoute(payload: String): RouteResult {
        val root = json.parseToJsonElement(payload).jsonObject
        val routes = root["routes"]?.jsonArray.orEmpty()
        require(routes.isNotEmpty()) { "No route returned" }
        val route = routes.first().jsonObject

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
                RouteStep(
                    instruction = stepObj["maneuver"]?.jsonObject?.get("instruction")?.jsonPrimitive?.content
                        ?: stepObj["name"]?.jsonPrimitive?.content.orEmpty(),
                    distanceMeters = stepObj["distance"]?.jsonPrimitive?.double ?: 0.0,
                    durationSeconds = stepObj["duration"]?.jsonPrimitive?.double ?: 0.0,
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
