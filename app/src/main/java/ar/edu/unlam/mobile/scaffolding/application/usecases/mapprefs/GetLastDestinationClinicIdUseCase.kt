package ar.edu.unlam.mobile.scaffolding.application.usecases.mapprefs

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.prefs.MapPrefsRepositoryPort
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastDestinationClinicIdUseCase
    @Inject
    constructor(
        private val repo: MapPrefsRepositoryPort,
    ) {
        operator fun invoke(): Flow<Int?> = repo.getLastDestinationClicId()
    }
