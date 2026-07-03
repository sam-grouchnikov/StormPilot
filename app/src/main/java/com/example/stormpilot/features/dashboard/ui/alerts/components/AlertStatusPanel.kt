package com.example.stormpilot.features.dashboard.ui.alerts.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Flood
import androidx.compose.material.icons.outlined.Tornado
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.R
import com.example.stormpilot.features.shared.data.alerts.NwsAlert
import com.example.stormpilot.features.shared.viewmodels.AlertsUiState
import com.example.stormpilot.ui.theme.extendedColors

@Composable
fun AlertStatusPanel(
    alertsState: AlertsUiState,
    onAlertClick: (NwsAlert) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    var expanded by remember { mutableStateOf(false) }
    val panelHasAlert = alertsState.hasAnyAlert
    val activeAlertRows = buildList {
        if (alertsState.tornadoWatch != null || alertsState.tornadoWarning != null) {
            add(
                ActiveAlertRow(
                    icon = Icons.Outlined.Tornado,
                    label = "Tornado",
                    alert = alertsState.tornadoWarning ?: alertsState.tornadoWatch,
                    state = alertState(
                        warningActive = alertsState.tornadoWarning != null,
                        watchActive = alertsState.tornadoWatch != null,
                        warningText = "Tornado Warning",
                        watchText = "Tornado Watch",
                        clearText = "No Tornado Alerts",
                    ),
                ),
            )
        }
        if (alertsState.severeThunderstormWatch != null || alertsState.severeThunderstormWarning != null) {
            add(
                ActiveAlertRow(
                    icon = Icons.Outlined.Bolt,
                    label = "Severe Thunderstorm",
                    alert = alertsState.severeThunderstormWarning ?: alertsState.severeThunderstormWatch,
                    state = alertState(
                        warningActive = alertsState.severeThunderstormWarning != null,
                        watchActive = alertsState.severeThunderstormWatch != null,
                        warningText = "Severe T-Storm Warning",
                        watchText = "Severe T-Storm Watch",
                        clearText = "No Storm Alerts",
                    ),
                ),
            )
        }
        if (alertsState.flashFloodWatch != null || alertsState.flashFloodWarning != null) {
            add(
                ActiveAlertRow(
                    icon = Icons.Outlined.Flood,
                    label = "Flood",
                    alert = alertsState.flashFloodWarning ?: alertsState.flashFloodWatch,
                    state = alertState(
                        warningActive = alertsState.flashFloodWarning != null,
                        watchActive = alertsState.flashFloodWatch != null,
                        warningText = "Flash Flood Warning",
                        watchText = "Flash Flood Watch",
                        clearText = "No Flood Alerts",
                    ),
                ),
            )
        }
    }


    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(start = 10.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = if (panelHasAlert) painterResource(id = R.drawable.thick_error) else painterResource(id = R.drawable.check_thick),
                    tint = MaterialTheme.extendedColors.weather.temperatureChartLine,
                    contentDescription = "Alert Status",
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (!panelHasAlert) "No Active Alerts" else " ${alertsState.activeAlertsCount} Active Alert(s)",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.95f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (expanded) "Collapse alerts" else "Expand alerts"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top,
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 220, delayMillis = 40),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 180),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(top = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    activeAlertRows.forEachIndexed { index, row ->
                        SlidingAlertStatusRow(
                            row = row,
                            index = index,
                            rowCount = activeAlertRows.size,
                            onAlertClick = onAlertClick,
                            clearContainerColor = containerColor,
                        )
                    }
                }
            }
        }
    }
}

private data class ActiveAlertRow(
    val icon: ImageVector,
    val label: String,
    val alert: NwsAlert?,
    val state: LocationAlertState,
)

@Composable
private fun SlidingAlertStatusRow(
    row: ActiveAlertRow,
    index: Int,
    rowCount: Int,
    onAlertClick: (NwsAlert) -> Unit,
    clearContainerColor: Color,
) {
    val visibleState = remember(row.label, row.alert?.id) {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    val staggerDelayMillis = alertRowStaggerDelayMillis(index = index, rowCount = rowCount)

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInHorizontally(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = staggerDelayMillis,
                easing = FastOutSlowInEasing,
            ),
        ) { fullWidth -> -fullWidth / 3 } + fadeIn(
            animationSpec = tween(
                durationMillis = 180,
                delayMillis = staggerDelayMillis,
            ),
        ),
        exit = ExitTransition.None,
    ) {
        AlertStatusRow(
            icon = row.icon,
            label = row.label,
            alert = row.alert,
            state = row.state,
            onAlertClick = onAlertClick,
            clearContainerColor = clearContainerColor,
        )
    }
}

private fun alertRowStaggerDelayMillis(index: Int, rowCount: Int): Int {
    if (rowCount <= 1) return 0

    val staggerStepMillis = (AlertRowStaggerWindowMillis / rowCount).coerceIn(
        minimumValue = AlertRowMinStaggerMillis,
        maximumValue = AlertRowMaxStaggerMillis,
    )
    return index * staggerStepMillis
}

@Composable
private fun AlertStatusRow(
    icon: ImageVector,
    label: String,
    alert: NwsAlert?,
    state: LocationAlertState,
    onAlertClick: (NwsAlert) -> Unit,
    clearContainerColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.extendedColors
    val isWarning = state.level == AlertLevel.Warning
    val containerTarget = when (state.level) {
        AlertLevel.Clear -> clearContainerColor
        AlertLevel.Watch -> colors.alerts.watchContainer.copy(alpha = 0.5f)
        AlertLevel.Warning -> colors.alerts.warningContainer.copy(alpha = 0.7f)
    }
    val contentTarget = when (state.level) {
        AlertLevel.Clear -> MaterialTheme.colorScheme.onSurfaceVariant
        AlertLevel.Watch -> colors.alerts.watchContent
        AlertLevel.Warning -> colors.alerts.warningSupportingContent
    }
    val titleTarget = when (state.level) {
        AlertLevel.Clear -> colors.alerts.clearContent
        AlertLevel.Watch -> colors.alerts.watchContent
        AlertLevel.Warning -> colors.alerts.warningContent
    }
    val iconTarget = when (state.level) {
        AlertLevel.Clear -> colors.alerts.clearEmphasisContent
        AlertLevel.Watch -> colors.alerts.watchContent
        AlertLevel.Warning -> colors.alerts.warningContent
    }
    val containerColor by animateColorAsState(
        targetValue = containerTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_container_color",
    )
    val contentColor by animateColorAsState(
        targetValue = contentTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_content_color",
    )
    val titleColor by animateColorAsState(
        targetValue = titleTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_title_color",
    )
    val iconColor by animateColorAsState(
        targetValue = iconTarget,
        animationSpec = tween(durationMillis = 450),
        label = "alert_icon_color",
    )
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "alert_row_scale",
    )
    val warningIconTransition = rememberInfiniteTransition(label = "warning_icon_pulse")
    val warningIconScale by warningIconTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "warning_icon_scale",
    )
    val warningIconAlpha by warningIconTransition.animateFloat(
        initialValue = 0.68f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "warning_icon_alpha",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .scale(scale)
            .clickable(enabled = alert != null) {
                alert?.let(onAlertClick)
            },
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = contentColor.copy(alpha = if (state.level == AlertLevel.Clear) 0.08f else 0.16f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isWarning) Icons.Outlined.PriorityHigh else icon,
                        contentDescription = if (isWarning) "Warning active" else null,
                        tint = iconColor,
                        modifier = Modifier
                            .size(21.dp)
                            .scale(if (isWarning) warningIconScale else 1f)
                            .alpha(if (isWarning) warningIconAlpha else 1f),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                AnimatedContent(
                    targetState = state.text,
                    transitionSpec = {
                        fadeIn(tween(220, delayMillis = 70)) togetherWith fadeOut(tween(140))
                    },
                    label = "alert_status_text",
                ) { text ->
                    Text(
                        text = text,
                        color = contentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                    )
                }
            }
            if (alert != null) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Open alert details",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private enum class AlertLevel {
    Clear,
    Watch,
    Warning,
}

private data class LocationAlertState(
    val level: AlertLevel,
    val text: String,
)

private fun alertState(
    warningActive: Boolean,
    watchActive: Boolean,
    warningText: String,
    watchText: String,
    clearText: String,
): LocationAlertState = when {
    warningActive -> LocationAlertState(AlertLevel.Warning, warningText)
    watchActive -> LocationAlertState(AlertLevel.Watch, watchText)
    else -> LocationAlertState(AlertLevel.Clear, clearText)
}

private const val AlertRowStaggerWindowMillis = 180
private const val AlertRowMinStaggerMillis = 60
private const val AlertRowMaxStaggerMillis = 90
