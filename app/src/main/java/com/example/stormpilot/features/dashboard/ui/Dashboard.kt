package com.example.stormpilot.features.dashboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.common.ui.AccountMenuAnchor
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.extendedColors

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    onOpenSettings: () -> Unit = {},
) {
    val colors = MaterialTheme.extendedColors

    StormPilotTheme(
        opaqueNavigationBar = true,
        navigationBarColorOverride = colors.navigation.barSurface
    ) {
        val dashboardWidgetContainerColor = if (AppSettings.isDarkMode) colorScheme.surfaceContainerLow.copy(alpha = 0.85f) else colorScheme.surfaceContainerLowest

        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    color = if (AppSettings.isDarkMode) colorScheme.surfaceContainerLowest else colorScheme.surfaceContainerLow
                ),

            ) {
            DashboardOverviewScreen(widgetContainerColor = dashboardWidgetContainerColor)

            TopIconRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
fun TopIconRow(
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
) {
    val cityName by alertsViewModel.cityName.collectAsStateWithLifecycle()
    val displayCityName = remember(cityName) { formatCityAndState(cityName) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            VerticalDivider(
                thickness = 6.dp,
                color = MaterialTheme.extendedColors.theme.vibrantPrimary,
                modifier = Modifier
                    .height(28.dp)
                    .clip(CircleShape),
            )
            Text(
                text = displayCityName,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.W700,
                fontSize = 21.sp,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            AccountMenuAnchor(onClick = onOpenSettings, circleSize = 43, textSize = 15)
        }
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
