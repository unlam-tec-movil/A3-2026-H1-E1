package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.AppClinicEntity
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ClinicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredClinicsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinic(c: ClinicEntity)

    @Query("SELECT * FROM storedClinics ORDER BY id DESC")
    fun getStoredClinics(): Flow<List<ClinicEntity>>

    @Insert
    suspend fun insertAll(clinics: List<ClinicEntity>)

    @Query("SELECT COUNT(*) FROM storedClinics")
    suspend fun getClinicCount(): Int
}
