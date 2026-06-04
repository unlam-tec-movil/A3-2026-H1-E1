package ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseRepositoryPort
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataBaseRepositoryImpl
    @Inject
    constructor(
        private val clinicsDao: StoredClinicsDao,
    ) : DataBaseRepositoryPort {
        override fun getStoredClinics(): Flow<List<Clinic>> =
            clinicsDao.getStoredClinics().map { entities ->
                entities.map { clinicEntity ->
                    clinicEntity.toDomain()
                }
            }
    }
