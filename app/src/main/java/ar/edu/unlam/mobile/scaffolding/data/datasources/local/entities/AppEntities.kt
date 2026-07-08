package ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val sessionToken: String?,
)

//
// @Entity(tableName = "clinics")
// data class AppClinicEntity(
//    @PrimaryKey val id: String,
//    val name: String,
//    val address: String,
//    val latitude: Double,
//    val longitude: Double,
//    val phone: String,
// )
@Entity(tableName = "clinics")
data class AppClinicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val phone: String,
    val website: String,
    val lat: Double,
    val lng: Double,
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val targetJoints: String, // Stored as comma-separated string or JSON
    val startAngle: Float,
    val endAngle: Float,
    val toleranceIdeal: Float,
    val toleranceWarning: Float,
    val repetitions: Int,
    val sets: Int,
    val bodyPart: String,
    val illustrationRes: Int? = null,
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val exerciseId: String,
    val dateTimestamp: Long,
    val durationSeconds: Long,
    val averageRom: Float,
    val successfulReps: Int,
)
