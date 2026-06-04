package ar.edu.unlam.mobile.scaffolding.infraestructure.di

import android.app.Application
import androidx.room.Room
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.LocationServicePort
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location.DataBaseRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location.LocationDataSource
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db.ClinicsDataBase
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
    fun providesLocationServicePortImpl(context: Application): LocationServicePort =
        LocationDataSource(context = context)

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
}
