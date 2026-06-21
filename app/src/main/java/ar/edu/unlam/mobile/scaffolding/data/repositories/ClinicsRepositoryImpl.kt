package ar.edu.unlam.mobile.scaffolding.data.repositories

import android.content.Context
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.mappers.toAppEntity
import ar.edu.unlam.mobile.scaffolding.data.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClinicsRepositoryImpl
    @Inject
    constructor(
        private val clinicsDao: ClinicsDao,
        @ApplicationContext private val context: Context,
    ) : ClinicsRepositoryPort {
        override fun getClinics(): Flow<List<Clinic>> =
            clinicsDao.getClinics().map { entities ->
                entities.map { clinicEntity ->
                    clinicEntity.toDomain()
                }
            }

        override suspend fun saveClinic(clinic: Clinic) {
            clinicsDao.insertClinic(clinic.toAppEntity())
        }

        override suspend fun deleteClinic(clinic: Clinic) {
            // Implement delete logic - currently no delete method in DAO
            // This would require adding a delete method to StoredClinicsDao
        }

        override suspend fun updateClinic(clinic: Clinic) {
            // Implement update logic - currently no update method in DAO
            // This would require adding an update method to StoredClinicsDao
        }

        override suspend fun saveAllClinics(clinics: List<Clinic>) {
            clinicsDao.insertAll(clinics.map { it.toAppEntity() })
        }

        override suspend fun hasStoredClinics(): Boolean = clinicsDao.getClinicCount() > 0

        override fun getClinicsFromAssets(): List<Clinic> {
            val jsonString =
                context.assets
                    .open("clinicas_ciudadela_ba.json")
                    .bufferedReader()
                    .use { it.readText() }

            val jsonObject = Gson().fromJson(jsonString, JsonObject::class.java)
            val clinicsArray = jsonObject.getAsJsonArray("clinics")

            val clinics: List<Clinic> =
                Gson().fromJson(
                    clinicsArray,
                    object : TypeToken<List<Clinic>>() {}.type,
                )

            return clinics
        }
    }
