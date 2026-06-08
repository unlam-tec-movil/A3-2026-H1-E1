package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
import javax.inject.Inject

class PopulateClinicsDbUseCase
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: DataBaseLocationRepositoryPort,
    ) {
        suspend operator fun invoke(clinics: List<Clinic>) = dataBaseRepositoryImpl.saveAllClinics(clinics = clinics)
    }
