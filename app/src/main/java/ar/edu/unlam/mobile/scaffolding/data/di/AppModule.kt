package ar.edu.unlam.mobile.scaffolding.data.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.HasStoredClinicsUseCase
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.location.LocationServicePort
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.map.ApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApi
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingRepository
import ar.edu.unlam.mobile.scaffolding.application.service.local.db.HasStoredClinicsInteractor
import ar.edu.unlam.mobile.scaffolding.application.service.local.remote.routing.GetRouteUseCase
import ar.edu.unlam.mobile.scaffolding.application.service.remote.routing.GetRouteInteractor
import ar.edu.unlam.mobile.scaffolding.data.datasources.camera.CameraXSessionAdapter
import ar.edu.unlam.mobile.scaffolding.data.datasources.device.health.HealthConnectDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ExerciseDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.SessionDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.UserDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.database.AppDatabase
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.data.datasources.location.BuildConfigApiKeyProviderImpl
import ar.edu.unlam.mobile.scaffolding.data.datasources.location.LocationServicePortImpl
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Constants
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.routing.BuildConfigRoutingApiKeyProviderImpl
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.AccelerometerDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.LightSensorDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.data.repositories.ClinicsRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.data.repositories.RehabRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.data.repositories.RoutingRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.data.repositories.UserRepositoryImpl
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesHasStoredClinicsUseCase(repository: ClinicsRepositoryPort): HasStoredClinicsUseCase =
        HasStoredClinicsInteractor(repository)

    @Provides
    @Singleton
    fun providesRoutingRepository(
        api: RoutingApi,
        routingApiKeyProvider: RoutingApiKeyProvider,
    ): RoutingRepository =
        RoutingRepositoryImpl(
            api = api,
            routingApiKeyProvider = routingApiKeyProvider,
        )

    @Provides
    @Singleton
    fun providesGetRouteUsecase(routeRepo: RoutingRepository): GetRouteUseCase =
        GetRouteInteractor(routRepo = routeRepo)

    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(Constants.GRAPH_HOPPER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providesRoutingApi(retrofit: Retrofit): RoutingApi = retrofit.create(RoutingApi::class.java)

    @Provides
    @Singleton
    fun providesFirebaseAuth(
        @ApplicationContext context: Context,
    ): FirebaseAuth {
        if (FirebaseApp
                .getApps(context)
                .isEmpty()
        ) {
            val options =
                FirebaseOptions
                    .Builder()
                    .setApiKey("dummy_api_key")
                    .setApplicationId("ar.edu.unlam.mobile.scaffolding")
                    .setProjectId("dummy-project")
                    .build()
            FirebaseApp
                .initializeApp(context, options)
        }
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providesLightSensorDataSource(context: Application): LightSensorDataSource =
        LightSensorDataSource(context = context)

    @Provides
    @Singleton
    fun providesAccelerometerDataSource(context: Application): AccelerometerDataSource =
        AccelerometerDataSource(context = context)

    @Provides
    @Singleton
    fun providesStepCounterDataSource(context: Application): StepCounterDataSource =
        StepCounterDataSource(context = context)

    @Provides
    @Singleton
    fun providesHealthConnectDataSource(context: Application): HealthConnectDataSource =
        HealthConnectDataSource(context = context)

    @Provides
    @Singleton
    fun providesSessionPreferences(context: Application): SessionPreferences = SessionPreferences(context = context)

    @Provides
    @Singleton
    fun providesLocationServicePortImpl(context: Application): LocationServicePort =
        LocationServicePortImpl(context = context)

    @Provides
    @Singleton
    fun providesCameraSessionPort(context: Application): CameraSessionPort = CameraXSessionAdapter(context = context)

    @Provides
    @Singleton
    fun providesMapApiKeyProvider(): ApiKeyProvider = BuildConfigApiKeyProviderImpl()

    @Provides
    @Singleton
    fun providesRoutingApiKeyProvider(): RoutingApiKeyProvider = BuildConfigRoutingApiKeyProviderImpl()

    @Provides
    @Singleton
    fun providesDataBaseRepository(
        dao: ClinicsDao,
        @ApplicationContext context: Context,
    ): ClinicsRepositoryPort = ClinicsRepositoryImpl(clinicsDao = dao, context)

    @Provides
    @Singleton
    fun providesAppDatabase(context: Application): AppDatabase =
        Room
            .databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = "app_database",
            ).createFromAsset("databases/prepopulated.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun providesClinicDao(db: AppDatabase): ClinicsDao = db.clinicDao()

    @Provides
    @Singleton
    fun providesSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    @Singleton
    fun providesExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    @Singleton
    fun providesUserRepository(userDao: UserDao): UserRepository = UserRepositoryImpl(userDao = userDao)

    @Provides
    @Singleton
    fun providesRehabRepository(
        sessionDao: SessionDao,
        exerciseDao: ExerciseDao,
    ): RehabRepository = RehabRepositoryImpl(sessionDao = sessionDao, exerciseDao = exerciseDao)
}
