package ar.edu.unlam.mobile.scaffolding.data.repositories

import android.content.Context
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.data.mappers.toEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataBaseLocationRepositoryImpl
    @Inject
    constructor(
        private val clinicsDao: StoredClinicsDao,
        @ApplicationContext private val context: Context,
    ) : DataBaseLocationRepositoryPort {
        override fun getStoredClinics(): Flow<List<Clinic>> =
            clinicsDao.getStoredClinics().map { entities ->
                entities.map { clinicEntity ->
                    clinicEntity.toDomain()
                }
            }

        override suspend fun saveClinic(clinic: Clinic) {
            clinicsDao.insertClinic(clinic.toEntity())
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
            clinicsDao.insertAll(clinics.map { it.toEntity() })
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
