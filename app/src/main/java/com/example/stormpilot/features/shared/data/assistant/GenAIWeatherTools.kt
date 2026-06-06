package com.example.stormpilot.features.shared.data.assistant

import com.example.stormpilot.features.shared.data.api.StormPilotApi
import com.example.stormpilot.features.shared.data.location.LocationRepository
import com.google.firebase.ai.type.AutoFunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.JsonSchema
import com.google.firebase.ai.type.Tool
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class GenAIWeatherTools(
    private val locationRepository: LocationRepository,
    private val api: StormPilotApi,
) {
    fun tool(): Tool = Tool.functionDeclarations(
        functionDeclarations = null,
        autoFunctionDeclarations = listOf(
            AutoFunctionDeclaration.create(
                functionName = "get_user_location",
                description = "Get the user's current device location as latitude and longitude when location permission is granted. Use this when the user says here, near me, my location, or asks for local weather without naming a place.",
                inputSchema = emptyInputSchema,
            ) { _: JsonObject ->
                safeFunctionResponse {
                    getUserLocation()
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "resolve_location",
                description = "Resolve a user-provided place name into latitude and longitude. Use this before weather tools when the user asks about a specific location.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantResolveLocation(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_current_weather",
                description = "Get latest observed weather conditions for a specific United States location using weather.gov observation stations.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantCurrentWeather(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_weather_forecast",
                description = "Get an NWS daily forecast for a specific United States location. Include location in the answer.",
                inputSchema = forecastInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantForecast(
                        location = args.requiredString("location"),
                        days = args.optionalInt("days") ?: 3,
                    )
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_hourly_weather",
                description = "Get NWS hourly forecast timing for a specific United States location, including temperature, wind, precipitation chance, and short conditions.",
                inputSchema = hourlyInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantHourly(
                        location = args.requiredString("location"),
                        hours = args.optionalInt("hours") ?: 12,
                    )
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_active_weather_alerts",
                description = "Get active NWS watches, warnings, and advisories for a specific United States location.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantAlerts(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_spc_convective_outlook",
                description = "Get SPC categorical convective outlook risk for days 1 through 5 at a specific United States location.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantSpcOutlook(args.requiredString("location"))
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_storm_environment",
                description = "Get storm-relevant forecast ingredients from Open-Meteo for a location: CAPE, lifted index, CIN, dew point, humidity, gusts, low-level shear, and estimated storm-relative helicity.",
                inputSchema = hourlyInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantStormEnvironment(
                        location = args.requiredString("location"),
                        hours = args.optionalInt("hours") ?: 12,
                    )
                }
            },
            AutoFunctionDeclaration.create(
                functionName = "get_weather_outlook",
                description = "Get a practical storm-chasing weather outlook for a specific United States location, combining NWS forecast periods and active alerts.",
                inputSchema = locationInputSchema,
            ) { args: JsonObject ->
                safeFunctionResponse {
                    api.assistantOutlook(args.requiredString("location"))
                }
            },
        ),
    )

    private suspend fun getUserLocation(): JsonObject {
        val location = locationRepository.location.value ?: locationRepository.fetchCurrentLocation()
        return if (location == null) {
            buildJsonObject {
                put("error", "Current device location is unavailable.")
                put("hint", "Ask the user to grant location permission or provide a city, address, landmark, or latitude/longitude pair.")
            }
        } else {
            buildJsonObject {
                put("location", buildJsonObject {
                    put("name", "Current location")
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                })
                put("weatherLocationQuery", "%.5f, %.5f".format(Locale.US, location.latitude, location.longitude))
                put("bearingDegrees", location.bearing.toDouble())
                put("speedMetersPerSecond", location.speed.toDouble())
            }
        }
    }

    private suspend fun safeFunctionResponse(block: suspend () -> JsonObject): FunctionResponsePart =
        try {
            FunctionResponsePart.from(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FunctionResponsePart.from(
                buildJsonObject {
                    put("error", e.localizedMessage ?: "Weather data could not be loaded.")
                    put("hint", "Ask the user to confirm the location if it may be outside weather.gov coverage or was misspelled.")
                },
            )
        }

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Missing required argument: $name")

    private fun JsonObject.optionalInt(name: String): Int? =
        this[name]?.jsonPrimitive?.intOrNull

    private companion object {
        val emptyInputSchema = JsonSchema.obj(
            properties = emptyMap(),
            description = "No arguments",
        )

        val locationInputSchema = JsonSchema.obj(
            properties = mapOf(
                "location" to JsonSchema.string(
                    description = "A city, address, landmark, or latitude/longitude pair supplied by the user.",
                ),
            ),
            description = "Location request",
        )

        val forecastInputSchema = JsonSchema.obj(
            properties = mapOf(
                "location" to JsonSchema.string(
                    description = "A city, address, landmark, or latitude/longitude pair supplied by the user.",
                ),
                "days" to JsonSchema.integer(
                    description = "Number of forecast days to retrieve, from 1 to 7.",
                    minimum = 1.0,
                    maximum = 7.0,
                ),
            ),
            optionalProperties = listOf("days"),
            description = "Forecast request",
        )

        val hourlyInputSchema = JsonSchema.obj(
            properties = mapOf(
                "location" to JsonSchema.string(
                    description = "A city, address, landmark, or latitude/longitude pair supplied by the user.",
                ),
                "hours" to JsonSchema.integer(
                    description = "Number of hourly forecast periods to retrieve, from 1 to 24.",
                    minimum = 1.0,
                    maximum = 24.0,
                ),
            ),
            optionalProperties = listOf("hours"),
            description = "Hourly weather request",
        )
    }
}
