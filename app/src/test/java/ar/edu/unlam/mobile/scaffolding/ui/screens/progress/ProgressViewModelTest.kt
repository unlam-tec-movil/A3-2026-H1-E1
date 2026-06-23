package ar.edu.unlam.mobile.scaffolding.ui.screens.progress

import app.cash.turbine.test
import ar.edu.unlam.mobile.scaffolding.data.datasources.device.health.HealthConnectDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    private val rehabRepository = mockk<RehabRepository>(relaxed = true)
    private val healthConnectDataSource = mockk<HealthConnectDataSource>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProgressData should transition from loading to success`() = runTest(testDispatcher) {
        coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(emptyList())
        coEvery { rehabRepository.getExercises() } returns flowOf(emptyList())
        coEvery { healthConnectDataSource.hasAllPermissions() } returns false

        val viewModel = ProgressViewModel(rehabRepository, healthConnectDataSource)

        viewModel.uiState.test {
            // Initial state (emitted immediately on connection because uiState is a StateFlow)
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            // Let coroutines run
            advanceUntilIdle()

            // Success state (use expectMostRecentItem to consume intermediate loading updates)
            val successState = expectMostRecentItem()
            assertFalse(successState.isLoading)
            assertNull(successState.error)
        }
    }

    @Test
    fun `loadProgressData should load sessions and complete 7 days timeline correctly`() =
        runTest(testDispatcher) {
            val today = LocalDate.now()
            val zoneId = ZoneId.systemDefault()

            val yesterdayTimestamp =
                today
                    .minusDays(1)
                    .atTime(12, 0)
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli()
            val threeDaysAgoTimestamp =
                today
                    .minusDays(3)
                    .atTime(12, 0)
                    .atZone(zoneId)
                    .toInstant()
                    .toEpochMilli()

            val mockSessions =
                listOf(
                    Session(
                        id = 1L,
                        userId = "user_imanol",
                        exerciseId = "ex_knee_flexion",
                        dateTimestamp = yesterdayTimestamp,
                        durationSeconds = 600,
                        averageRom = 112f,
                        successfulReps = 10,
                    ),
                    Session(
                        id = 2L,
                        userId = "user_imanol",
                        exerciseId = "ex_knee_flexion",
                        dateTimestamp = threeDaysAgoTimestamp,
                        durationSeconds = 800,
                        averageRom = 95f,
                        successfulReps = 8,
                    ),
                )

            val mockExercises =
                listOf(
                    Exercise(
                        id = "ex_knee_flexion",
                        name = "Flexión de Rodilla",
                        description = "Mock desc",
                        targetJoints = listOf("LEFT_HIP", "LEFT_KNEE", "LEFT_ANKLE"),
                        startAngle = 180f,
                        endAngle = 90f,
                        repetitions = 10,
                        sets = 3,
                        bodyPart = "Pierna Izquierda",
                    ),
                )

            // Mock the repository and Health Connect
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { rehabRepository.getExercises() } returns flowOf(mockExercises)
            coEvery { healthConnectDataSource.hasAllPermissions() } returns true

            // Instantiate ViewModel
            val viewModel = ProgressViewModel(rehabRepository, healthConnectDataSource)

            viewModel.uiState.test {
                // Initial State
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                // Advance dispatcher to run init block coroutines
                advanceUntilIdle()

                // Use expectMostRecentItem to consume the final state
                val successState = expectMostRecentItem()
                assertFalse(successState.isLoading)
                assertEquals(7, successState.sessionsData.size)
                assertTrue(successState.isHealthConnectLinked)

                // The last item (index 6) should correspond to today (or generated mock if no session today)
                // The item corresponding to yesterday (index 5) should match the first mock session (id = 1L, ROM = 112f)
                val yesterdayItem = successState.sessionsData[5]
                assertEquals(1L, yesterdayItem.id)
                assertEquals(112f, yesterdayItem.averageRom)
                assertEquals("Flexión de Rodilla", yesterdayItem.exerciseName)
            }
        }

    @Test
    fun `loadProgressData should set error state when repository throws exception`() =
        runTest(testDispatcher) {
            // Mock getSessions to succeed so prepareMockSessionsIfNeeded doesn't crash the coroutine
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(emptyList())
            // Mock getExercises to throw, which runs inside combine and is caught by catch block
            coEvery { rehabRepository.getExercises() } returns flow { throw RuntimeException("Rehab error") }
            coEvery { healthConnectDataSource.hasAllPermissions() } returns false

            val viewModel = ProgressViewModel(rehabRepository, healthConnectDataSource)

            viewModel.uiState.test {
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                advanceUntilIdle()

                // Use expectMostRecentItem to consume intermediate loading updates
                val errorState = expectMostRecentItem()
                assertFalse(errorState.isLoading)
                assertEquals("Rehab error", errorState.error)
            }
        }

    @Test
    fun `onHealthConnectPermissionsResult should check permissions and reload data`() =
        runTest(testDispatcher) {
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(emptyList())
            coEvery { rehabRepository.getExercises() } returns flowOf(emptyList())
            coEvery { healthConnectDataSource.hasAllPermissions() } returns false

            val viewModel = ProgressViewModel(rehabRepository, healthConnectDataSource)

            viewModel.uiState.test {
                // Initial loading state
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                advanceUntilIdle()

                // Success State with HC linked = false (use expectMostRecentItem)
                val firstSuccessState = expectMostRecentItem()
                assertFalse(firstSuccessState.isLoading)
                assertFalse(firstSuccessState.isHealthConnectLinked)

                // Mock new permissions granted
                coEvery { healthConnectDataSource.hasAllPermissions() } returns true

                // Call permissions result callback
                viewModel.onHealthConnectPermissionsResult(setOf("health_permission"))

                advanceUntilIdle()

                // Final state after reload and permissions granted
                val finalSuccessState = expectMostRecentItem()
                assertFalse(finalSuccessState.isLoading)
                assertTrue(finalSuccessState.isHealthConnectLinked)
            }
        }
}
