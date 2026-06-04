package com.example.stormpilot.features.alerts.data

data class NwsAlert(
    val id: String,
    val event: String,
    val headline: String,
    val description: String,
    val instruction: String?,
    val severity: String,
    val urgency: String,
    val effective: String?,
    val onset: String?,
    val expires: String?,
    val areaDescription: String?,
    val affectedZones: List<String> = emptyList(),
    val senderName: String,
)

enum class AlertType {
    TORNADO_WARNING,
    TORNADO_WATCH,
    FLASH_FLOOD_WARNING,
    FLASH_FLOOD_WATCH,
    SEVERE_THUNDERSTORM_WARNING,
    SEVERE_THUNDERSTORM_WATCH,
    OTHER,
}

fun NwsAlert.alertType(): AlertType = when {
    event.contains("Tornado Warning", ignoreCase = true) -> AlertType.TORNADO_WARNING
    event.contains("Tornado Watch", ignoreCase = true) -> AlertType.TORNADO_WATCH
    event.contains("Flash Flood Warning", ignoreCase = true) -> AlertType.FLASH_FLOOD_WARNING
    event.contains("Flash Flood Watch", ignoreCase = true) -> AlertType.FLASH_FLOOD_WATCH
    event.contains("Severe Thunderstorm Warning", ignoreCase = true) -> AlertType.SEVERE_THUNDERSTORM_WARNING
    event.contains("Severe Thunderstorm Watch", ignoreCase = true) -> AlertType.SEVERE_THUNDERSTORM_WATCH
    else -> AlertType.OTHER
}

fun List<NwsAlert>.bestMatchForEvent(eventType: String): NwsAlert? {
    val normalizedEvent = eventType.trim()
    if (normalizedEvent.isEmpty()) return null

    return firstOrNull { it.event.equals(normalizedEvent, ignoreCase = true) }
        ?: firstOrNull { it.event.contains(normalizedEvent, ignoreCase = true) }
        ?: firstOrNull { normalizedEvent.contains(it.event, ignoreCase = true) }
}
