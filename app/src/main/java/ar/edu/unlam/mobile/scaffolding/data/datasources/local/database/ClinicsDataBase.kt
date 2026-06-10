package ar.edu.unlam.mobile.scaffolding.data.datasources.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.ClinicEntity

@Database(version = 1, entities = [ClinicEntity::class], exportSchema = false)
abstract class ClinicsDataBase : RoomDatabase() {
    abstract fun getStoredClinicsDao(): StoredClinicsDao
}
