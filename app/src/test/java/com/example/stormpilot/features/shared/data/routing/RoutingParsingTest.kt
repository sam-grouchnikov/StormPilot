package com.example.stormpilot.features.shared.data.routing

import com.example.stormpilot.testing.StormPilotUnitTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.maplibre.spatialk.geojson.Position

class RoutingParsingTest : StormPilotUnitTest() {
    @Test
    fun parseOsrmRoutes_parsesGeometryTotalsStepsAndManeuvers() {
        val routes = RoutingParsing.parseOsrmRoutes(osrmPayloadWithSteps())

        assertEquals(2, routes.size)
        val route = routes.first()
        assertEquals(1_500.5, route.distanceMeters, 0.0)
        assertEquals(620.2, route.durationSeconds, 0.0)
        assertEquals(listOf(Position(-97.0, 35.0), Position(-96.5, 35.2)), route.polyline)
        assertEquals(15, route.steps.size)

        val explicitStep = route.steps[0]
        assertEquals("Use the custom instruction", explicitStep.instruction)
        assertEquals(100.0, explicitStep.distanceMeters, 0.0)
        assertEquals(60.0, explicitStep.durationSeconds, 0.0)
        assertEquals(Position(-97.0, 35.0), explicitStep.maneuverLocation)
        assertNotNull(explicitStep.maneuver)
        assertEquals("depart", explicitStep.maneuver?.type)
        assertEquals("north", explicitStep.maneuver?.modifier)
        assertEquals(10, explicitStep.maneuver?.bearingBefore)
        assertEquals(20, explicitStep.maneuver?.bearingAfter)

        assertEquals(
            listOf(
                "Use the custom instruction",
                "You have arrived at your destination",
                "Turn right onto Oak",
                "Continue onto Pine",
                "Merge left onto Ramp",
                "Take the ramp on the right onto I-70",
                "Take the exit on the right onto Exit 10",
                "Keep left at the fork onto Split",
                "Turn right at the end of the road onto Dead End",
                "Enter the roundabout and take exit 2 onto Circle",
                "Enter the roundabout and take exit 3 onto Rotary",
                "Continue straight onto Elm",
                "Use the left lane onto Lane",
                "notification slight right onto Alert",
                "Nameless",
            ),
            route.steps.map { it.instruction },
        )
        assertNull(route.steps.last().maneuver)
        assertEquals(99.0, routes[1].distanceMeters, 0.0)
    }

    @Test
    fun parseOsrmRoute_returnsFirstParsedRoute() {
        val route = RoutingParsing.parseOsrmRoute(osrmPayloadWithSteps())

        assertEquals(1_500.5, route.distanceMeters, 0.0)
        assertEquals(Position(-97.0, 35.0), route.polyline.first())
    }

    @Test
    fun parseOsrmRoutes_throwsWhenPayloadHasNoRoutes() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            RoutingParsing.parseOsrmRoutes("""{"routes":[]}""")
        }

        assertEquals("No route returned", error.message)
    }

    @Test
    fun parseOsrmRoutes_defaultsMissingOptionalFields() {
        val payload = """
            {
              "routes": [{
                "geometry": { "coordinates": [] },
                "legs": [{ "steps": [{ "name": "Only Name" }] }]
              }]
            }
        """.trimIndent()

        val route = RoutingParsing.parseOsrmRoute(payload)

        assertEquals(emptyList<Position>(), route.polyline)
        assertEquals(0.0, route.distanceMeters, 0.0)
        assertEquals(0.0, route.durationSeconds, 0.0)
        assertEquals("Only Name", route.steps.single().instruction)
        assertEquals(0.0, route.steps.single().distanceMeters, 0.0)
        assertNull(route.steps.single().maneuverLocation)
        assertNull(route.steps.single().maneuver)
    }

    @Test
    fun toGeoJsonLineString_buildsFeatureCollectionLineString() {
        val geoJson = RoutingParsing.toGeoJsonLineString(
            listOf(Position(-97.0, 35.0), Position(-96.5, 35.2)),
        )
        val root = Json.parseToJsonElement(geoJson).jsonObject
        val feature = root["features"]!!.jsonArray.first().jsonObject
        val geometry = feature["geometry"]!!.jsonObject

        assertEquals("FeatureCollection", root["type"]!!.jsonPrimitive.content)
        assertEquals("Feature", feature["type"]!!.jsonPrimitive.content)
        assertEquals("LineString", geometry["type"]!!.jsonPrimitive.content)
        assertEquals("[[-97.0,35.0],[-96.5,35.2]]", geometry["coordinates"].toString())
    }

    private fun osrmPayloadWithSteps(): String {
        return """
            {
              "routes": [
                {
                  "distance": 1500.5,
                  "duration": 620.2,
                  "geometry": {
                    "coordinates": [[-97.0,35.0],[-96.5,35.2]]
                  },
                  "legs": [{
                    "steps": [
                      ${step("depart", "north", "Main", instruction = "Use the custom instruction", bearingBefore = 10, bearingAfter = 20)},
                      ${step("arrive", null, "Finish")},
                      ${step("turn", "right", "Oak")},
                      ${step("new name", null, "Pine")},
                      ${step("merge", "left", "Ramp")},
                      ${step("on ramp", "right", "I-70")},
                      ${step("off ramp", "right", "Exit 10")},
                      ${step("fork", "left", "Split")},
                      ${step("end of road", "right", "Dead End")},
                      ${step("roundabout", null, "Circle", exit = 2)},
                      ${step("rotary", null, "Rotary", exit = 3)},
                      ${step("continue", null, "Elm")},
                      ${step("use lane", "left", "Lane")},
                      ${step("notification", "slight right", "Alert")},
                      { "name": "Nameless", "distance": 15.0, "duration": 8.0 }
                    ]
                  }]
                },
                {
                  "distance": 99.0,
                  "duration": 10.0,
                  "geometry": { "coordinates": [[-1.0,1.0],[-2.0,2.0]] },
                  "legs": []
                }
              ]
            }
        """.trimIndent()
    }

    private fun step(
        type: String,
        modifier: String?,
        name: String,
        instruction: String? = null,
        exit: Int? = null,
        bearingBefore: Int = 0,
        bearingAfter: Int = 0,
    ): String {
        val modifierJson = modifier?.let { """"modifier":"$it",""" }.orEmpty()
        val instructionJson = instruction?.let { """"instruction":"$it",""" }.orEmpty()
        val exitJson = exit?.let { """"exit":$it,""" }.orEmpty()
        return """
            {
              "name": "$name",
              "distance": 100.0,
              "duration": 60.0,
              "maneuver": {
                $instructionJson
                "type": "$type",
                $modifierJson
                $exitJson
                "bearing_before": $bearingBefore,
                "bearing_after": $bearingAfter,
                "location": [-97.0, 35.0]
              }
            }
        """.trimIndent()
    }
}
