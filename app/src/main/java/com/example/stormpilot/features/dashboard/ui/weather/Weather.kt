package com.example.stormpilot.features.dashboard.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stormpilot.data.CurrentWeather
import com.example.stormpilot.data.DailyWeatherOutlook
import com.example.stormpilot.data.HourlyForecast
import com.example.stormpilot.data.WeatherUiState
import com.example.stormpilot.data.WeatherViewModel
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
            .padding(horizontal = 15.dp)
            .verticalScroll(rememberScrollState()),

    verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CurrentWeatherCard(cityName = cityName, current = uiState.current, isLoading = uiState.isLoading)

        ForecastSectionTitle("5 Day Outlook")
        FiveDayOutlook(daily = uiState.daily)

        ForecastSectionTitle("Hourly Forecast")
        HourlyForecastRow(hourly = uiState.hourly)


    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CurrentWeatherCard(
    cityName: String,
    current: CurrentWeather?,
    isLoading: Boolean,
) {
    val colors = ExtendedColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.blueBackground,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 25.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = cityName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.weight(1f)) {
                    if(isLoading)  {
                        LoadingIndicator()
                    }
                    Text(
                        text = current?.let { "${it.temperature}°" } ?: "",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 54.sp,
                        lineHeight = 56.sp,
                        modifier = Modifier.padding(5.dp)
                    )
                    Text(
                        text = current?.conditions ?: "Waiting on Weather",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 10.dp),
                    )

                }


                AnimatedWeatherIcon(
                    condition = current?.conditions ?: "",
                    modifier = Modifier.padding(bottom = 4.dp, end = 12.dp)
                )
            }

        }
    }
}

@Composable
private fun CurrentWeatherPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ForecastSectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 2.dp),
    )
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
    Surface(
        modifier = Modifier.width(104.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
            )
            Text(
                text = "${forecast.temperature}°",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
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
    val colors = ExtendedColors()
    if (daily.isEmpty()) {
        EmptyWeatherCard("5 day outlook will appear once your location loads.")
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            daily.forEachIndexed { index, outlook ->
                DailyOutlookRow(outlook = outlook)
                if (index != daily.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyOutlookRow(outlook: DailyWeatherOutlook) {
    val outlookSPC = if (outlook.spcOutlook.startsWith("General")) "TSTMs" else outlook.spcOutlook
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = outlook.day,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
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
            text = "${outlook.high}°/${outlook.low}°",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(126.dp)
                .background(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(10.dp))
        ) {
            Text(
                text = outlookSPC,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            )
        }
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
