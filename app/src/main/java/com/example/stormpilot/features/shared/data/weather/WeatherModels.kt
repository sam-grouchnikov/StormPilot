package com.example.stormpilot.features.shared.data.weather

data class WeatherSnapshot(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyWeatherOutlook>,
    val stormSpecs: List<StormSpec>,
)

data class CurrentWeather(
    val temperature: Int?,
    val conditions: String,
    val dewPoint: Int?,
    val humidity: Int?,
    val windSpeed: Int?,
    val windGust: Int?,
)

data class HourlyForecast(
    val time: String,
    val temperature: Int?,
    val conditions: String,
    val precipitationChance: Int?,
)

data class DailyWeatherOutlook(
    val day: String,
    val conditions: String,
    val high: Int?,
    val low: Int?,
    val spcOutlook: String,
)

data class StormSpec(
    val label: String,
    val value: String,
    val detail: String,
)
