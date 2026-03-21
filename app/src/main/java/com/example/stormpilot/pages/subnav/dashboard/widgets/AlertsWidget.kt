package com.example.stormpilot.pages.subnav.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Flood
import androidx.compose.material.icons.outlined.Tornado
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable fun AlertsWidget() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(start = 25.dp, top = 12.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Active Alerts",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 23.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WarningWidget(Icons.Outlined.Flood, "Flash Flood",  Color(0xFF104129), Color(0xFFA5D396))
                Spacer(modifier = Modifier.height(8.dp))
                WarningWidget(Icons.Outlined.Bolt, "Severe Thunderstorm",  Color(0xFF5D3E10), Color(0xFFF7DE93))
                Spacer(modifier = Modifier.height(8.dp))
                WarningWidget(Icons.Outlined.Tornado, "Tornado (Warning)",  Color(0xFF822424), Color(0xFFFFC2C2))
            }
        }
    }
}

@Composable fun WarningWidget(warningIcon: ImageVector, warning: String, containerColor: Color, iconColor: Color) {
    var containerColorCopy: Color = containerColor
    var iconColorCopy: Color = iconColor

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(start = 0.dp, end = 0.dp),
        shape = RoundedCornerShape(14.dp),
        color = containerColorCopy,
    ) {
        Row(modifier = Modifier.padding(start = 15.dp, top = 6.dp, bottom = 6.dp)) {
            Icon(
                imageVector = warningIcon,
                contentDescription = null,
                tint = iconColorCopy,
                modifier = Modifier.size(23.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = warning,
                color = iconColorCopy,
                fontWeight = FontWeight.W500,
                fontSize = 17.sp
            )
        }
    }
}