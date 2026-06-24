package ar.edu.unlam.mobile.scaffolding.application.usecases.mapprefs

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.prefs.MapPrefsRepositoryPort
import javax.inject.Inject

class SaveLastDestinationClinicIdUseCase
    @Inject
    constructor(
        private val repo: MapPrefsRepositoryPort,
    ) {
        suspend operator fun invoke(id: Int) = repo.saveLastClinicDestinationId(id)
    }
