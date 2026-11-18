package com.example.stormpilot.features.dashboard.ui.alerts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.R
import com.example.stormpilot.features.shared.data.weather.CurrentWeather
import com.example.stormpilot.features.shared.data.weather.StormSpec

@Composable
fun StormSpecsPanel(
    stormSpecs: List<StormSpec>,
    current: CurrentWeather?,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    val cape = stormSpecs.firstOrNull { it.label.trim().equals("CAPE", ignoreCase = true) }?.value
    val srh = stormSpecs.firstOrNull {
        it.label.trim().equals("SRH", ignoreCase = true) ||
            it.label.trim().replace(" ", "").replace("–", "-").equals("0-1kmSRH", ignoreCase = true)
    }?.value?.replace("m2/s2", "m²/s²")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StormSpecPill(
                label = "Temperature",
                value = current?.temperature?.let { "$it° F" },
                icon = rememberVectorPainter(Icons.Outlined.DeviceThermostat),
                containerColor = containerColor,
                modifier = Modifier.weight(1f),
            )
            StormSpecPill(
                label = "Cape",
                value = cape,
                icon = rememberVectorPainter(Icons.Outlined.Thermostat),
                containerColor = containerColor,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StormSpecPill(
                label = "0-1km SRH",
                value = srh,
                icon = rememberVectorPainter(Icons.Outlined.Air),
                containerColor = containerColor,
                modifier = Modifier.weight(1f),
            )
            StormSpecPill(
                label = "Dew Point",
                value = current?.dewPoint?.let { "$it° F" },
                icon = painterResource(R.drawable.dew_point_24dp_32fc96_fill0_wght400_grad0_opsz24),
                containerColor = containerColor,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StormSpecPill(
    label: String,
    value: String?,
    icon: Painter,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(40.dp), color = containerColor) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )

            Column {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    value?.takeIf { it.isNotBlank() } ?: "—",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}
