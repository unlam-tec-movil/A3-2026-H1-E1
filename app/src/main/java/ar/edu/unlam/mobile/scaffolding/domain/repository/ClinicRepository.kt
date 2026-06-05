package ar.edu.unlam.mobile.scaffolding.domain.repository

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import kotlinx.coroutines.flow.Flow

interface ClinicRepository {
    fun getClinics(): Flow<List<Clinic>>

    suspend fun syncClinicsWithRemote()
}
