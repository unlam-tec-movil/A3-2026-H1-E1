package ar.edu.unlam.mobile.scaffolding.application.service.local.db

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.HasStoredClinicsUseCase
import javax.inject.Inject

class HasStoredClinicsInteractor
    @Inject
    constructor(
        private val repository: ClinicsRepositoryPort,
    ) : HasStoredClinicsUseCase {
        override suspend fun invoke(): Boolean = repository.hasStoredClinics()
    }
