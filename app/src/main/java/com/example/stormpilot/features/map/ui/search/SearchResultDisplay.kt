package com.example.stormpilot.features.map.ui.search

import com.example.stormpilot.features.shared.data.search.PhotonFeature
import java.util.Locale

internal fun PhotonFeature.displayAddress(): String {
    address?.takeIf { it.isNotBlank() }?.let { return it }

    val streetLine = listOfNotNull(houseNumber, street)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .takeIf { it.isNotBlank() }
    val localityLine = listOfNotNull(city, state, postcode)
        .filter { it.isNotBlank() }
        .joinToString(", ")
        .takeIf { it.isNotBlank() }

    return listOfNotNull(streetLine, localityLine, country?.takeIf { it.isNotBlank() })
        .joinToString(" • ")
        .takeIf { it.isNotBlank() }
        ?: String.format(Locale.US, "%.4f, %.4f", geometry.latitude, geometry.longitude)
}

internal fun PhotonFeature.displayCategory(): String? =
    listOfNotNull(category, type)
        .filter { it.isNotBlank() }
        .joinToString(" • ")
        .takeIf { it.isNotBlank() }
