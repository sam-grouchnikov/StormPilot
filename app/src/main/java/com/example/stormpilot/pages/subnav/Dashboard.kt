package com.example.stormpilot.pages.subnav

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Flood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Tornado
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.StormPilotTheme

@Composable fun RadarPage() {
    var isDarkMode by remember { mutableStateOf(true) }

    StormPilotTheme(darkTheme = isDarkMode) {
        Column{
            LocationWidget("Greensburg, Kansas")
            Spacer(modifier = Modifier.height(15.dp))
            AlertsWidget()
            Spacer(modifier = Modifier.height(10.dp))
            AtmosphereMetricsWidget()
            Spacer(modifier = Modifier.height(15.dp))
            ConnectivityWidget()
            Spacer(modifier = Modifier.height(15.dp))
            AssistanceWidget()
        }
    }
}

@Composable fun LocationWidget(location: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 25.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(27.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = location,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.W500,
                fontSize = 23.sp
            )
        }
    }
}

@Composable fun AlertsWidget() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
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

@Composable fun AtmosphereMetricsWidget() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(start = 20.dp)
        ) {
            AtmosphereTinyBox("CAPE", "1200 j/kg")
            Spacer(modifier = Modifier.width(7.dp))
            AtmosphereTinyBox("Shear (0-6km)", "35kt")
            Spacer(modifier = Modifier.width(7.dp))
            AtmosphereTinyBox("Helicity (0-3km)", "400 m/s")
        }
}

@Composable fun AtmosphereTinyBox(label: String, value: String) {
    Surface(
        modifier = Modifier
            .padding(start = 0.dp, end = 0.dp, top = 5.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(all = 10.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp
            )
        }
    }
}

@Composable fun ConnectivityWidget() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        var connectionHealthColor = Color(0xFFA5D396)
        var connectionHealthValue = "Strong"
        var areaColor = Color(0xFFC76F6F)
        var areaValue = "Area of Low Coverage"
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(start = 25.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Connectivity Health",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 23.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row() {
                Icon(
                    imageVector = Icons.Outlined.SignalCellularAlt,
                    contentDescription = null,
                    tint = connectionHealthColor,
                    modifier = Modifier.size(23.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "Current: $connectionHealthValue"
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Row() {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = areaColor,
                    modifier = Modifier.size(23.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "$areaValue"
                )
            }
        }
    }
}

@Composable fun AssistanceWidget() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = "Nearby Assistance",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 23.sp
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            LocationSelector("gas", "QuikTrip @ Greensburg")
            Spacer(modifier = Modifier.height(0.dp))
            LocationSelector("gas", "Wellstar Arlington")
        }
    }
}

@Composable
fun LocationSelector(type: String, location: String) {
    var isSelected by remember { mutableStateOf(false) }

    // 2. Dynamic colors based on selection state
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        onClick = { isSelected = !isSelected }, // Toggle selection on click
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(top = 3.dp, start = 5.dp, bottom = 3.dp, end=15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Icon and Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocalGasStation,
                    contentDescription = null,
                    tint = Color(0xFFBDAAD5),
                    modifier = Modifier.size(21.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Column {
                    Text(
                        text = location,
                        color = MaterialTheme.colorScheme.onSurface
                        )
                }
            }

            if (isSelected) {
                FilledIconButton(
                    onClick = { /* Handle car action here */ },
                    modifier = Modifier
                        .size(30.dp)
                        .padding(all = 1.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsCar,
                        contentDescription = "Drive",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}
