package ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.AppClinicEntity
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.SessionEntity
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface ClinicsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinic(c: AppClinicEntity)

    @Query("SELECT * FROM clinics ORDER BY id DESC")
    fun getClinics(): Flow<List<AppClinicEntity>>

    @Insert
    suspend fun insertAll(clinics: List<AppClinicEntity>)

    @Query("SELECT COUNT(*) FROM clinics")
    suspend fun getClinicCount(): Int
}
// @Dao
// interface ClinicsDao {
//    @Query("SELECT * FROM clinics")
//    fun getAllClinics(): Flow<List<AppClinicEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertClinics(clinics: List<AppClinicEntity>)
// }

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY dateTimestamp DESC")
    fun getSessionsByUser(userId: String): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: SessionEntity)
}
