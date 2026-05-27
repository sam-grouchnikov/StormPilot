package com.example.stormpilot.features.dashboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.dashboard.ui.weather.Weather
import com.example.stormpilot.features.dashboard.ui.alerts.AlertSlide
import com.example.stormpilot.ui.theme.ExtendedColors
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


    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 58.dp)) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 0.dp),
            tonalElevation = 0.dp
        ) {
            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = Color.Transparent,
                indicator = {},
                divider = {},
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val isOpenPageGreyedOut = showChat && index == selectedPageIndex && index != chatIndex

                    val bubbleBackgroundColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            isOpenPageGreyedOut -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "BubbleBackground"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            isOpenPageGreyedOut -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "ContentColor"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.95f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "BubbleScale"
                    )

                    Tab(
                        selected = isSelected,
                        onClick = {
                            if (index == chatIndex) {
                                onChatClick()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(bubbleBackgroundColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = contentColor
                            )

                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                ) + expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    expandFrom = Alignment.Start
                                ),
                                exit = fadeOut(
                                    animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                ) + shrinkHorizontally(
                                    animationSpec = spring(stiffness = Spring.StiffnessHigh),
                                    shrinkTowards = Alignment.Start
                                )
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp)
        ) { page ->
            when (page) {
                0 -> AlertSlide(title = "Location Content Screen")
                1 -> Weather(title = "Alerts Content Screen")
            }
        }
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
