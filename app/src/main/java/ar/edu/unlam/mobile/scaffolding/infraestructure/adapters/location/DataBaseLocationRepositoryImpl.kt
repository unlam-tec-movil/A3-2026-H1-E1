package ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location

import android.content.Context
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers.toEntity
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
