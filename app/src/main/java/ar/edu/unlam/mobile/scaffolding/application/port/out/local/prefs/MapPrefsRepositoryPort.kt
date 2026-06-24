package ar.edu.unlam.mobile.scaffolding.application.port.out.local.prefs

import kotlinx.coroutines.flow.Flow

interface MapPrefsRepositoryPort {
    suspend fun saveLastClinicDestinationId(clinicIdToBeSaved: Int)

    fun getLastDestinationClicId(): Flow<Int?>
}
