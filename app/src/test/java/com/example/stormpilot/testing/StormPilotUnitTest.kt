package com.example.stormpilot.testing

import org.junit.Rule

open class StormPilotUnitTest {
    @get:Rule
    val skipAfterTimeout = SkipAfterTimeoutRule()
}
