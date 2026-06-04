package com.example.stormpilot.features.dashboard.ui.alerts.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.weather.data.StormSpec
import com.example.stormpilot.ui.theme.ExtendedColors

@Composable
fun StormSpecsPanel(stormSpecs: List<StormSpec>) {
    val colors = ExtendedColors()

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)),
                    ) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Thunderstorm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(23.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(start = 11.dp)) {
                        Text(
                            text = "Storm Environment",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                        )
                        Text(
                            text = "Convective ingredients near you",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (stormSpecs.isEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearWavyProgressIndicator(
                            waveSpeed = 1.dp,
                            wavelength = 50.dp,
                            gapSize = 5.dp,
                            modifier = Modifier
                                .width(250.dp)
                                .padding(top = 10.dp, bottom = 5.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        stormSpecs.forEach { spec ->
                            StormSpecCard(spec = spec)
                        }
                    }
                }
            }
        }
    }
}

private fun specToRisk(spec: StormSpec): String {
    val numericString = spec.value.replace(Regex("[^0-9.-]"), "")
    val value = numericString.toDoubleOrNull() ?: return "Unknown"

    return when (spec.label) {
        "CAPE" -> when {
            value < 1000 -> "Low"
            value in 1000.0..2500.0 -> "Moderate"
            value in 2501.0..4000.0 -> "High"
            else -> "Extreme"
        }
        "CIN" -> when {
            value > 100 -> "Low"
            value in 25.0..100.0 -> "Moderate"
            value in 1.0..24.0 -> "High"
            else -> "Extreme"
        }
        "SRH" -> when {
            value < 100 -> "Low"
            value in 100.0..250.0 -> "Moderate"
            value in 251.0..400.0 -> "High"
            else -> "Extreme"
        }
        "Lifted" -> when {
            value >= 0 -> "Low"
            value in -4.0..-1.0 -> "Moderate"
            value in -7.0..-5.0 -> "High"
            else -> "Extreme"
        }
        "LL Shear" -> when {
            value < 15 -> "Low"
            value in 15.0..25.0 -> "Moderate"
            value in 26.0..40.0 -> "High"
            else -> "Extreme"
        }
        "Dew Pt" -> when {
            value < 55 -> "Low"
            value in 55.0..64.0 -> "Moderate"
            value in 65.0..72.0 -> "High"
            else -> "Extreme"
        }
        "RH" -> when {
            value < 50 -> "Low"
            value in 50.0..70.0 -> "Moderate"
            value in 71.0..85.0 -> "High"
            else -> "Extreme"
        }
        "Gust" -> when {
            value < 40 -> "Low"
            value in 40.0..57.0 -> "Moderate"
            value in 58.0..74.0 -> "High"
            else -> "Extreme"
        }
        else -> "Unknown"
    }
}

@Composable
private fun StormSpecCard(spec: StormSpec) {
    val colors = ExtendedColors()
    val risk = specToRisk(spec)
    val riskColor = when (risk) {
        "Low" -> colors.lowRisk
        "Moderate" -> colors.moderateRisk
        "High" -> colors.highRisk
        "Extreme" -> colors.extremeRisk
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.width(136.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = spec.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Surface(shape = CircleShape, color = riskColor.copy(alpha = 0.14f)) {
                    Text(
                        text = risk,
                        color = riskColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                text = spec.value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                maxLines = 1,
            )
            RiskMeter(risk = risk, color = riskColor)
            Text(
                text = spec.detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RiskMeter(risk: String, color: Color) {
    val progress = when (risk) {
        "Low" -> 0.28f
        "Moderate" -> 0.52f
        "High" -> 0.76f
        "Extreme" -> 1f
        else -> 0.18f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(7.dp)
                .background(color, CircleShape),
        )
    }
}
