package com.example.stormpilot.features.dashboard.ui.alerts.components

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
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.shared.data.weather.StormSpec
import com.example.stormpilot.ui.theme.extendedColors

@Composable
fun StormSpecsPanel(
    stormSpecs: List<StormSpec>,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = Color.Transparent
    ) {
        Box {
            Column(
                modifier = Modifier.padding(vertical = 0.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
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
                            color = MaterialTheme.extendedColors.theme.vibrantPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        stormSpecs.forEach { spec ->
                            StormSpecCard(
                                spec = spec,
                                containerColor = containerColor,
                            )
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
            value in 1000.0..2500.0 -> "Medium"
            value in 2501.0..4000.0 -> "High"
            else -> "Extreme"
        }
        "CIN" -> when {
            value > 100 -> "Low"
            value in 25.0..100.0 -> "Medium"
            value in 1.0..24.0 -> "High"
            else -> "Extreme"
        }
        "SRH" -> when {
            value < 100 -> "Low"
            value in 100.0..250.0 -> "Medium"
            value in 251.0..400.0 -> "High"
            else -> "Extreme"
        }
        "Lifted" -> when {
            value >= 0 -> "Low"
            value in -4.0..-1.0 -> "Medium"
            value in -7.0..-5.0 -> "High"
            else -> "Extreme"
        }
        "LL Shear" -> when {
            value < 15 -> "Low"
            value in 15.0..25.0 -> "Medium"
            value in 26.0..40.0 -> "High"
            else -> "Extreme"
        }
        "Dew Pt" -> when {
            value < 55 -> "Low"
            value in 55.0..64.0 -> "Medium"
            value in 65.0..72.0 -> "High"
            else -> "Extreme"
        }
        "RH" -> when {
            value < 50 -> "Low"
            value in 50.0..70.0 -> "Medium"
            value in 71.0..85.0 -> "High"
            else -> "Extreme"
        }
        "Gust" -> when {
            value < 40 -> "Low"
            value in 40.0..57.0 -> "Medium"
            value in 58.0..74.0 -> "High"
            else -> "Extreme"
        }
        else -> "Unknown"
    }
}

@Composable
private fun StormSpecCard(
    spec: StormSpec,
    containerColor: Color,
) {
    val colors = MaterialTheme.extendedColors
    val risk = specToRisk(spec)
    val riskColor = colors.theme.vibrantPrimary

    Surface(
        modifier = Modifier.width(115.dp),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = spec.label,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = spec.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1.0f),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1,
            )
            RiskMeter(risk = risk, color = riskColor)
        }
    }
}

@Composable
private fun RiskMeter(risk: String, color: Color) {
    val activeDots = when (risk) {
        "Low" -> 1
        "Moderate", "Medium" -> 2
        "High" -> 4
        "Extreme" -> 5
        else -> 1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, top = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(activeDots) { index ->
            val transparency = (index) * 0.15f + 0.40f
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(
                        color = color.copy(transparency),
                        shape = CircleShape
                    )
            )
        }
    }
}
