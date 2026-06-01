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

@Entity(tableName = "clinics")
data class ClinicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val targetAngle: Float,
    val repetitions: Int,
    val sets: Int,
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val dateTimestamp: Long,
    val durationSeconds: Long,
    val averageRom: Float,
    val successfulReps: Int,
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedAtTimestamp: Long?,
)
