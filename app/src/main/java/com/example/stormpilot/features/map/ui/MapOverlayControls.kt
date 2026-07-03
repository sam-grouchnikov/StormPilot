package com.example.stormpilot.features.map.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.navigation.ui.components.coloredShadow
import com.example.stormpilot.ui.theme.ExtendedColors

@Composable
internal fun MapOverlayControls(
    showRadarOverlay: Boolean,
    onRadarOverlayClick: () -> Unit,
    showSevereAlertsOverlay: Boolean,
    onSevereAlertsOverlayClick: () -> Unit,
    navMode: Boolean,
    is2dNavView: Boolean,
    onNavViewToggleClick: () -> Unit,
    colors: ExtendedColors,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        FilledIconButton(
            onClick = onRadarOverlayClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (showRadarOverlay) {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                } else {
                    colors.navigation.floatingControlButtonContainer
                },
            ),
            modifier = Modifier.coloredShadow(
                color = colors.navigation.floatingControlShadow,
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

        FilledIconButton(
            onClick = onSevereAlertsOverlayClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (showSevereAlertsOverlay) {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                } else {
                    colors.navigation.floatingControlButtonContainer
                },
            ),
            modifier = Modifier.coloredShadow(
                color = colors.navigation.floatingControlShadow,
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

        if (navMode) {
            FilledIconButton(
                onClick = onNavViewToggleClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.navigation.floatingControlContainer,
                ),
                modifier = Modifier.coloredShadow(
                    color = colors.navigation.floatingControlShadow,
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
}
