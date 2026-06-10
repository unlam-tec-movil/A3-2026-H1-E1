package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db

import androidx.room.Room
import androidx.room.RoomDatabase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.robolectric.RuntimeEnvironment

object RoomTestDatabase {
    fun <T : RoomDatabase> build(dbClass: Class<T>): T {
        val context = RuntimeEnvironment.getApplication()

        return Room
            .inMemoryDatabaseBuilder(context, dbClass)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
