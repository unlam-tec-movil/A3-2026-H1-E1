package ar.edu.unlam.mobile.scaffolding.domain.ports.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationServicePort {
    fun getLocationUpdates(): Flow<Location>
}
