package com.example.stormpilot.features.dashboard.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.common.ui.AccountMenuAnchor
import com.example.stormpilot.features.common.ui.AnimatedStormAiChatBackdrop
import com.example.stormpilot.features.dashboard.ui.aichat.ChatPopup
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.features.shared.viewmodels.GenAIViewModel
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    genAIViewModel: GenAIViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit = {},
) {
    var showChat by remember { mutableStateOf(false) }
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
                onOpenChat = { showChat = true },
            )

            AnimatedStormAiChatBackdrop(
                visible = showChat,
                modifier = Modifier.fillMaxSize(),
            )

            if (showChat) {
                ChatPopup({ showChat = false }, genAIViewModel)
            }
        }
    }
}

@Composable
fun TopIconRow(
    alertsViewModel: AlertsViewModel = hiltViewModel(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onOpenChat: () -> Unit = {},
) {

    val cityName by alertsViewModel.cityName.collectAsStateWithLifecycle()
    val displayCityName = remember(cityName) { formatCityAndState(cityName) }
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    VerticalDivider(
                        thickness = 6.dp,
                        color = MaterialTheme.extendedColors.weather.temperatureChartLine,
                        modifier = Modifier
                            .height(28.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = displayCityName,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.W700,
                        fontSize = 21.sp,
                        modifier = Modifier.padding(start = 6.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                        FilledIconButton(
                            onClick = onOpenChat,
                            modifier = Modifier
                                .size(44.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            ShimmeringStormAiChatIcon()
                        }


                    Spacer(modifier = Modifier.width(0.dp))

                    AccountMenuAnchor(onClick = onOpenSettings, circleSize = 43, textSize = 15)
                }
            }


        }


}

@Composable
private fun ShimmeringStormAiChatIcon() {
    val stormAiColors = MaterialTheme.extendedColors.stormAi
    val shimmerTransition = rememberInfiniteTransition(label = "stormAiChatIconShimmer")
    val shimmerProgress by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "stormAiChatIconShimmerProgress",
    )

    Icon(
        imageVector = Icons.Rounded.AutoAwesome,
        tint = stormAiColors.iconContent,
        contentDescription = "Open StormPilot AI chat",
        modifier = Modifier
            .size(28.dp)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                val shimmerTravel = size.width * 2.4f
                val startX = -shimmerTravel + shimmerProgress * shimmerTravel * 2f
                val shimmerBrush = Brush.linearGradient(
                    colors = stormAiColors.glowColors() + stormAiColors.glowBlue,
                    start = Offset(startX, 0f),
                    end = Offset(startX + shimmerTravel, size.height),
                )

                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = shimmerBrush,
                        blendMode = BlendMode.SrcIn,
                    )
                }
            },
    )
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
