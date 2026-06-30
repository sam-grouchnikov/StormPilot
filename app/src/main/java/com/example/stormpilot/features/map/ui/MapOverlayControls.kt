package com.example.stormpilot.features.map.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.navigation.ui.components.coloredShadow
import com.example.stormpilot.ui.theme.ExtendedColors
import kotlinx.coroutines.delay

@Composable
internal fun MapOverlayControls(
    showRadarOverlay: Boolean,
    onRadarOverlayClick: () -> Unit,
    showSevereAlertsOverlay: Boolean,
    onSevereAlertsOverlayClick: () -> Unit,
    navMode: Boolean,
    is2dNavView: Boolean,
    onNavViewToggleClick: () -> Unit,
    onFoldClick: () -> Unit,
    colors: ExtendedColors,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        StaggeredMapControlButton(index = 0) {
            FilledIconButton(
                onClick = onRadarOverlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (showRadarOverlay) {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    } else {
                        colors.iconButtonColor
                    },
                ),
                modifier = Modifier.coloredShadow(
                    color = colors.purpleShadow,
                    blurRadius = 2.dp,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Toggle radar overlay",
                    tint = if (showRadarOverlay) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }

        StaggeredMapControlButton(index = 1) {
            FilledIconButton(
                onClick = onSevereAlertsOverlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (showSevereAlertsOverlay) {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    } else {
                        colors.iconButtonColor
                    },
                ),
                modifier = Modifier.coloredShadow(
                    color = colors.purpleShadow,
                    blurRadius = 2.dp,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = "Toggle severe weather alerts overlay",
                    tint = if (showSevereAlertsOverlay) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }

        if (navMode) {
            StaggeredMapControlButton(index = 2) {
                FilledIconButton(
                    onClick = onNavViewToggleClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colors.searchBarColor,
                    ),
                    modifier = Modifier.coloredShadow(
                        color = colors.purpleShadow,
                        blurRadius = 2.dp,
                    ),
                ) {
                    Icon(
                        imageVector = if (!is2dNavView) Icons.Outlined.Navigation else Icons.Filled.Crop,
                        contentDescription = if (!is2dNavView) "Enable 2D view" else "Enable 3D view",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        StaggeredMapControlButton(index = if (navMode) 3 else 2) {
            FilledIconButton(
                onClick = onFoldClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.searchBarColor,
                ),
                modifier = Modifier.coloredShadow(
                    color = colors.purpleShadow,
                    blurRadius = 2.dp,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Fold map controls",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun StaggeredMapControlButton(
    index: Int,
    content: @Composable () -> Unit,
) {
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(index) {
        delay(index * 60L)
        revealed = true
    }

    val offsetY by animateDpAsState(
        targetValue = if (revealed) 0.dp else (-18).dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "map_control_button_offset_$index",
    )
    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "map_control_button_alpha_$index",
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .alpha(alpha),
    ) {
        content()
    }
}
