package com.example.stormpilot.features.navigation.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.features.navigation.ui.TabDest
import com.example.stormpilot.ui.theme.ExtendedColors

@Composable
fun FloatingNavBar(
    tabs: List<TabDest>,
    currentRoute: String?,
    onTabSelected: (TabDest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val itemOffsets = remember { mutableStateOf(IntArray(tabs.size)) }
    val itemWidths = remember { mutableStateOf(IntArray(tabs.size)) }
    val animatedPillX by animateIntAsState(
        targetValue = itemOffsets.value.getOrElse(selectedIndex) { 0 },
        animationSpec = tween(durationMillis = 150),
        label = "pillX",
    )
    val animatedPillWidth by animateIntAsState(
        targetValue = itemWidths.value.getOrElse(selectedIndex) { 0 },
        animationSpec = tween(durationMillis = 150),
        label = "pillWidth",
    )

    val colors = ExtendedColors()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        Layout(
            modifier = Modifier
                .coloredShadow(
                    color = colors.purpleShadow,
                    blurRadius = 6.dp,
                    spread = 1.dp,
                )
                .clip(CircleShape)
                .background(colors.purpleSurfaceContainer)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            content = {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.purplePrimaryContainer),
                )

                tabs.forEach { tab ->
                    FloatingNavItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = { onTabSelected(tab) },
                    )
                }
            },
        ) { measurables, constraints ->
            val itemMeasurables = measurables.drop(1)
            val pillMeasurable = measurables[0]
            val itemPlaceables = itemMeasurables.map { it.measure(constraints.copy(minWidth = 0)) }
            val totalWidth = itemPlaceables.sumOf { it.width }
            val height = itemPlaceables.maxOf { it.height }

            var xCursor = 0
            val offsets = IntArray(itemPlaceables.size)
            val widths = IntArray(itemPlaceables.size)
            itemPlaceables.forEachIndexed { index, placeable ->
                offsets[index] = xCursor
                widths[index] = placeable.width
                xCursor += placeable.width
            }
            itemOffsets.value = offsets
            itemWidths.value = widths

            val pillPlaceable = pillMeasurable.measure(Constraints.fixed(animatedPillWidth, height))

            layout(totalWidth, height) {
                pillPlaceable.placeRelative(animatedPillX, 0)
                var x = 0
                itemPlaceables.forEach { placeable ->
                    placeable.placeRelative(x, 0)
                    x += placeable.width
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: TabDest,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val unselectedColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp, horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = if (selected) selectedColor else unselectedColor.copy(alpha = 0.55f),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(19.dp),
            )
            Text(
                text = tab.title,
                color = if (selected) selectedColor else unselectedColor.copy(alpha = 0.55f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
