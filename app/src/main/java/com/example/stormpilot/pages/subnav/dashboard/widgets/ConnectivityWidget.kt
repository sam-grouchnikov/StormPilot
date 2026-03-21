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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable fun ConnectivityWidget() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    text = areaValue
                )
            }
        }
    }
}