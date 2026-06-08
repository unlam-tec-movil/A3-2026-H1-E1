package ar.edu.unlam.mobile.scaffolding.domain.ports.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import kotlinx.coroutines.flow.Flow

interface DataBaseLocationRepositoryPort {
    fun getStoredClinics(): Flow<List<Clinic>>

    suspend fun saveAllClinics(clinics: List<Clinic>)

    fun getClinicsFromAssets(): List<Clinic>

    suspend fun hasStoredClinics(): Boolean
}
