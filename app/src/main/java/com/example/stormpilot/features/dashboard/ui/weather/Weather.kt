package com.example.stormpilot.features.dashboard.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.stormpilot.core.AppSettings
import com.example.stormpilot.features.shared.data.weather.CurrentWeather
import com.example.stormpilot.features.shared.data.weather.DailyWeatherOutlook
import com.example.stormpilot.features.shared.data.weather.HourlyForecast
import com.example.stormpilot.features.shared.viewmodels.WeatherUiState
import com.example.stormpilot.features.shared.viewmodels.WeatherViewModel
import com.example.stormpilot.features.dashboard.ui.ForecastSectionTitle
import com.example.stormpilot.ui.theme.extendedColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel


import kotlin.math.roundToInt


@Composable
fun Weather(
    title: String,
    weatherViewModel: WeatherViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val cityName by weatherViewModel.cityName.collectAsStateWithLifecycle()
    val useDashboardPlaceholderData = AppSettings.useDashboardPlaceholderData
    val dashboardPlaceholderAssetName = AppSettings.dashboardPlaceholderWeatherAssetName

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted && !useDashboardPlaceholderData) {
            weatherViewModel.locationRepository.startTracking()
        }
    }

    LaunchedEffect(useDashboardPlaceholderData, dashboardPlaceholderAssetName) {
        weatherViewModel.setDashboardPlaceholderSource(
            enabled = useDashboardPlaceholderData,
            assetName = dashboardPlaceholderAssetName,
        )
    }

    LaunchedEffect(useDashboardPlaceholderData) {
        if (useDashboardPlaceholderData) return@LaunchedEffect

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

    WeatherOverviewSection(
        cityName = cityName,
        uiState = uiState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = dashboardBottomContentPadding()),
    )
}

@Composable
fun WeatherOverviewSection(
    cityName: String,
    uiState: WeatherUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
//        CurrentWeatherCard(cityName = cityName, current = uiState.current, isLoading = uiState.isLoading)

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
private fun HourlyForecastRow(hourly: List<HourlyForecast>) {
    if (hourly.isEmpty()) {
        EmptyWeatherCard("Hourly forecast will appear once your location loads.")
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineModel { series(2, 4, 3, 5, 7, 6, 8) }
        }
    }

    ComposeBasicLineChart(modelProducer)
}

@SuppressLint("SuspiciousIndentation")
@Composable
private fun ComposeBasicLineChart(
    modelProducer: CartesianChartModelProducer,
        modifier: Modifier = Modifier,
    ) {
    val customBrush = Brush.verticalGradient(
        0.0f to Color.Red.copy(alpha = 0.3f),
        0.3f to Color.Yellow.copy(alpha = 0.3f),
        0.7f to Color.Green.copy(alpha = 0.3f),
        1.0f to Color.Blue.copy(alpha = 0.3f)
    )
    val customRangeProvider = CartesianLayerRangeProvider.fixed(
        minX = 0.0,
        maxX = 10.0,
        minY = 0.0,
        maxY = 10.0
    )


        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                // 1. Fill style for the line itself
                                fill = LineCartesianLayer.LineFill.single(Fill(Color.White)),
                                // 2. Fill style for the area below the line
                                areaFill = LineCartesianLayer.AreaFill.single(
                                    Fill(brush = customBrush)
                                )

                            )
                        ),
                        rangeProvider = customRangeProvider
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
            modelProducer = modelProducer,
            modifier = modifier,
        )
    }


@Composable
private fun HourlyMetricToggle(
    selectedMetric: HourlyForecastMetric,
    onMetricSelected: (HourlyForecastMetric) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier
                .width(164.dp)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            HourlyMetricToggleOption(
                metric = HourlyForecastMetric.Temperature,
                selected = selectedMetric == HourlyForecastMetric.Temperature,
                onClick = { onMetricSelected(HourlyForecastMetric.Temperature) },
                modifier = Modifier.weight(1f),
            )
            HourlyMetricToggleOption(
                metric = HourlyForecastMetric.PrecipitationChance,
                selected = selectedMetric == HourlyForecastMetric.PrecipitationChance,
                onClick = { onMetricSelected(HourlyForecastMetric.PrecipitationChance) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HourlyMetricToggleOption(
    metric: HourlyForecastMetric,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(32.dp)
            .background(
                color = if (selected) colorScheme.surfaceContainerHigh else Color.Transparent,
                shape = RoundedCornerShape(11.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = if (selected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = metric.tabLabel,
            color = if (selected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun HourlyForecastGraphStats(
    metric: HourlyForecastMetric,
    points: List<HourlyForecastChartPoint>,
) {
    val values = points.map { it.value }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HourlyForecastGraphStat(
            label = "Now",
            value = metric.formatValue(values.first()),
            modifier = Modifier.weight(1f),
        )
        HourlyForecastGraphStat(
            label = metric.peakLabel,
            value = metric.formatValue(values.max()),
            modifier = Modifier.weight(1f),
        )
        HourlyForecastGraphStat(
            label = metric.lowLabel,
            value = metric.formatValue(metric.lowStatValue(values)),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HourlyForecastGraphStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun HourlyForecastGraphEmpty(metric: HourlyForecastMetric) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = metric.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "No ${metric.emptyStateLabel} data available.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private enum class HourlyForecastMetric(
    val tabLabel: String,
    val title: String,
    val emptyStateLabel: String,
    val peakLabel: String,
    val lowLabel: String,
    val icon: ImageVector,
) {
    Temperature(
        tabLabel = "Temp",
        title = "Temperature Trend",
        emptyStateLabel = "temperature",
        peakLabel = "High",
        lowLabel = "Low",
        icon = Icons.Outlined.WbSunny,
    ),
    PrecipitationChance(
        tabLabel = "Precip",
        title = "Precipitation Chance",
        emptyStateLabel = "precipitation",
        peakLabel = "Peak",
        lowLabel = "Avg",
        icon = Icons.Outlined.WaterDrop,
    );

    fun valueFor(forecast: HourlyForecast): Int? =
        when (this) {
            Temperature -> forecast.temperature
            PrecipitationChance -> forecast.precipitationChance
        }

    fun formatValue(value: Int): String =
        when (this) {
            Temperature -> "$value°"
            PrecipitationChance -> "$value%"
        }

    fun subtitle(points: List<HourlyForecastChartPoint>): String =
        if (points.isEmpty()) {
            "Waiting for ${emptyStateLabel} data"
        } else {
            "${points.first().label} to ${points.last().label}"
        }

    fun lowStatValue(values: List<Int>): Int =
        when (this) {
            Temperature -> values.min()
            PrecipitationChance -> values.average().roundToInt()
        }
}

private data class HourlyForecastChartPoint(
    val label: String,
    val value: Int,
)

@Composable
private fun FiveDayOutlook(daily: List<DailyWeatherOutlook>) {
    if (daily.isEmpty()) {
        EmptyWeatherCard("5 day outlook will appear once your location loads.")
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getWeatherIconForCondition(outlook.conditions),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
    val colors = MaterialTheme.extendedColors
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
    val colors = MaterialTheme.extendedColors
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
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
