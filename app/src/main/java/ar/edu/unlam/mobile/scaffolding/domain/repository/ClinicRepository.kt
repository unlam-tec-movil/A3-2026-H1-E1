package ar.edu.unlam.mobile.scaffolding.domain.repository

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.ClinicEntity
import kotlinx.coroutines.flow.Flow

interface ClinicRepository {
    fun getClinics(): Flow<List<ClinicEntity>>

    suspend fun syncClinicsWithRemote()
}
