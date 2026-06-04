package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import android.location.Location
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.LocationServicePort
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location.DataBaseRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClinicsStoredUseCase
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: DataBaseRepositoryPort,
    ) {
        operator fun invoke(): Flow<List<Clinic>> = dataBaseRepositoryImpl.getStoredClinics()
    }
