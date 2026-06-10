package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.ClinicDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.ExerciseDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.SessionDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.UserDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.*

@Database(
    entities = [
        UserEntity::class,
        AppClinicEntity::class,
        ExerciseEntity::class,
        SessionEntity::class,
        AchievementEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun clinicDao(): ClinicDao

    abstract fun sessionDao(): SessionDao

    abstract fun exerciseDao(): ExerciseDao
}
