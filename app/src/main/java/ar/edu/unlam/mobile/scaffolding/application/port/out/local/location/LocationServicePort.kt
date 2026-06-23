package ar.edu.unlam.mobile.scaffolding.application.port.out.local.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationServicePort {
    fun getLocationUpdates(): Flow<Location>
}
