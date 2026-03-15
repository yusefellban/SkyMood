package iti.yousef.skymood.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Repository for encapsulating geographic location interactions.
 * Hides the Google Play Services FusedLocationProviderClient dependency from ViewModels.
 */
class LocationRepository(application: Application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
        } catch (e: Exception) {
            null
        }
    }
}
