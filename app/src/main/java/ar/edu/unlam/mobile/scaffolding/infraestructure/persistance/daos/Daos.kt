package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos

import androidx.room.*
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface ClinicDao {
    @Query("SELECT * FROM clinics")
    fun getAllClinics(): Flow<List<AppClinicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinics(clinics: List<AppClinicEntity>)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY dateTimestamp DESC")
    fun getSessionsByUser(userId: String): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: SessionEntity)
}
