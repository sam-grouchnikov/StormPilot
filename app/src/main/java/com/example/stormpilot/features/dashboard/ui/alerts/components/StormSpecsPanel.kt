package com.example.stormpilot.features.dashboard.ui.alerts.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.weather.data.StormSpec
import com.example.stormpilot.ui.theme.ExtendedColors

@Composable
fun StormSpecsPanel(stormSpecs: List<StormSpec>) {
    val colors = ExtendedColors()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 50.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = colors.stormContainer,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Storm Environment",
                color = colors.stormText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
            androidx.compose.foundation.layout.Row(
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
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 5.dp),
                        color = colors.stormText,
                        trackColor = colors.stormContainer,
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
        modifier = Modifier.width(118.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = colors.stormContainerNested,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = spec.label,
                color = colors.stormTextNested,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
            )
            Text(
                text = spec.value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
            )
            Text(
                text = "$risk risk",
                color = riskColor,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 13.sp,
            )
        }
    }
}
