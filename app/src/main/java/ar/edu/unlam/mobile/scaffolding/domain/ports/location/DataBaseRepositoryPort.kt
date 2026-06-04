package ar.edu.unlam.mobile.scaffolding.domain.ports.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import kotlinx.coroutines.flow.Flow

interface DataBaseRepositoryPort {
    fun getStoredClinics(): Flow<List<Clinic>>
}
