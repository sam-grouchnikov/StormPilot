package com.example.stormpilot.pages.subnav.dashboard.widgets

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun LocationWidget() {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Locating…") }

    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    DisposableEffect(hasPermission) {
        if (!hasPermission) {
            locationText = "Location unavailable"
            return@DisposableEffect onDispose {}
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    val name = reverseGeocode(context, location.latitude, location.longitude)
                    withContext(Dispatchers.Main) {
                        locationText = name
                    }
                }
            }
        }

        client.requestLocationUpdates(request, callback, context.mainLooper)
        onDispose { client.removeLocationUpdates(callback) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                text = locationText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.W500,
                fontSize = 23.sp
            )
        }
    }
}

private fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var result = "Unknown location"
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                val address = addresses.firstOrNull()
                val city = address?.locality
                val state = address?.adminArea
                result = listOfNotNull(city, state).joinToString(", ")
            }
            result
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            val city = address?.locality
            val state = address?.adminArea
            listOfNotNull(city, state).joinToString(", ")
        }
    } catch (e: Exception) {
        "Unknown location"
    }
}