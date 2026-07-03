package com.example.stormpilot.features.dashboard.ui.weather

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.stormpilot.ui.theme.extendedColors

@Composable
fun AnimatedWeatherIcon(condition: String, modifier: Modifier = Modifier) {
    when (condition) {
        "Clear", "Mainly clear" -> AnimatedSunnyIcon(modifier)
        "Partly cloudy", "Overcast", "Fog" -> AnimatedCloudyIcon(modifier)
        "Drizzle", "Freezing drizzle", "Rain", "Freezing rain", "Rain showers" -> AnimatedRainyIcon(modifier)
        "Snow", "Snow grains", "Snow showers" -> AnimatedSnowyIcon(modifier)
        "Thunderstorms", "Severe storms" -> AnimatedStormyIcon(modifier)
        "Sunny" -> AnimatedSunnyIcon(modifier)
        else -> null
    }
}

@Composable
fun AnimatedSunnyIcon(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extendedColors.weather
    val infiniteTransition = rememberInfiniteTransition(label = "sunny_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunny_rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunny_scale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.WbSunny,
            contentDescription = "Sunny",
            tint = colors.sunIcon,
            modifier = Modifier
                .size(64.dp)
                .rotate(rotation)
                .scale(scale)
        )
    }
}

@Composable
fun AnimatedCloudyIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloudy_transition")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudy_offset"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Cloud,
            contentDescription = "Cloudy",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .size(48.dp)
                .offset(x = (offsetX - 10).dp, y = (-10).dp)
        )
        Icon(
            imageVector = Icons.Outlined.Cloud,
            contentDescription = "Cloudy",
            tint = MaterialTheme.extendedColors.theme.vibrantPrimary,
            modifier = Modifier
                .size(64.dp)
                .offset(x = offsetX.dp)
        )
    }
}

@Composable
fun AnimatedRainyIcon(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extendedColors.weather
    val infiniteTransition = rememberInfiniteTransition(label = "rainy_transition")
    
    // Rain drop animations
    val drop1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drop1_y"
    )
    val drop2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing, delayMillis = 300),
            repeatMode = RepeatMode.Restart
        ),
        label = "drop2_y"
    )
    val drop3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing, delayMillis = 600),
            repeatMode = RepeatMode.Restart
        ),
        label = "drop3_y"
    )

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        // Rain drops
        Icon(
            imageVector = Icons.Outlined.WaterDrop,
            contentDescription = null,
            tint = colors.rainIcon,
            modifier = Modifier
                .size(20.dp)
                .offset(x = (-12).dp, y = (20 + drop1Y).dp)
                .alpha(1f - (drop1Y / 30f))
        )
        Icon(
            imageVector = Icons.Outlined.WaterDrop,
            contentDescription = null,
            tint = colors.rainIcon,
            modifier = Modifier
                .size(20.dp)
                .offset(x = 0.dp, y = (25 + drop2Y).dp)
                .alpha(1f - (drop2Y / 30f))
        )
        Icon(
            imageVector = Icons.Outlined.WaterDrop,
            contentDescription = null,
            tint = colors.rainIcon,
            modifier = Modifier
                .size(20.dp)
                .offset(x = 12.dp, y = (20 + drop3Y).dp)
                .alpha(1f - (drop3Y / 30f))
        )
        
        // Cloud on top
        Icon(
            imageVector = Icons.Outlined.Cloud,
            contentDescription = "Rainy",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun AnimatedSnowyIcon(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extendedColors.weather
    val infiniteTransition = rememberInfiniteTransition(label = "snowy_transition")
    
    // Snow flake animations (spin + fall)
    val fall1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow1_y"
    )
    val fall2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow2_y"
    )
    
    val rot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow1_rot"
    )
    val rot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow2_rot"
    )

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        // Snowflakes
        Icon(
            imageVector = Icons.Outlined.AcUnit,
            contentDescription = null,
            tint = colors.snowIcon,
            modifier = Modifier
                .size(18.dp)
                .offset(x = (-10).dp, y = (20 + fall1Y).dp)
                .rotate(rot1)
                .alpha(1f - (fall1Y / 30f))
        )
        Icon(
            imageVector = Icons.Outlined.AcUnit,
            contentDescription = null,
            tint = colors.snowIcon,
            modifier = Modifier
                .size(18.dp)
                .offset(x = 10.dp, y = (25 + fall2Y).dp)
                .rotate(rot2)
                .alpha(1f - (fall2Y / 30f))
        )
        
        // Cloud on top
        Icon(
            imageVector = Icons.Outlined.Cloud,
            contentDescription = "Snowy",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun AnimatedStormyIcon(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extendedColors.weather
    val infiniteTransition = rememberInfiniteTransition(label = "stormy_transition")
    
    // Lightning flash
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing, delayMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightning_flash"
    )

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        // Lightning bolt
        Icon(
            imageVector = Icons.Outlined.Thunderstorm,
            contentDescription = "Stormy",
            tint = colors.lightningIcon.copy(alpha = if (flashAlpha > 0.5f) 1f else 0.4f),
            modifier = Modifier.size(64.dp)
        )
    }
}
