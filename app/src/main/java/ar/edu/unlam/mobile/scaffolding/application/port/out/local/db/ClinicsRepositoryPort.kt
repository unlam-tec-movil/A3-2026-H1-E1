package ar.edu.unlam.mobile.scaffolding.application.port.out.local.db

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import kotlinx.coroutines.flow.Flow

interface ClinicsRepositoryPort {
    fun getClinics(): Flow<List<Clinic>>

    suspend fun saveClinic(clinic: Clinic)

    suspend fun deleteClinic(clinic: Clinic)

    suspend fun updateClinic(clinic: Clinic)

    suspend fun saveAllClinics(clinics: List<Clinic>)

    fun getClinicsFromAssets(): List<Clinic>

    suspend fun hasStoredClinics(): Boolean
}
