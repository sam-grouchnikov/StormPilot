package com.example.stormpilot.pages.subnav.maps

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.compose.StormPilotTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapsPage() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var userLocation by remember { mutableStateOf<Position?>(null) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = 39.8283, longitude = -98.5795),
            zoom = 3.0,
        ),
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
                    .setMinUpdateIntervalMillis(1_000L)
                    .build()

            val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { location ->
                            userLocation = Position(location.longitude, location.latitude)
                        }
                    }
                }

            fusedLocationClient.requestLocationUpdates(locationRequest, callback, context.mainLooper)

            onDispose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }

    var hasInitialLocation by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            if (!hasInitialLocation) {
                hasInitialLocation = true
                cameraState.animateTo(
                    finalPosition = cameraState.position.copy(target = location, zoom = 16.0),
                    duration = 1.seconds,
                )
            }
        }
    }

    StormPilotTheme(darkTheme = true) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri("https://api.protomaps.com/styles/v5/dark/en.json?key=64a5f0a9c35b4ca1"),
            cameraState = cameraState,
            modifier = Modifier.padding(5.dp),
            options = MapOptions(
                ornamentOptions = OrnamentOptions(
                    padding = PaddingValues(0.dp),
                    isLogoEnabled = false,
                    logoAlignment = Alignment.BottomStart,
                    isAttributionEnabled = false,
                    attributionAlignment = Alignment.BottomEnd,
                    isCompassEnabled = true,
                    compassAlignment = Alignment.TopEnd,
                    isScaleBarEnabled = true,
                    scaleBarAlignment = Alignment.TopStart,
                ),
            ),
        ) {
            val userLocationFeatureCollection = remember(userLocation) {
                val json = if (userLocation != null) {
                    """
        {
          "type": "FeatureCollection",
          "features": [{
            "type": "Feature",
            "geometry": {
              "type": "Point",
              "coordinates": [${userLocation!!.longitude}, ${userLocation!!.latitude}]
            },
            "properties": {}
          }]
        }
        """.trimIndent()
                } else {
                    """{"type": "FeatureCollection", "features": []}"""
                }
                GeoJsonData.JsonString(json)
            }

            val userLocationSource = rememberGeoJsonSource(
                data = userLocationFeatureCollection
            )

            CircleLayer(
                id = "user-location",
                source = userLocationSource,
                color = const(Color(0xFF2E8BFF)),
                radius = const(8.dp),
                strokeColor = const(Color.White),
                strokeWidth = const(3.dp),
            )
        }
    }
}
