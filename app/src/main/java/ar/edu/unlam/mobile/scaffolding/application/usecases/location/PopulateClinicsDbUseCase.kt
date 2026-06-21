package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import javax.inject.Inject

class PopulateClinicsDbUseCase
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: ClinicsRepositoryPort,
    ) {
        suspend operator fun invoke(clinics: List<Clinic>) = dataBaseRepositoryImpl.saveAllClinics(clinics = clinics)
    }
