package com.example.stormpilot.features.map.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.stormpilot.ui.theme.extendedColors

@Composable
internal fun SearchShortcutRow(
    containerColor: Color,
    onOpenStormAiChat: () -> Unit,
    onPlaceSearch: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SearchShortcutPill(
            label = "Ask StormAI",
            icon = Icons.Rounded.AutoAwesome,
            containerColor = containerColor,
            isStormAi = true,
            onClick = onOpenStormAiChat,
        )
        SearchShortcutPill("Gas", Icons.Rounded.LocalGasStation, containerColor) { onPlaceSearch("gas") }
        SearchShortcutPill("Hotel", Icons.Rounded.Hotel, containerColor) { onPlaceSearch("hotel") }
        SearchShortcutPill("Food", Icons.Rounded.Restaurant, containerColor) { onPlaceSearch("restaurant") }
    }
}

@Composable
private fun SearchShortcutPill(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    isStormAi: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.extendedColors
    val border = if (isStormAi) {
        val aiBlue = colors.search.stormAiGradientStart.takeUnless { it == Color.White }
            ?: colors.stormAi.glowStop1
        BorderStroke(1.5.dp, aiBlue)
    } else {
        null
    }
    val iconModifier = if (isStormAi) {
        Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val gradient = Brush.linearGradient(
                    colors = listOf(colors.search.stormAiGradientStart, colors.search.stormAiGradientEnd),
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(gradient, blendMode = BlendMode.SrcIn)
                }
            }
    } else {
        Modifier
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = border,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp).then(iconModifier))
            Text(text = label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}
