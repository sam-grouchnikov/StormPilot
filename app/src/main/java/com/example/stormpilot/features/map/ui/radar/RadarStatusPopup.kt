package com.example.stormpilot.features.map.ui.radar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RadarStatusPopup(
    site: NexradSite?,
    metadata: RadarTileMetadata?,
    selectedProduct: RadarProduct,
    onProductSelected: (RadarProduct) -> Unit,
    playbackFrameCount: Int,
    playbackChangesAgo: Int?,
    isPlaybackRunning: Boolean,
    onPlaybackFrameCountSelected: (Int) -> Unit,
    onPlaybackToggled: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    val frameText = playbackChangesAgo?.let { changesAgo ->
        "$changesAgo ago"
    }
    val statusText = when {
        errorMessage != null -> "Unavailable"
        metadata?.tilesReady == false -> "Preparing"
        isLoading -> "Loading"
        metadata?.scanTimeUtc != null -> listOfNotNull(
            metadata.scanTimeUtc.toRadarScanTimeLabel(),
            frameText,
        ).joinToString(" - ")
        frameText != null -> frameText
        else -> selectedProduct.displayName
    }

    var productMenuExpanded by remember { mutableStateOf(false) }
    var frameMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = site?.id ?: "Radar",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = statusText,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (errorMessage == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }

            Box {
                TextButton(
                    onClick = { productMenuExpanded = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = selectedProduct.displayName,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = productMenuExpanded,
                    onDismissRequest = { productMenuExpanded = false }
                ) {
                    RadarProduct.entries.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.displayName) },
                            onClick = {
                                onProductSelected(product)
                                productMenuExpanded = false
                            }
                        )
                    }
                }
            }

            FilledIconButton(
                onClick = onPlaybackToggled,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = if (isPlaybackRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaybackRunning) "Pause" else "Play",
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                TextButton(
                    onClick = { frameMenuExpanded = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = playbackFrameCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = frameMenuExpanded,
                    onDismissRequest = { frameMenuExpanded = false }
                ) {
                    RADAR_PLAYBACK_FRAME_COUNTS.forEach { frameCount ->
                        DropdownMenuItem(
                            text = { Text(frameCount.toString()) },
                            onClick = {
                                onPlaybackFrameCountSelected(frameCount)
                                frameMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
