package ar.edu.unlam.mobile.scaffolding.infraestructure.di

import android.app.Application
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.LocationServicePort
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.location.LocationDataSource
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
}
