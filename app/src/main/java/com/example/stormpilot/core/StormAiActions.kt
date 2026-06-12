package com.example.stormpilot.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

private typealias JSONObject = JsonObject

data class StormAiResponse(
    val reply: String,
    val actions: List<StormAiAction>,
)

sealed interface StormAiAction {
    val id: String?
    val requiresConfirmation: Boolean

    data class AnswerOnly(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val reply: String,
    ) : StormAiAction

    data class AskClarification(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val question: String,
        val missingField: String,
        val choices: List<String>,
    ) : StormAiAction

    data class SetDestination(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val label: String,
        val latitude: Double,
        val longitude: Double,
        val address: String?,
        val placeId: String?,
        val confidence: Double?,
    ) : StormAiAction

    data class ShowAlertsOverlay(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val focusLocation: AssistantPoint?,
        val focusAlertId: String?,
    ) : StormAiAction

    data class ShowAlertDetail(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val event: String,
        val latitude: Double,
        val longitude: Double,
        val alertId: String?,
    ) : StormAiAction

    data class PreviewRoute(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val label: String,
        val destination: AssistantDestination,
        val routeGeoJson: String?,
        val waypoints: List<AssistantPoint>,
        val distanceMeters: Double?,
        val durationSeconds: Double?,
        val warningCount: Int?,
        val warningError: String?,
    ) : StormAiAction

    data class RequestConfirmation(
        override val id: String?,
        override val requiresConfirmation: Boolean,
        val message: String,
        val pendingActions: List<StormAiAction>,
        val confirmLabel: String?,
        val cancelLabel: String?,
        val riskSummary: String?,
    ) : StormAiAction
}

data class AssistantPoint(
    val latitude: Double,
    val longitude: Double,
)

data class AssistantDestination(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

fun parseStormAiResponse(rawJson: String): StormAiResponse {
    val root = Json.parseToJsonElement(rawJson).jsonObject
    val reply = root.requiredString("reply")
    val actions = root.optionalArray("actions")
        ?.mapObjects(::parseStormAiAction)
        ?: emptyList()

    return StormAiResponse(reply = reply, actions = actions)
}

private fun parseStormAiAction(json: JSONObject): StormAiAction {
    val type = json.requiredString("type")
    val id = json.optNullableString("id")
    val requiresConfirmation = json.optNullableBoolean("requiresConfirmation") ?: false

    return when (type) {
        "ANSWER_ONLY" -> StormAiAction.AnswerOnly(
            id = id,
            requiresConfirmation = requiresConfirmation,
            reply = json.requiredString("reply"),
        )

        "ASK_CLARIFICATION" -> StormAiAction.AskClarification(
            id = id,
            requiresConfirmation = requiresConfirmation,
            question = json.requiredString("question"),
            missingField = json.requiredString("missingField"),
            choices = json.optionalArray("choices").orEmpty().mapStrings(),
        )

        "SET_DESTINATION" -> StormAiAction.SetDestination(
            id = id,
            requiresConfirmation = requiresConfirmation,
            label = json.requiredString("label"),
            latitude = json.requiredDouble("latitude"),
            longitude = json.requiredDouble("longitude"),
            address = json.optNullableString("address")?.takeIf { it.isNotBlank() },
            placeId = json.optNullableString("placeId")?.takeIf { it.isNotBlank() },
            confidence = json.optNullableDouble("confidence"),
        )

        "SHOW_ALERTS_OVERLAY" -> StormAiAction.ShowAlertsOverlay(
            id = id,
            requiresConfirmation = requiresConfirmation,
            focusLocation = json.optionalObject("focusLocation")?.toAssistantPoint(),
            focusAlertId = json.optNullableString("focusAlertId")?.takeIf { it.isNotBlank() },
        )

        "SHOW_ALERT_DETAIL" -> StormAiAction.ShowAlertDetail(
            id = id,
            requiresConfirmation = requiresConfirmation,
            event = json.requiredString("event"),
            latitude = json.requiredDouble("latitude"),
            longitude = json.requiredDouble("longitude"),
            alertId = json.optNullableString("alertId")?.takeIf { it.isNotBlank() },
        )

        "PREVIEW_ROUTE" -> StormAiAction.PreviewRoute(
            id = id,
            requiresConfirmation = requiresConfirmation,
            label = json.requiredString("label"),
            destination = json.requiredObject("destination").toAssistantDestination(),
            routeGeoJson = json.optNullableString("routeGeoJson")?.takeIf { it.isNotBlank() },
            waypoints = json.optionalArray("waypoints").orEmpty().mapObjects { it.toAssistantPoint() },
            distanceMeters = json.optNullableDouble("distanceMeters"),
            durationSeconds = json.optNullableDouble("durationSeconds"),
            warningCount = json.optNullableInt("warningCount"),
            warningError = json.optNullableString("warningError")?.takeIf { it.isNotBlank() },
        )

        "REQUEST_CONFIRMATION" -> StormAiAction.RequestConfirmation(
            id = id,
            requiresConfirmation = requiresConfirmation,
            message = json.requiredString("message"),
            pendingActions = json.optionalArray("pendingActions")
                .orEmpty()
                .mapObjects(::parseStormAiAction),
            confirmLabel = json.optNullableString("confirmLabel")?.takeIf { it.isNotBlank() },
            cancelLabel = json.optNullableString("cancelLabel")?.takeIf { it.isNotBlank() },
            riskSummary = json.optNullableString("riskSummary")?.takeIf { it.isNotBlank() },
        )

        else -> throw IllegalArgumentException("Unsupported StormAI action type: $type")
    }
}

private fun JSONObject.toAssistantPoint(): AssistantPoint =
    AssistantPoint(
        latitude = requiredDouble("latitude"),
        longitude = requiredDouble("longitude"),
    )

private fun JSONObject.toAssistantDestination(): AssistantDestination =
    AssistantDestination(
        label = requiredString("label"),
        latitude = requiredDouble("latitude"),
        longitude = requiredDouble("longitude"),
    )

private fun JSONObject.requiredString(name: String): String =
    optNullableString(name)
        ?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("StormAI response is missing required field '$name'.")

private fun JSONObject.requiredDouble(name: String): Double =
    optNullableDouble(name)
        ?: throw IllegalArgumentException("StormAI response is missing required numeric field '$name'.")

private fun JSONObject.optNullableString(name: String): String? =
    this[name]?.jsonPrimitiveOrNull()?.contentOrNull

private fun JSONObject.optNullableDouble(name: String): Double? =
    this[name]?.jsonPrimitiveOrNull()?.doubleOrNull

private fun JSONObject.optNullableInt(name: String): Int? =
    this[name]?.jsonPrimitiveOrNull()?.intOrNull

private fun JSONObject.optNullableBoolean(name: String): Boolean? =
    this[name]?.jsonPrimitiveOrNull()?.contentOrNull?.toBooleanStrictOrNull()

private fun JSONObject.optionalObject(name: String): JSONObject? =
    this[name] as? JSONObject

private fun JSONObject.requiredObject(name: String): JSONObject =
    optionalObject(name)
        ?: throw IllegalArgumentException("StormAI response is missing required object '$name'.")

private fun JSONObject.optionalArray(name: String): JsonArray? =
    this[name] as? JsonArray

private fun JsonArray?.orEmpty(): JsonArray =
    this ?: JsonArray(emptyList())

private fun <T> JsonArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    map { element ->
        transform(element.jsonObject)
    }

private fun JsonArray.mapStrings(): List<String> =
    mapNotNull { element ->
        element.jsonPrimitiveOrNull()?.contentOrNull?.takeIf { it.isNotBlank() }
    }

private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? =
    this as? JsonPrimitive
