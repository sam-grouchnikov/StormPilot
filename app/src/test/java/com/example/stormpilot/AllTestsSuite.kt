package com.example.stormpilot

import com.example.stormpilot.features.shared.data.alerts.NwsAlertTest
import com.example.stormpilot.features.shared.viewmodels.AlertsUiStateTest
import com.example.stormpilot.features.shared.data.location.LocationDataTest
import com.example.stormpilot.features.shared.data.routing.RouteWarningCounterTest
import com.example.stormpilot.features.shared.data.routing.RoutingFormattersTest
import com.example.stormpilot.features.shared.data.routing.RoutingParsingTest
import com.example.stormpilot.features.shared.data.routing.RoutingRepositoryImplTest
import com.example.stormpilot.features.shared.data.routing.StormRoutePolicyTest
import com.example.stormpilot.features.shared.data.search.PhotonApiClientTest
import com.example.stormpilot.features.shared.viewmodels.MapNavigationStateTest
import com.example.stormpilot.features.shared.viewmodels.WeatherUiStateTest
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
