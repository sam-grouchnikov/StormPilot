package com.example.stormpilot

import com.example.stormpilot.features.map.data.routing.RoutingParsingTest
import com.example.stormpilot.features.map.presentation.RouteWarningCounterTest
import com.example.stormpilot.data.NwsAlertTest
import com.example.stormpilot.data.RoutingFormattersTest
import com.example.stormpilot.data.RoutingParsingExtendedTest
import com.example.stormpilot.core.AppSettingsTest
import com.example.stormpilot.data.WeatherViewModelTest
import com.example.stormpilot.data.AlertsViewModelTest
import com.example.stormpilot.data.MapsViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExampleUnitTest::class,
    RoutingParsingTest::class,
    RouteWarningCounterTest::class,
    NwsAlertTest::class,
    RoutingFormattersTest::class,
    RoutingParsingExtendedTest::class,
    AppSettingsTest::class,
    WeatherViewModelTest::class,
    AlertsViewModelTest::class,
    MapsViewModelTest::class
)
class AllTestsSuite
