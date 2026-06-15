package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClinicsStoredUseCase
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: DataBaseLocationRepositoryPort,
    ) {
        operator fun invoke(): Flow<List<Clinic>> = dataBaseRepositoryImpl.getClinics()
    }
