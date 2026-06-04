package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ClinicEntity

@Database(version = 1, entities = [ClinicEntity::class], exportSchema = false)
abstract class ClinicsDataBase : RoomDatabase() {
    abstract fun getStoredClinicsDao(): StoredClinicsDao
}
