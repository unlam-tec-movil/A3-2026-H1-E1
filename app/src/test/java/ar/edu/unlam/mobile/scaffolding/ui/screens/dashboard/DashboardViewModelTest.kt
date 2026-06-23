package ar.edu.unlam.mobile.scaffolding.ui.screens.dashboard

import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.DashboardViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val rehabRepository = mockk<RehabRepository>(relaxed = true)
    private val stepCounterDataSource = mockk<StepCounterDataSource>(relaxed = true)
    private val achievementRepository = mockk<AchievementRepository>(relaxed = true)

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
    fun `loadDashboardData should load user and session data correctly`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            val mockSessions =
                listOf(
                    Session(
                        id = 1L,
                        userId = "user_imanol",
                        exerciseId = "ex1",
                        dateTimestamp = 1000L,
                        durationSeconds = 600,
                        averageRom = 85f,
                        successfulReps = 10,
                    ),
                    Session(
                        id = 2L,
                        userId = "user_imanol",
                        exerciseId = "ex1",
                        dateTimestamp = 2000L,
                        durationSeconds = 900,
                        averageRom = 112f,
                        successfulReps = 15,
                    ),
                )

            // Mock the repository calls
            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(5000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(emptyList())

            // Instantiate ViewModel
            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            // Advance dispatcher to run init block coroutines
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertFalse(uiState.isLoading)
            assertEquals("Imanol", uiState.userName)
            assertEquals(112f, uiState.maxRom)
            assertEquals(mockSessions[1], uiState.lastSession)
            assertEquals(5000, uiState.currentSteps)
        }
}
