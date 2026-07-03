package com.example.stormpilot.features.navigation.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.stormpilot.ui.theme.extendedColors

@Composable
fun FloatingNavBar(
    tabs: List<TabDest>,
    currentRoute: String?,
    onTabSelected: (TabDest) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
) {
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val itemOffsets = remember { mutableStateOf(IntArray(tabs.size)) }
    val itemWidths = remember { mutableStateOf(IntArray(tabs.size)) }

    val selectionTransition = updateTransition(
        targetState = selectedIndex,
        label = "navSelection",
    )
    val animatedPillX by selectionTransition.animateInt(
        transitionSpec = { tween(durationMillis = 110, easing = FastOutSlowInEasing) },
        label = "pillX",
    ) { index ->
        itemOffsets.value.getOrElse(index) { 0 }
    }
    val animatedPillWidth by selectionTransition.animateInt(
        transitionSpec = { tween(durationMillis = 110, easing = FastOutSlowInEasing) },
        label = "pillWidth",
    ) { index ->
        itemWidths.value.getOrElse(index) { 0 }
    }

    val colors = MaterialTheme.extendedColors

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Layout(
            modifier = Modifier
                .coloredShadow(
                    color = colors.navigation.floatingControlShadow,
                    blurRadius = 6.dp,
                    spread = 1.dp,
                )
                .clip(CircleShape)
                .background(colors.navigation.barSurface)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            content = {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.navigation.selectedPillContainer),
                )

                tabs.forEachIndexed { index, tab ->
                    FloatingNavItem(
                        tab = tab,
                        index = index,
                        selectionTransition = selectionTransition,
                        onClick = { onTabSelected(tab) },
                        showLabel = showLabels,
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
    index: Int,
    selectionTransition: Transition<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val contentColor by selectionTransition.animateColor(
        transitionSpec = { tween(durationMillis = 110, easing = FastOutSlowInEasing) },
        label = "${tab.route}ContentColor",
    ) { selectedIndex ->
        if (selectedIndex == index) selectedColor else unselectedColor
    }

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
                tint = contentColor,
                modifier = Modifier
                    .padding(end = if (showLabel) 4.dp else 0.dp)
                    .size(19.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            if (true) {
                Text(
                    text = tab.title,
                    color = contentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}
