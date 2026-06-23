package ar.edu.unlam.mobile.scaffolding.ui.screens.dashboard

import app.cash.turbine.test
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `loadDashboardData should transition from loading to success`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            val mockSessions =
                listOf(
                    Session(1L, "user_imanol", "ex1", 1000L, 600, 85f, 10),
                )
            val mockAchievements =
                listOf(
                    Achievement("10k_steps", "Pasos Pasos", "Desc", false, null),
                )

            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(5000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(mockAchievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            viewModel.uiState.test {
                // Initial state (emitted immediately on connection because uiState is a StateFlow)
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                // Let coroutines run
                advanceUntilIdle()

                // Success state
                val successState = awaitItem()
                assertFalse(successState.isLoading)
                assertEquals("Imanol", successState.userName)
                assertEquals(85f, successState.maxRom)
                assertEquals(5000, successState.currentSteps)
                assertNull(successState.error)
            }
        }

    @Test
    fun `loadDashboardData should unlock first session achievement when sessions are not empty`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            val mockSessions =
                listOf(
                    Session(1L, "user_imanol", "ex1", 1000L, 600, 85f, 10),
                )
            val mockAchievements =
                listOf(
                    Achievement("first_session", "Primer Paso", "Desc", false, null),
                )

            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(5000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(mockAchievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            viewModel.uiState.test {
                // Initial state
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                advanceUntilIdle()

                // We expect the state updates. Because triggerNewUnlock calls _uiState.update,
                // the state will be updated with the newlyUnlockedAchievement.
                val finalState = expectMostRecentItem()
                assertFalse(finalState.isLoading)
                assertNotNull(finalState.newlyUnlockedAchievement)
                assertEquals("first_session", finalState.newlyUnlockedAchievement?.id)
                assertTrue(finalState.newlyUnlockedAchievement?.isUnlocked == true)
            }
        }

    @Test
    fun `loadDashboardData should unlock 10k steps achievement when steps are greater than or equal to 10000`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            val mockSessions = emptyList<Session>()
            val mockAchievements =
                listOf(
                    Achievement("10k_steps", "Pasos Legendarios", "Desc", false, null),
                )

            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(12000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(mockAchievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            viewModel.uiState.test {
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                advanceUntilIdle()

                val finalState = expectMostRecentItem()
                assertFalse(finalState.isLoading)
                assertNotNull(finalState.newlyUnlockedAchievement)
                assertEquals("10k_steps", finalState.newlyUnlockedAchievement?.id)
                assertTrue(finalState.newlyUnlockedAchievement?.isUnlocked == true)
            }
        }

    @Test
    fun `loadDashboardData should unlock master ROM achievement when a session ROM is greater than or equal to 120`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            val mockSessions =
                listOf(
                    Session(1L, "user_imanol", "ex1", 1000L, 600, 125f, 10),
                )
            val mockAchievements =
                listOf(
                    Achievement("master_rom", "Flexibilidad Suprema", "Desc", false, null),
                )

            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(5000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(mockAchievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            viewModel.uiState.test {
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                advanceUntilIdle()

                val finalState = expectMostRecentItem()
                assertFalse(finalState.isLoading)
                assertNotNull(finalState.newlyUnlockedAchievement)
                assertEquals("master_rom", finalState.newlyUnlockedAchievement?.id)
                assertTrue(finalState.newlyUnlockedAchievement?.isUnlocked == true)
            }
        }

    @Test
    fun `dismissUnlockPopup should set newlyUnlockedAchievement to null`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            val mockSessions = emptyList<Session>()
            val mockAchievements =
                listOf(
                    Achievement("10k_steps", "Pasos Legendarios", "Desc", false, null),
                )

            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(mockSessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(12000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(mockAchievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            viewModel.uiState.test {
                // Loading state
                awaitItem()

                advanceUntilIdle()

                // State with newlyUnlockedAchievement (use expectMostRecentItem to consume intermediate steps)
                val stateWithUnlock = expectMostRecentItem()
                assertNotNull(stateWithUnlock.newlyUnlockedAchievement)

                // Call dismiss
                viewModel.dismissUnlockPopup()

                // State after dismiss
                val stateAfterDismiss = awaitItem()
                assertNull(stateAfterDismiss.newlyUnlockedAchievement)
            }
        }

    @Test
    fun `loadDashboardData should update error state when repository throws exception`() =
        runTest(testDispatcher) {
            val mockUser = User("user_imanol", "Imanol", "imanol@example.com", "token")
            coEvery { userRepository.getUser() } returns flowOf(mockUser)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(emptyList())
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(5000)
            coEvery { achievementRepository.getAchievements() } returns
                flow { throw RuntimeException("Database error") }

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            viewModel.uiState.test {
                // Initial state
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)

                advanceUntilIdle()

                // Error state
                val errorState = awaitItem()
                assertFalse(errorState.isLoading)
                assertEquals("Database error", errorState.error)
            }
        }
}
