package com.example.stormpilot.features.dashboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.common.ui.AccountMenuAnchor
import com.example.stormpilot.features.navigation.ui.components.StormAiLauncherMenu
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.extendedColors

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    onOpenSettings: () -> Unit = {},
    onOpenStormAiChat: () -> Unit,
    onOpenStormAiVoice: () -> Unit,
) {
    val colors = MaterialTheme.extendedColors

    StormPilotTheme(
        opaqueNavigationBar = true,
        navigationBarColorOverride = colors.navigation.barSurface
    ) {
        val dashboardWidgetContainerColor = if (AppSettings.isDarkMode) colorScheme.surfaceContainerLow.copy(alpha = 0.85f) else colorScheme.surfaceContainerLowest

        Box(
            modifier = Modifier.fillMaxSize()
                .background(colorScheme.surfaceDim),

            ) {
            DashboardOverviewScreen(
                widgetContainerColor = dashboardWidgetContainerColor,
                onOpenStormAiChat = onOpenStormAiChat,
            )

            TopIconRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                onOpenSettings = onOpenSettings,
                onOpenStormAiChat = onOpenStormAiChat,
                onOpenStormAiVoice = onOpenStormAiVoice,
            )
        }
    }
}

@Composable
fun TopIconRow(
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onOpenStormAiChat: () -> Unit,
    onOpenStormAiVoice: () -> Unit,
) {
    val cityName by alertsViewModel.cityName.collectAsStateWithLifecycle()
    val displayCityName = remember(cityName) { formatCityAndState(cityName) }
    val wand = rememberStormAiWand()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Location Icon",
                tint = MaterialTheme.extendedColors.theme.vibrantPrimary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = displayCityName,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            AccountMenuAnchor(
                modifier = Modifier.padding(start = with(LocalDensity.current) { 12.toDp() }),
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
internal fun AskStormAiButton(onClick: () -> Unit) {
    val actionColor = MaterialTheme.extendedColors.theme.vibrantPrimary

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, actionColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = actionColor),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = rememberStormAiWand(),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = "Ask StormAI",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun rememberStormAiWand(): ImageVector {
    val aiColors = MaterialTheme.extendedColors.stormAi
    return remember(aiColors.aiOutlineBlue, aiColors.aiOutlinePink) {
        ImageVector.Builder("Wand Stars", 28.dp, 28.dp, 960f, 960f)
            .addPath(
                // Material Symbols Outlined wand_stars, shifted to a zero-based viewport.
                pathData = PathParser().parsePathString(
                    "M176,840l-56,-56 301,-302 -181,-45 198,-123 -17,-234 179,151 216,-88 -87,217 " +
                        "151,178 -234,-16 -124,198 -45,-181 -301,301Z " +
                        "m24,-520 -80,-80 80,-80 80,80 -80,80Z " +
                        "m355,197 48,-79 93,7 -60,-71 35,-86 -86,35 -71,-59 7,92 -79,49 90,22 23,90Z " +
                        "m165,323 -80,-80 80,-80 80,80 -80,80Z",
                ).toNodes(),
                fill = Brush.linearGradient(
                    colors = listOf(aiColors.aiOutlinePink, aiColors.aiOutlineBlue),
                    start = Offset(120f, 840f),
                    end = Offset(840f, 120f),
                ),
            )
            .build()
    }
}

private fun formatCityAndState(cityName: String): String {
    val parts = cityName
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return if (parts.size >= 2) {
        parts.take(2).joinToString(", ")
    } else {
        cityName
    }
}
