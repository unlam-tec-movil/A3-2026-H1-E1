package ar.edu.unlam.mobile.scaffolding.infraestructure.di

import android.app.Application
import androidx.room.Room
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.LocationServicePort
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.camera.CameraXSessionAdapter
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location.DataBaseRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location.LocationDataSource
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.SessionDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.UserDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db.AppDatabase
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db.ClinicsDataBase
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.repositories.RehabRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.repositories.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesSessionPreferences(context: Application): SessionPreferences = SessionPreferences(context = context)

    @Provides
    @Singleton
    fun providesLocationServicePortImpl(context: Application): LocationServicePort =
        LocationDataSource(context = context)

    @Provides
    @Singleton
    fun providesCameraSessionPort(context: Application): CameraSessionPort = CameraXSessionAdapter(context = context)

    @Provides
    @Singleton
    fun providesClinicsDatabase(context: Application): ClinicsDataBase =
        Room
            .databaseBuilder(
                context = context,
                klass = ClinicsDataBase::class.java,
                name = "storedClinicsDB",
            ).build()

    @Provides
    @Singleton
    fun providesStoredClinicsDao(db: ClinicsDataBase): StoredClinicsDao = db.getStoredClinicsDao()

    @Provides
    @Singleton
    fun providesDataBaseRepository(dao: StoredClinicsDao): DataBaseRepositoryPort =
        DataBaseRepositoryImpl(clinicsDao = dao)

    @Provides
    @Singleton
    fun providesAppDatabase(context: Application): AppDatabase =
        Room
            .databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = "app_database",
            ).build()

    @Provides
    @Singleton
    fun providesUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun providesSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    @Singleton
    fun providesUserRepository(userDao: UserDao): UserRepository = UserRepositoryImpl(userDao = userDao)

    @Provides
    @Singleton
    fun providesRehabRepository(sessionDao: SessionDao): RehabRepository = RehabRepositoryImpl(sessionDao = sessionDao)
}
