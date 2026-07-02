package com.example.stormpilot.features.dashboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.features.shared.viewmodels.GenAIViewModel
import com.example.stormpilot.features.dashboard.ui.aichat.ChatPopup
import com.example.stormpilot.R
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.common.ui.AccountMenuAnchor
import com.example.stormpilot.features.common.ui.AnimatedStormAiChatBackdrop
import com.example.stormpilot.features.shared.viewmodels.AlertsViewModel
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.ui.theme.extendedColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RadarPage(
    genAIViewModel: GenAIViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit = {},
) {
    var showChat by remember { mutableStateOf(false) }
    var topIconRowHeightPx by remember { mutableStateOf(0) }
    val colors = MaterialTheme.extendedColors

    val settings = AppSettings
    StormPilotTheme(
        opaqueNavigationBar = true,
        navigationBarColorOverride = colors.purpleSurfaceContainer
    ) {
        val dashboardWidgetContainerColor = if (AppSettings.isDarkMode) colorScheme.surfaceContainerLow.copy(alpha = 0.85f) else colorScheme.surfaceContainerLowest

        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    color = if (AppSettings.isDarkMode) colorScheme.surfaceContainerLowest else colorScheme.surfaceContainerLow
                ),

            ) {
//            DashboardOverviewScreen(
//                modifier = Modifier.dashboardTopIconRowBackdropBlur(topIconRowHeightPx),
//            )
            DashboardOverviewScreen(widgetContainerColor = dashboardWidgetContainerColor)

            TopIconRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                onHeightChanged = { topIconRowHeightPx = it },
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
    onHeightChanged: (Int) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenChat: () -> Unit = {},
) {

    val cityName by alertsViewModel.cityName.collectAsStateWithLifecycle()
    val displayCityName = remember(cityName) { formatCityAndState(cityName) }
    Box(
        modifier = modifier
            .onSizeChanged { onHeightChanged(it.height) }
//            .background(colorScheme.surfaceContainer.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
//                .background(color = Color.Red)
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.LocationOn,
//                        contentDescription = null,
//                        tint = colorScheme.primary,
//                        modifier = Modifier.size(24.dp),
//                    )
                    VerticalDivider(
                        thickness = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
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

                    IconButton(
                        onClick = onOpenChat,
                        modifier = Modifier.size(43.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Open StormPilot AI chat",
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AccountMenuAnchor(onClick = onOpenSettings, circleSize = 43, textSize = 15)
                }
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

private val TopIconRowBackdropBlurRadius = 70.dp

private fun Modifier.dashboardTopIconRowBackdropBlur(topIconRowHeightPx: Int): Modifier {
    if (topIconRowHeightPx <= 0) return this

    return drawWithCache {
        val blurBottom = (topIconRowHeightPx - 0.dp.toPx()).coerceAtMost(size.height)
        val blurRadiusPx = TopIconRowBackdropBlurRadius.toPx()
        val captureBottom = (blurBottom + blurRadiusPx * 2f).coerceAtMost(size.height)
        val blurLayerSize = IntSize(size.width.roundToInt(), captureBottom.roundToInt())
        val blurLayer = obtainGraphicsLayer().apply {
            clip = true
            renderEffect = BlurEffect(
                blurRadiusPx,
                blurRadiusPx,
                TileMode.Clamp,
            )
        }

        onDrawWithContent {
            val contentScope = this

            if (blurBottom <= 0f) return@onDrawWithContent

            blurLayer.record(size = blurLayerSize) {
                contentScope.drawContent()
            }

            drawContent()

            clipRect(left = 0f, top = 0f, right = size.width, bottom = blurBottom) {
                drawLayer(blurLayer)
            }
        }
    }
}

@Composable
fun ForecastSectionTitle(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, start = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(99.dp)),
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}
