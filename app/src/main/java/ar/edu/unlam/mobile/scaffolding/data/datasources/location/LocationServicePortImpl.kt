package ar.edu.unlam.mobile.scaffolding.data.datasources.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.location.LocationServicePort
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationServicePortImpl(
    private val context: Context,
) : LocationServicePort {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> =
        callbackFlow {
            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { trySend(it) }
                    }
                }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper(),
            )
            awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
        }
}
