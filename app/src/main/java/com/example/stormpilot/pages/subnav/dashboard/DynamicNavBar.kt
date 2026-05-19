package com.example.stormpilot.pages.subnav.dashboard

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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.stormpilot.genai.GenAIViewModel
import com.example.stormpilot.pages.subnav.dashboard.pages.AIChat
import com.example.stormpilot.pages.subnav.dashboard.pages.Alerts
import com.example.stormpilot.pages.subnav.dashboard.pages.Location
import kotlinx.coroutines.launch

data class BubbleNavigationItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernBubbleNavBarScreen(genAIViewModel: GenAIViewModel) {
    val items = listOf(
        BubbleNavigationItem("Location", Icons.Outlined.LocationOn),
        BubbleNavigationItem("Alerts", Icons.Outlined.WarningAmber),
        BubbleNavigationItem("AI Chat", Icons.Outlined.AutoAwesome)
    )

    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    // Use targetPage during swipe gestures for a leading highlight effect;
    // fall back to currentPage when no animation is in progress.
    val selectedIndex = if (pagerState.isScrollInProgress) {
        pagerState.targetPage
    } else {
        pagerState.currentPage
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            // TabRow instead of ScrollableTabRow → tabs are equally spaced / centered
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                // Hide the default ink indicator; we use bubble backgrounds instead
                indicator = {},
                divider = {}
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    // Spring-based color animation for a more physical, responsive feel
                    val bubbleBackgroundColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "BubbleBackground"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "ContentColor"
                    )

                    // Subtle scale pop when a tab becomes active
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
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
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

                            // Horizontal expand/shrink instead of plain fade for a slicker reveal
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

        // --- SWIPABLE PAGES ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> Location(title = "Location Content Screen")
                1 -> Alerts(title = "Alerts Content Screen")
                2 -> AIChat(title = "AI Chat Content Screen", genAIViewModel)
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