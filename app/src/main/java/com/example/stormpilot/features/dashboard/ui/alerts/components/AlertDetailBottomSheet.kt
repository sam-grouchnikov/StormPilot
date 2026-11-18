package com.example.stormpilot.features.dashboard.ui.alerts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.dashboard.ui.SkeletonBlock
import com.example.stormpilot.features.dashboard.ui.rememberSkeletonShimmer
import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailBottomSheet(
    isVisible: Boolean,
    alert: NwsAlert?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when {
                isLoading -> AlertDetailLoadingState()
                errorMessage != null -> AlertDetailMessageState(
                    title = "Alert details unavailable",
                    body = errorMessage,
                    isError = true,
                )
                alert != null -> AlertDetailContent(alert = alert)
                else -> AlertDetailMessageState(
                    title = "Alert details unavailable",
                    body = "No detail text is available for this alert.",
                    isError = false,
                )
            }
        }
    }
}

@Composable
private fun AlertDetailContent(alert: NwsAlert) {
    Text(
        text = alert.headline.ifBlank { alert.event },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DetailTag(
            label = "Severity",
            value = alert.severity.ifBlank { "Unknown" },
            modifier = Modifier.weight(1f),
        )
        DetailTag(
            label = "Event",
            value = alert.event,
            modifier = Modifier.weight(1f),
        )
    }

    DetailSection(title = "Effective", body = formatAlertTime(alert.effective))
    DetailSection(title = "Expires", body = formatAlertTime(alert.expires))
    DetailSection(
        title = "Affected areas",
        body = alert.areaDescription ?: "Not provided by NWS.",
    )
    DetailSection(
        title = "Affected zones",
        body = alert.affectedZones.takeIf { it.isNotEmpty() }?.joinToString(", ")
            ?: "Not provided by NWS.",
    )
    DetailSection(
        title = "Full description",
        body = alert.description.ifBlank { "No description provided." },
    )
    DetailSection(
        title = "What to do",
        body = alert.instruction ?: "No action instructions provided by NWS.",
    )
    DetailSection(
        title = "Issued by",
        body = alert.senderName.ifBlank { "National Weather Service" },
    )
}

@Composable
private fun AlertDetailLoadingState() {
    val shimmer = rememberSkeletonShimmer()
    val tagShimmer = shimmer.delayedBy(80)
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    SkeletonBlock(shimmer, Modifier.fillMaxWidth(0.8f).height(26.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(2) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SkeletonBlock(tagShimmer, Modifier.fillMaxWidth(0.4f).height(10.dp))
                    SkeletonBlock(tagShimmer, Modifier.fillMaxWidth(0.72f).height(15.dp))
                }
            }
        }
    }
    // Effective time, expiry, areas, zones, description, instructions, and issuer.
    for ((index, lineCount) in listOf(1, 1, 2, 2, 4, 3, 1).withIndex()) {
        val sectionShimmer = shimmer.delayedBy(150 + index * 55)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = containerColor,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SkeletonBlock(sectionShimmer, Modifier.fillMaxWidth(0.24f).height(12.dp))
                repeat(lineCount) { line ->
                    val width = if (line == lineCount - 1) 0.7f else 1f
                    SkeletonBlock(sectionShimmer, Modifier.fillMaxWidth(width).height(15.dp))
                }
            }
        }
    }
}

@Composable
private fun AlertDetailMessageState(
    title: String,
    body: String,
    isError: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun DetailTag(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun formatAlertTime(rawTime: String?): String {
    if (rawTime.isNullOrBlank()) return "Not provided by NWS."

    return runCatching {
        OffsetDateTime.parse(rawTime)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(ALERT_TIME_FORMATTER)
    }.getOrElse { rawTime }
}

private val ALERT_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d, h:mm a z", Locale.getDefault())
