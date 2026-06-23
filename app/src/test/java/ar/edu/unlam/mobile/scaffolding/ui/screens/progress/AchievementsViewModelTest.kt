package ar.edu.unlam.mobile.scaffolding.ui.screens.progress

import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.AchievementsViewModel
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.DashboardViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {
    private val achievementRepository = mockk<AchievementRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val rehabRepository = mockk<RehabRepository>(relaxed = true)
    private val stepCounterDataSource = mockk<StepCounterDataSource>(relaxed = true)

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
    fun `AchievementsViewModel should seed achievements if empty on init`() =
        runTest(testDispatcher) {
            coEvery { achievementRepository.getAchievements() } returns flowOf(emptyList())

            val viewModel = AchievementsViewModel(achievementRepository)

            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.achievements.collect {}
                }

            advanceUntilIdle()

            coVerify(exactly = 1) { achievementRepository.insertAchievements(any()) }

            collectJob.cancel()
        }

    @Test
    fun `AchievementsViewModel should not seed achievements if already present on init`() =
        runTest(testDispatcher) {
            val existing =
                listOf(
                    Achievement("10k_steps", "Pasos Legendarios", "Desc", false, null),
                )
            coEvery { achievementRepository.getAchievements() } returns flowOf(existing)

            val viewModel = AchievementsViewModel(achievementRepository)

            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.achievements.collect {}
                }

            advanceUntilIdle()

            coVerify(exactly = 0) { achievementRepository.insertAchievements(any()) }
            assertEquals(existing, viewModel.achievements.value)

            collectJob.cancel()
        }

    @Test
    fun `DashboardViewModel should unlock 10k_steps when steps exceed 10000`() =
        runTest(testDispatcher) {
            val achievements =
                listOf(
                    Achievement("10k_steps", "Pasos", "Desc", false, null),
                    Achievement("first_session", "Sesion", "Desc", false, null),
                    Achievement("master_rom", "ROM", "Desc", false, null),
                )
            val user = User("user_imanol", "Imanol", "imanol@example.com", null)
            val sessions = emptyList<Session>()

            coEvery { userRepository.getUser() } returns flowOf(user)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(sessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(12500)
            coEvery { achievementRepository.getAchievements() } returns flowOf(achievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.uiState.collect {}
                }

            advanceUntilIdle()

            // Check if unlockAchievement was called for 10k steps
            coVerify { achievementRepository.unlockAchievement("10k_steps") }

            // The newly unlocked achievement state should be set to display celebration popup
            assertNotNull(viewModel.uiState.value.newlyUnlockedAchievement)
            assertEquals(
                "10k_steps",
                viewModel.uiState.value.newlyUnlockedAchievement
                    ?.id,
            )

            collectJob.cancel()
        }

    @Test
    fun `DashboardViewModel should unlock master_rom when session ROM exceeds 120`() =
        runTest(testDispatcher) {
            val achievements =
                listOf(
                    Achievement("10k_steps", "Pasos", "Desc", false, null),
                    Achievement("first_session", "Sesion", "Desc", false, null),
                    Achievement("master_rom", "ROM", "Desc", false, null),
                )
            val user = User("user_imanol", "Imanol", "imanol@example.com", null)
            val sessions =
                listOf(
                    Session(1L, "user_imanol", "ex_knee_flexion", System.currentTimeMillis(), 600, 125f, 10),
                )

            coEvery { userRepository.getUser() } returns flowOf(user)
            coEvery { rehabRepository.getSessions("user_imanol") } returns flowOf(sessions)
            coEvery { stepCounterDataSource.getStepsFlow() } returns flowOf(3000)
            coEvery { achievementRepository.getAchievements() } returns flowOf(achievements)

            val viewModel =
                DashboardViewModel(userRepository, rehabRepository, stepCounterDataSource, achievementRepository)

            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.uiState.collect {}
                }

            advanceUntilIdle()

            // Verify it unlocks both first_session and master_rom
            coVerify { achievementRepository.unlockAchievement("first_session") }
            coVerify { achievementRepository.unlockAchievement("master_rom") }

            collectJob.cancel()
        }
}
