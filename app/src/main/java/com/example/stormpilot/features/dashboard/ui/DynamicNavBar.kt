package com.example.stormpilot.features.dashboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.dashboard.ui.weather.Weather
import com.example.stormpilot.features.dashboard.ui.alerts.AlertSlide
import kotlinx.coroutines.launch

data class BubbleNavigationItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernBubbleNavBarScreen(
    showChat: Boolean,
    onChatClick: () -> Unit
) {
    val items = listOf(
        BubbleNavigationItem("Alerts", Icons.Outlined.WarningAmber),
        BubbleNavigationItem("Weather", Icons.Outlined.Cloud),
        BubbleNavigationItem("AI Chat", Icons.Outlined.AutoAwesome)
    )
    val chatIndex = items.lastIndex

    val pagerState = rememberPagerState(pageCount = { chatIndex })
    val coroutineScope = rememberCoroutineScope()

    val selectedPageIndex = if (pagerState.isScrollInProgress) {
        pagerState.targetPage
    } else {
        pagerState.currentPage
    }
    val selectedIndex = if (showChat) chatIndex else selectedPageIndex


    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedDashboardBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 62.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                tonalElevation = 3.dp,
                shadowElevation = 0.dp,
            ) {
                DashboardSegmentedNav(
                    items = items,
                    selectedIndex = selectedIndex,
                    selectedPageIndex = selectedPageIndex,
                    chatIndex = chatIndex,
                    showChat = showChat,
                    onItemClick = { index ->
                        if (index == chatIndex) {
                            onChatClick()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    },
                )
            }

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp)
            ) { page ->
                when (page) {
                    0 -> AlertSlide(title = "Location Content Screen")
                    1 -> Weather(title = "Alerts Content Screen")
                }
            }
        }
    }
}

@Composable
private fun DashboardSegmentedNav(
    items: List<BubbleNavigationItem>,
    selectedIndex: Int,
    selectedPageIndex: Int,
    chatIndex: Int,
    showChat: Boolean,
    onItemClick: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(5.dp),
    ) {
        val segmentWidth = maxWidth / items.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "dashboard_nav_indicator_offset",
        )

        Surface(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .fillMaxHeight()
                .fillMaxWidth(1f / items.size),
            shape = CircleShape,
            color = colors.primaryContainer.copy(alpha = 0.86f),
            tonalElevation = 2.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.12f)),
        ) {}

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                DashboardSegment(
                    item = item,
                    selected = selectedIndex == index,
                    subdued = showChat && index == selectedPageIndex && index != chatIndex,
                    modifier = Modifier.weight(1f),
                    onClick = { onItemClick(index) },
                )
            }
        }
    }
}

@Composable
private fun DashboardSegment(
    item: BubbleNavigationItem,
    selected: Boolean,
    subdued: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.onPrimaryContainer
            subdued -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 180),
        label = "dashboard_nav_content_color",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun AnimatedDashboardBackdrop() {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "dashboard_backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dashboard_backdrop_drift",
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLowest),
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.surfaceContainerLowest,
                    colors.surfaceContainerLowest,
                    colors.surface.copy(alpha = 0.72f),
                ),
            ),
        )

        val scanTop = size.height * (0.20f + drift * 0.38f)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    colors.primary.copy(alpha = 0.025f),
                    Color.Transparent,
                ),
                startY = scanTop - size.height * 0.12f,
                endY = scanTop + size.height * 0.12f,
            ),
            topLeft = androidx.compose.ui.geometry.Offset(0f, scanTop - size.height * 0.12f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.24f),
        )
    }
}

@Composable
fun BubbleSampleScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}
