package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import javax.inject.Inject

class UpdateClinicUseCAse
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: ClinicsRepositoryPort,
    ) {
        suspend operator fun invoke(clinic: Clinic) = dataBaseRepositoryImpl.deleteClinic(clinic)
    }
