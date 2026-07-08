package ar.edu.unlam.mobile.scaffolding.data.datasources.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.AchievementDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ExerciseDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.SessionDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.UserDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        AppClinicEntity::class,
        ExerciseEntity::class,
        SessionEntity::class,
        AchievementEntity::class,
    ],
    version = 11,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun clinicDao(): ClinicsDao

    abstract fun sessionDao(): SessionDao

    abstract fun exerciseDao(): ExerciseDao

    abstract fun achievementDao(): AchievementDao
}
