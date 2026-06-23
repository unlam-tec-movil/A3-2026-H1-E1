package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import javax.inject.Inject

class GetClinicsFromAssetsUseCase
    @Inject
    constructor(
        private val dataBaseRepositoryImpl: ClinicsRepositoryPort,
    ) {
        operator fun invoke() = dataBaseRepositoryImpl.getClinicsFromAssets()
    }
