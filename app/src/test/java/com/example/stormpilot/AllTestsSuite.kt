package com.example.stormpilot

import com.example.stormpilot.features.alerts.data.NwsAlertTest
import com.example.stormpilot.features.alerts.presentation.AlertsUiStateTest
import com.example.stormpilot.features.location.data.LocationDataTest
import com.example.stormpilot.features.map.data.routing.RouteWarningCounterTest
import com.example.stormpilot.features.map.data.routing.RoutingFormattersTest
import com.example.stormpilot.features.map.data.routing.RoutingParsingTest
import com.example.stormpilot.features.map.data.routing.RoutingRepositoryImplTest
import com.example.stormpilot.features.map.data.routing.StormRoutePolicyTest
import com.example.stormpilot.features.map.data.search.PhotonApiClientTest
import com.example.stormpilot.features.map.presentation.MapNavigationStateTest
import com.example.stormpilot.features.weather.presentation.WeatherUiStateTest
import com.example.stormpilot.testing.SkipAfterTimeoutRuleTest
import com.example.stormpilot.ui.theme.ExtendedColorsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    NwsAlertTest::class,
    AlertsUiStateTest::class,
    LocationDataTest::class,
    RouteWarningCounterTest::class,
    RoutingFormattersTest::class,
    RoutingParsingTest::class,
    RoutingRepositoryImplTest::class,
    StormRoutePolicyTest::class,
    PhotonApiClientTest::class,
    MapNavigationStateTest::class,
    WeatherUiStateTest::class,
    SkipAfterTimeoutRuleTest::class,
    ExtendedColorsTest::class,
)
class AllTestsSuite
