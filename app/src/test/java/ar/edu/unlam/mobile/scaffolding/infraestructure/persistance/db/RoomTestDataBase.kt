package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.robolectric.RuntimeEnvironment

/**
 * Utility to create an in-memory Room database for JVM integration tests.
 */
object RoomTestDatabase {
    fun <T : RoomDatabase> build(dbClass: Class<T>): T =
        Room
            .inMemoryDatabaseBuilder(
                context = RuntimeEnvironment.getApplication(),
                klass = dbClass,
            ).setDriver(BundledSQLiteDriver()) // Essential for JVM execution
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
}
