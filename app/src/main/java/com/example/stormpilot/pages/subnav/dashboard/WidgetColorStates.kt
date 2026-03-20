package com.example.stormpilot.pages.subnav.dashboard

import androidx.compose.ui.graphics.Color

data class WarningColorState(val containerColor: Color, val iconColor: Color)

var states = WarningColorStates()
var returned = states.states



public class WarningColorStates {
    public var states = mapOf<String, WarningColorState>(
        "Flash Flood" to WarningColorState(Color(0xFF104129), Color(0xFFA5D396)),
        "Severe Thunderstorm" to WarningColorState(Color(0xFF5D3E10), Color(0xFFF7DE93)),
        "Tornado (Warning)" to WarningColorState(Color(0xFF822424), Color(0xFFFFC2C2))
    )

    public var flashFloodContainer = Color(0xFF104129)
    public var flashFloodContents = Color(0xFFA5D396)
    public var thunderstormContainer = Color(0xFF5D3E10)
    public var thunderstormContents = Color(0xFFF7DE93)

}