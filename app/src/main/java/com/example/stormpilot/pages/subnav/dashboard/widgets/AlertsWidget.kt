package com.example.stormpilot.pages.subnav.dashboard.widgets

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Flood
import androidx.compose.material.icons.outlined.Tornado
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.ui.theme.WarningColorStates
import com.example.stormpilot.viewmodel.AlertsViewModel

@Composable fun AlertsWidget(viewModel: AlertsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stateColors = WarningColorStates()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(15.dp))

            val floodActive = uiState.flashFloodWarning != null
            val floodText = "Flash Flood Warning"

            WarningWidget(
                warningIcon = Icons.Outlined.Flood,
                warning = if (floodActive) floodText else "No Flood Alerts",
                containerColor = if (floodActive) stateColors.flashFloodContainer else stateColors.disabledContainer,
                iconColor = if (floodActive) stateColors.flashFloodContents else stateColors.disabledContents
            )

            Spacer(modifier = Modifier.height(10.dp))

            val stormActive = uiState.severeThunderstormWarning != null
            val stormText = "T-Storm Warning"

            WarningWidget(
                warningIcon = Icons.Outlined.Bolt,
                warning = if (stormActive) stormText else "No Storm Alerts",
                containerColor = if (stormActive) stateColors.thunderstormContainer else stateColors.disabledContainer,
                iconColor = if (stormActive) stateColors.thunderstormContents else stateColors.disabledContents
            )

            Spacer(modifier = Modifier.height(10.dp))

            val tornadoActive = uiState.tornadoWarning != null
            val tornadoText = "Tornado Warning"

            WarningWidget(
                warningIcon = Icons.Outlined.Tornado,
                warning = if (tornadoActive) tornadoText else "No Tornado Alerts",
                containerColor = if (tornadoActive) Color.Red else stateColors.disabledContainer,
                iconColor = if (tornadoActive) Color.White else stateColors.disabledContents
            )

            Log.v("Alerts", "Flood: $floodActive Storm: $stormActive Tornado: $tornadoActive")

            Spacer(modifier = Modifier.height(15.dp))

        }
    }
}

@Composable fun WarningWidget(warningIcon: ImageVector, warning: String, containerColor: Color, iconColor: Color) {
    var containerColorCopy: Color = containerColor
    var iconColorCopy: Color = iconColor

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.93f)
            .padding(start = 0.dp, end = 0.dp),
        shape = RoundedCornerShape(14.dp),
        color = containerColorCopy,
    ) {
        Row(modifier = Modifier.padding(start = 15.dp, top = 6.dp, bottom = 6.dp)) {
            Icon(
                imageVector = warningIcon,
                contentDescription = null,
                tint = iconColorCopy,
                modifier = Modifier.size(23.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = warning,
                color = iconColorCopy,
                fontWeight = FontWeight.W500,
                fontSize = 17.sp
            )
        }
    }
}