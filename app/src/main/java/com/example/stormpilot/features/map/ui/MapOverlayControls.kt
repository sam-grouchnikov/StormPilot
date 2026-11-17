package com.example.stormpilot.features.map.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.example.stormpilot.features.navigation.ui.components.coloredShadow
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapOverlayControls(
    showRadarOverlay: Boolean,
    onRadarOverlayChange: (Boolean) -> Unit,
    showSevereAlertsOverlay: Boolean,
    onSevereAlertsOverlayChange: (Boolean) -> Unit,
    navMode: Boolean,
    is2dNavView: Boolean,
    onNavViewToggleClick: () -> Unit,
    colors: ExtendedColors,
) {
    var showLayersSheet by rememberSaveable { mutableStateOf(false) }
    val anyLayerEnabled = showRadarOverlay || showSevereAlertsOverlay

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilledIconButton(
            onClick = { showLayersSheet = true },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceDim
            ),
            modifier = Modifier.coloredShadow(
                color = colors.navigation.floatingControlShadow,
                blurRadius = 2.dp,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Layers,
                contentDescription = "Map layers",
                tint = if (anyLayerEnabled) {
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

    if (showLayersSheet) {
        val allLayersEnabled = showRadarOverlay && showSevereAlertsOverlay
        val allLayersState = when {
            allLayersEnabled -> ToggleableState.On
            anyLayerEnabled -> ToggleableState.Indeterminate
            else -> ToggleableState.Off
        }

        ModalBottomSheet(
            onDismissRequest = { showLayersSheet = false },
            sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            ),
        ) {
            Text(
                text = "Map layers",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp),
            )
            MapLayerOption(
                label = "Select all",
                icon = Icons.Outlined.Layers,
                state = allLayersState,
                onClick = {
                    val enabled = !allLayersEnabled
                    if (showRadarOverlay != enabled) onRadarOverlayChange(enabled)
                    if (showSevereAlertsOverlay != enabled) onSevereAlertsOverlayChange(enabled)
                },
            )
            MapLayerOption(
                label = "Radar",
                icon = Icons.Outlined.Shield,
                state = ToggleableState(showRadarOverlay),
                onClick = { onRadarOverlayChange(!showRadarOverlay) },
            )
            MapLayerOption(
                label = "Warnings",
                icon = Icons.Outlined.WarningAmber,
                state = ToggleableState(showSevereAlertsOverlay),
                onClick = { onSevereAlertsOverlayChange(!showSevereAlertsOverlay) },
            )
        }
    }
}

@Composable
private fun MapLayerOption(
    label: String,
    icon: ImageVector,
    state: ToggleableState,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { TriStateCheckbox(state = state, onClick = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.triStateToggleable(
            state = state,
            role = Role.Checkbox,
            onClick = onClick,
        ),
    )
}
