package ar.edu.unlam.mobile.scaffolding.data.repositories

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.prefs.MapPrefsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.MapScreenPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MapPrefsRepositoryImpl
    @Inject
    constructor(
        private val mapPrefs: MapScreenPreferences,
    ) : MapPrefsRepositoryPort {
        override suspend fun saveLastClinicDestinationId(clinicIdToBeSaved: Int) {
            mapPrefs.saveLastClinicId(clinicIdToBeSaved)
        }

        override fun getLastDestinationClicId(): Flow<Int?> = mapPrefs.lastClinicId
    }
