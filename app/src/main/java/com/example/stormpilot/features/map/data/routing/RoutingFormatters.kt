package com.example.stormpilot.features.map.data.routing

import kotlin.math.roundToInt

fun formatDistance(distanceMeters: Double): String {
    val miles = distanceMeters / 1609.344
    return if (miles >= 0.2) {
        "${"%.1f".format(miles)} mi"
    } else {
        "${(distanceMeters / 1.0).roundToInt()} m"
    }
}

fun formatDuration(durationSeconds: Double): String {
    val minutes = (durationSeconds / 60.0).roundToInt()
    return if (minutes < 60) {
        "$minutes min"
    } else {
        val hours = minutes / 60
        val remaining = minutes % 60
        if (remaining == 0) "$hours hr" else "$hours hr $remaining min"
    }
}
