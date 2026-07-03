package com.example.stormpilot.features.map.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stormpilot.ui.theme.extendedColors


@Composable
fun StormAiSearchCard(
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.extendedColors
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            colors.search.stormAiGradientStart.copy(alpha = 0.25f),
            colors.search.stormAiGradientEnd.copy(alpha = 0.25f),
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(16.dp),
                color = colors.search.stormAiOutline
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(9.dp)
                        .size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Use StormPilot AI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


@Composable
fun GasSearchCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.extendedColors
    PlaceSearchCard(
        label = "Gas",
        icon = Icons.Rounded.LocalGasStation,
        containerColor = colors.search.gasContainer,
        outlineColor = colors.search.gasOutline,
        contentColor = colors.search.gasContent,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun HotelSearchCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.extendedColors
    PlaceSearchCard(
        label = "Hotel",
        icon = Icons.Rounded.Hotel,
        containerColor = colors.search.hotelContainer,
        outlineColor = colors.search.hotelOutline,
        contentColor = colors.search.hotelContent,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun GrocerySearchCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.extendedColors
    PlaceSearchCard(
        label = "Grocery",
        icon = Icons.Rounded.LocalGroceryStore,
        containerColor = colors.search.groceryContainer,
        outlineColor = colors.search.groceryOutline,
        contentColor = colors.search.groceryContent,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun PlaceSearchCard(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    outlineColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(16.dp),
                color = outlineColor,
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.14f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(17.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
