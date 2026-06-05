package com.example.stormpilot.features.location.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Owns fused-location tracking and exposes the latest device position to weather and alert features.
 */
class LocationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<LocationData?>(null)
    val location: StateFlow<LocationData?> = _location

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L)
        .setMinUpdateIntervalMillis(500L)
        .build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                _location.value = LocationData(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    bearing = loc.bearing,
                    speed = loc.speed,
                )
            }
        }
    }

    fun startTracking() {
        if (!hasFineLocationPermission()) return

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            context.mainLooper
        )
    }

    suspend fun fetchCurrentLocation(): LocationData? {
        if (!hasAnyLocationPermission()) return null

        val priority = if (hasFineLocationPermission()) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val tokenSource = CancellationTokenSource()

        return try {
            val location = fusedLocationClient.getCurrentLocation(priority, tokenSource.token).await(tokenSource)
                ?: fusedLocationClient.lastLocation.await()
            location?.toLocationData()?.also { _location.value = it }
        } catch (_: SecurityException) {
            null
        }
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(callback)
    }

    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasAnyLocationPermission(): Boolean =
        hasFineLocationPermission() ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float = 0f,
    val speed: Float = 0f,
)

private fun android.location.Location.toLocationData(): LocationData =
    LocationData(
        latitude = latitude,
        longitude = longitude,
        bearing = bearing,
        speed = speed,
    )

private suspend fun <T> Task<T>.await(tokenSource: CancellationTokenSource? = null): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.cancel(CancellationException("Location request was canceled."))
        }
        continuation.invokeOnCancellation {
            tokenSource?.cancel()
        }
    }
