package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import android.location.Location
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.location.LocationServicePort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserverLocationUseCase
    @Inject
    constructor(
        private val locationServicePort: LocationServicePort,
    ) {
        operator fun invoke(): Flow<Location> = locationServicePort.getLocationUpdates()
    }
