package com.example.breez.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    object LocationDisabled : LocationResult()
    object PermissionDenied : LocationResult()
    object Error : LocationResult()
}

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationWithStatus(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.PermissionDenied
        }

        if (!isLocationEnabled()) {
            return LocationResult.LocationDisabled
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (currentLocation != null) {
                return LocationResult.Success(currentLocation)
            }

            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                LocationResult.Success(lastLocation)
            } else {
                LocationResult.Error
            }
        } catch (e: SecurityException) {
            LocationResult.PermissionDenied
        } catch (e: Exception) {
            try {
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    LocationResult.Success(lastLocation)
                } else {
                    LocationResult.Error
                }
            } catch (e: Exception) {
                LocationResult.Error
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (currentLocation != null) {
                return currentLocation
            }

            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            try {
                fusedLocationClient.lastLocation.await()
            } catch (e: Exception) {
                null
            }
        }
    }
}
