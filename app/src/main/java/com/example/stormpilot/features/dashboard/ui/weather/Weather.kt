package com.example.stormpilot.features.dashboard.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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
import com.example.stormpilot.ui.theme.ExtendedColors
import com.example.stormpilot.ui.theme.extendedColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel


import kotlin.math.max
import kotlin.math.roundToInt


private const val HOURLY_FORECAST_AXIS_LABEL_SLOTS = 4


@Composable
private fun dashboardBottomContentPadding() =
    112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()


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
fun HourlyForecastRow(
    hourly: List<HourlyForecast>,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    var selectedMetric by remember { mutableStateOf(HourlyForecastMetric.Temperature) }
    val chartPoints = remember(hourly, selectedMetric) {
        hourly.toHourlyForecastChartPoints(selectedMetric)
    }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(chartPoints, selectedMetric) {
        if (chartPoints.isNotEmpty()) {
            modelProducer.runTransaction {
                lineModel {
                    series(
                        x = chartPoints.indices.map { it },
                        y = chartPoints.map { it.value },
                        key = selectedMetric.name,
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedMetric.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selectedMetric.subtitle(chartPoints),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                HourlyMetricToggle(
                    selectedMetric = selectedMetric,
                    onMetricSelected = { selectedMetric = it },
                )
            }

            if (chartPoints.isEmpty()) {
                HourlyForecastGraphEmpty(selectedMetric)
            } else {
                HourlyForecastLineChart(
                    modelProducer = modelProducer,
                    metric = selectedMetric,
                    points = chartPoints,
                    pointContainerColor = containerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )
                HourlyForecastGraphStats(
                    metric = selectedMetric,
                    points = chartPoints,
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastLineChart(
    modelProducer: CartesianChartModelProducer,
    metric: HourlyForecastMetric,
    points: List<HourlyForecastChartPoint>,
    pointContainerColor: Color,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val lineColor = metric.chartLineColor(MaterialTheme.extendedColors)
    val areaBrush = Brush.verticalGradient(
        0.0f to lineColor.copy(alpha = 0.34f),
        0.62f to lineColor.copy(alpha = 0.12f),
        1.0f to Color.Transparent,
    )
    val rangeProvider = remember(points, metric) {
        metric.rangeProvider(points)
    }
    val labelSpacing = remember(points) {
        max(1, points.lastIndex / HOURLY_FORECAST_AXIS_LABEL_SLOTS)
    }
    val firstLabelOffset = remember(points) {
        if (points.size > 1 && points.first().label.equals("Now", ignoreCase = true)) 1 else 0
    }
    val xValueFormatter = remember(points) {
        CartesianValueFormatter { _, value, _ ->
            points[value.roundToInt().coerceIn(points.indices)].label.ifBlank { "--" }
        }
    }
    val yValueFormatter = remember(metric) {
        CartesianValueFormatter { _, value, _ ->
            metric.formatValue(value.roundToInt())
        }
    }
    val axisLabel = rememberTextComponent(
        style = TextStyle(
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
    val horizontalGuideline = rememberLineComponent(
        fill = Fill(colorScheme.outlineVariant.copy(alpha = 0.18f)),
        thickness = 1.dp,
    )
    val pointComponent = rememberShapeComponent(
        fill = Fill(pointContainerColor),
        shape = CircleShape,
        strokeFill = Fill(lineColor),
        strokeThickness = 2.dp,
    )
    val pointProvider = remember(pointComponent) {
        LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                pointComponent,
                size = 7.dp,
            )
        )
    }
    val chartLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(
            thickness = 3.dp,
            cap = StrokeCap.Round,
        ),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(brush = areaBrush)),
        pointProvider = pointProvider,
        interpolator = LineCartesianLayer.Interpolator.cubic(curvature = 0.28f),
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(chartLine),
                pointSpacing = 22.dp,
                rangeProvider = rangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                line = null,
                tick = null,
                guideline = horizontalGuideline,
                label = axisLabel,
                valueFormatter = yValueFormatter,
                itemPlacer = remember {
                    VerticalAxis.ItemPlacer.count(count = { 4 }, shiftTopLines = false)
                },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = null,
                tick = null,
                guideline = null,
                label = axisLabel,
                valueFormatter = xValueFormatter,
                itemPlacer = remember(labelSpacing, firstLabelOffset) {
                    HorizontalAxis.ItemPlacer.aligned(
                        spacing = { labelSpacing },
                        offset = { firstLabelOffset },
                        shiftExtremeLines = false,
                    )
                },
            ),
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier,
    )
}


@Composable
private fun HourlyMetricToggle(
    selectedMetric: HourlyForecastMetric,
    onMetricSelected: (HourlyForecastMetric) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
    val activeColor = metric.chartLineColor(MaterialTheme.extendedColors)

    val activeContainerColor = if (metric == HourlyForecastMetric.Temperature) activeColor.copy(alpha = 0.24f) else colorScheme.secondaryContainer

    Row(
        modifier = modifier
            .height(32.dp)
            .background(
                color = if (selected) activeContainerColor else Color.Transparent,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = if (selected) activeColor else colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = metric.tabLabel,
            color = if (selected) activeColor else colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
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
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp),
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
                tint = MaterialTheme.extendedColors.theme.vibrantPrimary,
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

private fun List<HourlyForecast>.toHourlyForecastChartPoints(
    metric: HourlyForecastMetric,
): List<HourlyForecastChartPoint> =
    mapNotNull { forecast ->
        metric.valueFor(forecast)?.let { value ->
            HourlyForecastChartPoint(
                label = forecast.time.ifBlank { "--" },
                value = metric.normalizeValue(value),
            )
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

    fun normalizeValue(value: Int): Int =
        when (this) {
            Temperature -> value
            PrecipitationChance -> value.coerceIn(0, 100)
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

    fun chartLineColor(colors: ExtendedColors): Color =
        colors.theme.vibrantPrimary

    fun rangeProvider(points: List<HourlyForecastChartPoint>): CartesianLayerRangeProvider {
        val values = points.map { it.value }
        val minX = 0.0
        val maxX = points.lastIndex.coerceAtLeast(1).toDouble()

        return when (this) {
            Temperature -> {
                val low = values.min()
                val high = values.max()
                val spread = high - low
                val padding = if (spread == 0) 4 else max(3, (spread * 0.22f).roundToInt())

                CartesianLayerRangeProvider.fixed(
                    minX = minX,
                    maxX = maxX,
                    minY = (low - padding).toDouble(),
                    maxY = (high + padding).toDouble(),
                )
            }
            PrecipitationChance -> {
                val peak = values.max()
                val maxY = when {
                    peak <= 20 -> 40
                    peak <= 50 -> 70
                    else -> 100
                }

                CartesianLayerRangeProvider.fixed(
                    minX = minX,
                    maxX = maxX,
                    minY = 0.0,
                    maxY = maxY.toDouble(),
                )
            }
        }
    }
}

private data class HourlyForecastChartPoint(
    val label: String,
    val value: Int,
)

@Composable
fun FiveDayOutlook(
    daily: List<DailyWeatherOutlook>,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
    ) {
        Column(modifier = Modifier.padding(vertical = 5.dp)) {
            daily.forEachIndexed { index, outlook ->
                DailyOutlookRow(outlook = outlook)
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
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getWeatherIconForCondition(outlook.conditions),
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.theme.vibrantPrimary,
                    modifier = Modifier.size(30.dp),
                )
            }
        Spacer(modifier = Modifier.width(20.dp))
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
        Surface(
            shape = CircleShape,
            color = if (AppSettings.isDarkMode) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ) {
            Text(
                text = outlookSPC,
                color = badgeContent.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun outlookRiskContainer(outlook: String): Color {
    val colors = MaterialTheme.extendedColors
    return when {
        outlook.contains("High", ignoreCase = true) -> colors.weather.outlookHighContainer.copy(alpha = 0.6f)
        outlook.contains("Moderate", ignoreCase = true) -> colors.weather.outlookModerateContainer.copy(alpha = 0.6f)
        outlook.contains("Enhanced", ignoreCase = true) -> if (AppSettings.isDarkMode) colors.weather.outlookEnhancedContainer.copy(alpha = 0.8f) else colors.weather.outlookEnhancedContainer.copy(alpha = 0.6f)
        outlook.contains("Slight", ignoreCase = true) -> colors.weather.outlookSlightContainer.copy(alpha = 0.6f)
        outlook.contains("Marginal", ignoreCase = true) -> colors.weather.outlookMarginalContainer.copy(alpha = 0.5f)
        outlook.contains("TSTM", ignoreCase = true) -> colors.weather.outlookThunderstormContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
}

@Composable
private fun outlookRiskContent(outlook: String): Color {
    val colors = MaterialTheme.extendedColors
    return when {
        outlook.contains("High", ignoreCase = true) -> colors.weather.outlookHighContent
        outlook.contains("Moderate", ignoreCase = true) -> colors.weather.outlookModerateContent
        outlook.contains("Enhanced", ignoreCase = true) -> colors.weather.outlookEnhancedContent
        outlook.contains("Slight", ignoreCase = true) -> colors.weather.outlookSlightContent
        outlook.contains("Marginal", ignoreCase = true) -> colors.weather.outlookMarginalContent
        outlook.contains("TSTM", ignoreCase = true) -> colors.weather.outlookThunderstormContent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyWeatherCard(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
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
