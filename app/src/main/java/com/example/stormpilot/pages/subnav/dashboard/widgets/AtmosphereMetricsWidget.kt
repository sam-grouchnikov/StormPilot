package com.example.stormpilot.pages.subnav.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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