package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClinicsStoredUseCase
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: ClinicsRepositoryPort,
    ) {
        operator fun invoke(): Flow<List<Clinic>> = dataBaseRepositoryImpl.getClinics()
    }
