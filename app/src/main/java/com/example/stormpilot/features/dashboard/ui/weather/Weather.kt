package com.example.stormpilot.features.dashboard.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.features.shared.data.weather.CurrentWeather
import com.example.stormpilot.features.shared.data.weather.DailyWeatherOutlook
import com.example.stormpilot.features.shared.data.weather.HourlyForecast
import com.example.stormpilot.features.shared.viewmodels.WeatherUiState
import com.example.stormpilot.features.shared.viewmodels.WeatherViewModel
import com.example.stormpilot.ui.theme.ExtendedColors

@Composable
fun Weather(
    title: String,
    weatherViewModel: WeatherViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val cityName by weatherViewModel.cityName.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) weatherViewModel.locationRepository.startTracking()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            weatherViewModel.locationRepository.startTracking()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    WeatherContent(cityName = cityName, uiState = uiState)
}

@Composable
private fun WeatherContent(
    cityName: String,
    uiState: WeatherUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = dashboardBottomContentPadding()),

        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CurrentWeatherCard(cityName = cityName, current = uiState.current, isLoading = uiState.isLoading)

        ForecastSectionTitle("5 Day Outlook")
        FiveDayOutlook(daily = uiState.daily)

        ForecastSectionTitle("Hourly Forecast")
        HourlyForecastRow(hourly = uiState.hourly)


    }
}

@Composable
private fun dashboardBottomContentPadding() =
    112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CurrentWeatherCard(
    cityName: String,
    current: CurrentWeather?,
    isLoading: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.primaryContainer.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, colorScheme.onPrimaryContainer.copy(alpha = 0.10f)),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = colorScheme.surface.copy(alpha = 0.42f),
                    border = BorderStroke(1.dp, colorScheme.onPrimaryContainer.copy(alpha = 0.10f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(17.dp),
                        )
                        Text(
                            text = cityName,
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 6.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (isLoading) {
                    LoadingIndicator(modifier = Modifier.size(34.dp))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = current?.temperature,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                        label = "current_temperature",
                    ) { temperature ->
                        Text(
                            text = temperature?.let { "$it°" } ?: "--°",
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Black,
                            fontSize = 64.sp,
                            lineHeight = 66.sp,
                        )
                    }
                    Text(
                        text = current?.conditions ?: "Waiting on Weather",
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 21.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colorScheme.surface.copy(alpha = 0.32f),
                    border = BorderStroke(1.dp, colorScheme.onPrimaryContainer.copy(alpha = 0.10f)),
                ) {
                    AnimatedWeatherIcon(
                        condition = current?.conditions ?: "",
                        modifier = Modifier
                            .size(94.dp)
                            .padding(10.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                CurrentWeatherPill("Humidity", current?.humidity?.let { "$it%" } ?: "--")
                CurrentWeatherPill("Dew Point", current?.dewPoint?.let { "$it°" } ?: "--")
                CurrentWeatherPill("Wind", current?.windSpeed?.let { "$it mph" } ?: "--")
            }
        }
    }
}

@Composable
private fun CurrentWeatherPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ForecastSectionTitle(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
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

@Composable
private fun HourlyForecastRow(hourly: List<HourlyForecast>) {
    if (hourly.isEmpty()) {
        EmptyWeatherCard("Hourly forecast will appear once your location loads.")
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(hourly) { forecast ->
            HourlyForecastCard(forecast = forecast)
        }
    }
}

@Composable
private fun HourlyForecastCard(forecast: HourlyForecast) {
    val transition = rememberInfiniteTransition(label = "hourly_card")
    val lift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hourly_card_lift",
    )
    Surface(
        modifier = Modifier.width(106.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.34f + lift * 0.08f),
                        ),
                    ),
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = forecast.time,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
            Icon(
                imageVector = getWeatherIconForCondition(forecast.conditions),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(29.dp),
            )
            Text(
                text = forecast.temperature?.let { "$it°" } ?: "--°",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
            )
            Text(
                text = forecast.conditions,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(14.dp)
                        .height(14.dp),
                )
                Text(
                    text = forecast.precipitationChance?.let { "$it%" } ?: "--",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun FiveDayOutlook(daily: List<DailyWeatherOutlook>) {
    if (daily.isEmpty()) {
        EmptyWeatherCard("5 day outlook will appear once your location loads.")
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(vertical = 5.dp)) {
            daily.forEachIndexed { index, outlook ->
                DailyOutlookRow(outlook = outlook)
                if (index != daily.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyOutlookRow(outlook: DailyWeatherOutlook) {
    val outlookSPC = if (outlook.spcOutlook.startsWith("General")) "TSTMs" else outlook.spcOutlook
    val badgeColor = outlookRiskContainer(outlookSPC)
    val badgeContent = outlookRiskContent(outlookSPC)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f),
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getWeatherIconForCondition(outlook.conditions),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = outlook.day,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
            )
            Text(
                text = outlook.conditions,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            )
        }
        Text(
            text = "${outlook.high?.toString() ?: "--"}°/${outlook.low?.toString() ?: "--"}°",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Surface(
            shape = CircleShape,
            color = badgeColor,
            border = BorderStroke(1.dp, badgeContent.copy(alpha = 0.18f)),
        ) {
            Text(
                text = outlookSPC,
                color = badgeContent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun outlookRiskContainer(outlook: String): Color {
    val colors = ExtendedColors()
    return when {
        outlook.contains("High", ignoreCase = true) -> colors.alertWarningContainer
        outlook.contains("Moderate", ignoreCase = true) -> colors.alertWarningContainer.copy(alpha = 0.78f)
        outlook.contains("Enhanced", ignoreCase = true) -> colors.alertWatchContainer
        outlook.contains("Slight", ignoreCase = true) -> colors.alertWatchContainer.copy(alpha = 0.76f)
        outlook.contains("Marginal", ignoreCase = true) -> MaterialTheme.colorScheme.tertiaryContainer
        outlook.contains("TSTM", ignoreCase = true) -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
}

@Composable
private fun outlookRiskContent(outlook: String): Color {
    val colors = ExtendedColors()
    return when {
        outlook.contains("High", ignoreCase = true) -> colors.alertWarningContent
        outlook.contains("Moderate", ignoreCase = true) -> colors.alertWarningContent
        outlook.contains("Enhanced", ignoreCase = true) -> colors.alertWatchContent
        outlook.contains("Slight", ignoreCase = true) -> colors.alertWatchContent
        outlook.contains("Marginal", ignoreCase = true) -> MaterialTheme.colorScheme.onTertiaryContainer
        outlook.contains("TSTM", ignoreCase = true) -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyWeatherCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearWavyProgressIndicator(
                waveSpeed = 1.dp,
                wavelength = 50.dp,
                gapSize = 5.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun getWeatherIconForCondition(condition: String): ImageVector {
    return when (condition) {
        "Clear", "Mainly clear" -> Icons.Outlined.WbSunny
        "Partly cloudy", "Overcast", "Fog" -> Icons.Outlined.Cloud
        "Drizzle", "Freezing drizzle", "Rain", "Freezing rain", "Rain showers" -> Icons.Outlined.WaterDrop
        "Snow", "Snow grains", "Snow showers" -> Icons.Outlined.AcUnit
        "Thunderstorms", "Severe storms" -> Icons.Outlined.Thunderstorm
        else -> Icons.Outlined.WbSunny
    }
}
