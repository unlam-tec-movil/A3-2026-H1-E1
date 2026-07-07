package ar.edu.unlam.mobile.scaffolding.data.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.location.LocationServicePort
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.prefs.MapPrefsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.map.ApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApi
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingRepository
import ar.edu.unlam.mobile.scaffolding.application.service.local.db.HasStoredClinicsInteractor
import ar.edu.unlam.mobile.scaffolding.data.datasources.device.health.HealthConnectDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.AchievementDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ExerciseDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.SessionDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.UserDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.database.AppDatabase
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.MapScreenPreferences
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Constants
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.AccelerometerDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.LightSensorDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AppModuleTest {
    @Test
    fun `providesRetrofit should return Retrofit instance with the correct Url `() {
        val retrofit = AppModule.providesRetrofit()

        assertNotNull(retrofit)
        assertEquals(Constants.GRAPH_HOPPER_BASE_URL, retrofit.baseUrl().toString())
    }

    @Test
    fun `providesRoutingApi should return RoutingApi instance using retrofit`() {
        val retrofitTest = AppModule.providesRetrofit()
        val routingApiTest = AppModule.providesRoutingApi(retrofitTest)

        assertNotNull(routingApiTest)
        assertTrue(routingApiTest is RoutingApi)
    }

    @Test
    fun `providesRoutingRepository should return RoutingRepository instance`() {
        val api = mockk<RoutingApi>()
        val provider = mockk<RoutingApiKeyProvider>()

        val routingRepoTest = AppModule.providesRoutingRepository(api, provider)
        assertNotNull(routingRepoTest)
        assertTrue(routingRepoTest is RoutingRepository)
    }

    @Test
    fun `providesGetRouteUseCase should return a GetRouteInteractor instance`() {
        val repo = mockk<RoutingRepository>()
        AppModule.providesGetRouteUsecase(repo)
    }

    @Test
    fun `providesRoutingApikey should return a RoutingApiKeyProvider instance`() {
        val provider = AppModule.providesRoutingApiKeyProvider()
        assertNotNull(provider)
        assertTrue(provider is RoutingApiKeyProvider)
    }

    @Test
    fun `providesApikey should return a ApiKeyProvider instance`() {
        val provider = AppModule.providesMapApiKeyProvider()
        assertNotNull(provider)
        assertTrue(provider is ApiKeyProvider)
    }

    @Test
    fun `providesApplicationScope should provide the scope`() =
        runTest {
            val scope =
                AppModule.providesApplicationScope()

            assertNotNull(scope)
            assertTrue(scope is CoroutineScope)
        }

    @Test
    fun `providesHasStoredClinicsUseCase should provide the interactor`() =
        runTest {
            val repo = mockk<ClinicsRepositoryPort>()
            coEvery { repo.hasStoredClinics() } returns true

            val interactor = AppModule.providesHasStoredClinicsUseCase(repo)
            val result = interactor.invoke()

            assertTrue(result)
            assertTrue(interactor is HasStoredClinicsInteractor)
        }

    @Test
    fun `providesMapScreenPrefs should provide an instance of prefs`() =
        runTest {
            val context = mockk<Application>(relaxed = true)
            val prefs = AppModule.providesMapScreenPreferences(context)

            assertNotNull(prefs)
            assertTrue(prefs is MapScreenPreferences)
        }

    @Test
    fun `providesSessionPreferences should provide an instance of prefs`() =
        runTest {
            val context = mockk<Application>(relaxed = true)
            val prefs = AppModule.providesSessionPreferences(context)

            assertNotNull(prefs)
            assertTrue(prefs is SessionPreferences)
        }

    @Test
    fun `providesMapPrefsRepository should provide the Repository instance`() =
        runTest {
            val prefs = mockk<MapScreenPreferences>(relaxed = true)
            val repo = AppModule.providesMapPrefsRepository(prefs)
            assertNotNull(repo)
            assertTrue(repo is MapPrefsRepositoryPort)
        }

    @Test
    fun `providesUserRepository should provide the Repository instance`() =
        runTest {
            val userDao = mockk<UserDao>(relaxed = true)
            val repo = AppModule.providesUserRepository(userDao)

            assertNotNull(repo)
            assertTrue(repo is UserRepository)
        }

    @Test
    fun `providesRehabRepository should provide the Repository instance`() =
        runTest {
            val sessionDao = mockk<SessionDao>(relaxed = true)
            val exerciseDao = mockk<ExerciseDao>(relaxed = true)
            val repo = AppModule.providesRehabRepository(sessionDao, exerciseDao)

            assertNotNull(repo)
            assertTrue(repo is RehabRepository)
        }

    @Test
    fun `providesAchievementRepository should provide the Repository instance`() =
        runTest {
            val achievementsDao = mockk<AchievementDao>(relaxed = true)
            val repo = AppModule.providesAchievementRepository(achievementsDao)

            assertNotNull(repo)
            assertTrue(repo is AchievementRepository)
        }

    @Test
    fun `providesLocationServicePortImpl should provide the interactor`() =
        runTest {
            val context = mockk<Application>(relaxed = true)
            val result = AppModule.providesLocationServicePortImpl(context)

            assertNotNull(result)
            assertTrue(result is LocationServicePort)
        }

    @Test
    fun `providesCameraSessionPort should provide the interactor`() =
        runTest {
            val context = mockk<Application>(relaxed = true)
            val result = AppModule.providesCameraSessionPort(context)

            assertNotNull(result)
            assertTrue(result is CameraSessionPort)
        }

    @Test
    fun `providesLightSensorDataSource should provide the interactor`() =
        runTest {
            val context = mockk<Application>(relaxed = true)

            val sensorManager = mockk<SensorManager>(relaxed = true)
            every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager

            val result = AppModule.providesLightSensorDataSource(context)

            assertNotNull(result)
            assertTrue(result is LightSensorDataSource)
        }

    @Test
    fun `providesAccelerometerDataSource should provide the interactor`() =
        runTest {
            val context = mockk<Application>(relaxed = true)

            val sensorManager = mockk<SensorManager>(relaxed = true)
            every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager

            val result = AppModule.providesAccelerometerDataSource(context)

            assertNotNull(result)
            assertTrue(result is AccelerometerDataSource)
        }

    // The internal functions of the step counter must have its own functions test in a separated file
    @Test
    fun `providesStepCounterDataSource should provide the interactor`() =
        runTest {
            val context = mockk<Application>(relaxed = true)

            val sensorManager = mockk<SensorManager>(relaxed = true)
            every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager

            val result = AppModule.providesStepCounterDataSource(context)

            assertNotNull(result)
            assertTrue(result is StepCounterDataSource)
        }

    @Test
    fun `providesHealthConnectDataSource should provide the interactor`() =
        runTest {
            val context = mockk<Application>(relaxed = true)

            val sensorManager = mockk<SensorManager>(relaxed = true)
            every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager

            val result = AppModule.providesHealthConnectDataSource(context)

            assertNotNull(result)
            assertTrue(result is HealthConnectDataSource)
        }

    @Test
    fun `providesClinicsInDataBaseRepository should provide the ClinicsRepositoryPort`() =
        runTest {
            val clinicsDao = mockk<ClinicsDao>(relaxed = true)
            val context = mockk<Context>(relaxed = true)

            val clinicRepo = AppModule.providesClinicsInDataBaseRepository(clinicsDao, context)

            assertNotNull(clinicRepo)
            assertTrue(clinicRepo is ClinicsRepositoryPort)
        }

    @Test
    fun providesClinicDao() =
        runTest {
            val db = mockk<AppDatabase>(relaxed = true)
            val result = AppModule.providesClinicDao(db)
            assertNotNull(result)
            assertTrue(result is ClinicsDao)
        }

    @Test
    fun providesSessionDao() =
        runTest {
            val db = mockk<AppDatabase>(relaxed = true)
            val result = AppModule.providesSessionDao(db)
            assertNotNull(result)
            assertTrue(result is SessionDao)
        }

    @Test
    fun providesExerciseDao() =
        runTest {
            val db = mockk<AppDatabase>(relaxed = true)
            val result = AppModule.providesExerciseDao(db)
            assertNotNull(result)
            assertTrue(result is ExerciseDao)
        }

    @Test
    fun providesAchievementDao() =
        runTest {
            val db = mockk<AppDatabase>(relaxed = true)
            val result = AppModule.providesAchievementDao(db)
            assertNotNull(result)
            assertTrue(result is AchievementDao)
        }

    @Test
    fun providesUserDao() =
        runTest {
            val db = mockk<AppDatabase>(relaxed = true)
            val result = AppModule.providesUserDao(db)
            assertNotNull(result)
            assertTrue(result is UserDao)
        }
}
