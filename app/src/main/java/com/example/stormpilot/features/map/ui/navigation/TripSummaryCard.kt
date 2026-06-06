package com.example.stormpilot.features.map.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.shared.data.routing.formatDistance
import com.example.stormpilot.features.shared.data.routing.formatDuration
import com.example.stormpilot.features.shared.viewmodels.MapsUiState
import com.example.stormpilot.ui.theme.ExtendedColors
import kotlinx.coroutines.delay

@Composable
fun TripSummaryCard(
    state: MapsUiState,
    onRetry: () -> Unit,
    onDirectionsClick: () -> Unit,
    onSafeDirectionsClick: () -> Unit,
    onClearRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoadingRoute) {
        if (state.isLoadingRoute) {
            delay(500)
            showIndicator = true
        } else {
            showIndicator = false
        }
    }

    if (state.address != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!state.isLoadingRoute) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = state.address,
                            modifier = Modifier.weight(1f),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        FilledIconButton(
                            onClick = onClearRoute,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                if (state.isLoadingRoute && showIndicator) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                }

                state.routeError?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Retry",
                            modifier = Modifier.clickable { onRetry() },
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Clear",
                            modifier = Modifier.clickable { onClearRoute() },
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                val displayedDistance = state.remainingDistanceMeters ?: state.distanceMeters
                val displayedDuration = state.remainingDurationSeconds ?: state.durationSeconds
                if (!state.isLoadingRoute) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        Button(
                            onClick = onDirectionsClick,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Button(
                            onClick = onSafeDirectionsClick,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = ExtendedColors().alertClearContentAlternate,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Safe Start",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = ExtendedColors().alertClearContentAlternate,
                            )
                        }
                    }

                    if (displayedDistance != null && displayedDuration != null) {
                        Text(
                            text = "${formatDistance(displayedDistance)} • ${formatDuration(displayedDuration)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val warningCount = state.routeWarningCount
                            val warningError = state.routeWarningError
                            val warningColor = when {
                                warningError != null && (warningCount ?: 0) > 0 -> MaterialTheme.colorScheme.error
                                warningError != null -> MaterialTheme.colorScheme.onSurfaceVariant
                                warningCount == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                warningCount == 0 -> ExtendedColors().alertClearContent
                                else -> MaterialTheme.colorScheme.error
                            }

                            if (warningCount == null && warningError == null) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                    color = warningColor,
                                )
                            } else {
                                Icon(
                                    imageVector = if (warningCount == 0) Icons.Outlined.Check else Icons.Outlined.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = warningColor,
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    warningError != null -> "No route found avoiding storm warnings"
                                    warningCount == null -> "Checking warnings en route"
                                    warningCount == 0 -> "No warnings en route"
                                    warningCount == 1 -> "1 warning en route"
                                    else -> "$warningCount warnings en route"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = warningColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
