package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "Imanol",
    val maxRom: Float = 0f,
    val targetRom: Float = 120f,
    val currentSteps: Int = 7428,
    val targetSteps: Int = 10000,
    val lastSession: Session? = null,
    val unlockedAchievementsCount: Int = 0,
    val newlyUnlockedAchievement: Achievement? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val userRepository: UserRepository,
        private val rehabRepository: RehabRepository,
        private val stepCounterDataSource: StepCounterDataSource,
        private val achievementRepository: AchievementRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DashboardUiState())
        val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

        init {
            loadDashboardData()
        }

        private fun loadDashboardData() {
            viewModelScope.launch {
                // First, ensure we have a user and some sessions in the database for display
                prepareMockDataIfNeeded()

                // Observe user changes
                val userFlow = userRepository.getUser()

                // We use a default user id if none is found, but we will seed it
                val userId = "user_imanol"

                // Observe sessions changes
                val sessionsFlow = rehabRepository.getSessions(userId)

                // Observe achievements changes
                val achievementsFlow = achievementRepository.getAchievements()

                combine(
                    userFlow,
                    sessionsFlow,
                    stepCounterDataSource.getStepsFlow(),
                    achievementsFlow,
                ) { user, sessions, steps, achievements ->
                    val userName = user?.name ?: "Imanol"
                    val maxRom = sessions.maxOfOrNull { it.averageRom } ?: 0f
                    val lastSession = sessions.maxByOrNull { it.dateTimestamp }

                    // Seed mock achievements if not already present
                    seedAchievementsIfNeeded(achievements)

                    // Check and unlock achievements dynamically
                    checkAndUnlockAchievements(steps, sessions, achievements)

                    val unlockedCount = achievements.count { it.isUnlocked }

                    DashboardUiState(
                        userName = userName,
                        maxRom = maxRom,
                        targetRom = 120f, // Target ROM is 120 degrees
                        currentSteps = steps,
                        targetSteps = 10000,
                        lastSession = lastSession,
                        unlockedAchievementsCount = unlockedCount,
                        newlyUnlockedAchievement = _uiState.value.newlyUnlockedAchievement,
                        isLoading = false,
                        error = null,
                    )
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown error") }
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }

        private fun checkAndUnlockAchievements(
            steps: Int,
            sessions: List<Session>,
            achievements: List<Achievement>,
        ) {
            val firstSessionLocked = achievements.find { it.id == "first_session" }?.isUnlocked == false
            if (firstSessionLocked && sessions.isNotEmpty()) {
                viewModelScope.launch {
                    triggerNewUnlock(achievements.find { it.id == "first_session" })
                    achievementRepository.unlockAchievement("first_session")
                }
            }

            val stepCountLocked = achievements.find { it.id == "10k_steps" }?.isUnlocked == false
            if (stepCountLocked && steps >= 10000) {
                viewModelScope.launch {
                    triggerNewUnlock(achievements.find { it.id == "10k_steps" })
                    achievementRepository.unlockAchievement("10k_steps")
                }
            }

            val romLocked = achievements.find { it.id == "master_rom" }?.isUnlocked == false
            if (romLocked && sessions.any { it.averageRom >= 120f }) {
                viewModelScope.launch {
                    triggerNewUnlock(achievements.find { it.id == "master_rom" })
                    achievementRepository.unlockAchievement("master_rom")
                }
            }
        }

        private fun triggerNewUnlock(achievement: Achievement?) {
            achievement?.let {
                _uiState.update { state ->
                    state.copy(
                        newlyUnlockedAchievement =
                            it.copy(
                                isUnlocked = true,
                                unlockedAtTimestamp = System.currentTimeMillis(),
                            ),
                    )
                }
            }
        }

        fun dismissUnlockPopup() {
            _uiState.update { it.copy(newlyUnlockedAchievement = null) }
        }

        private fun seedAchievementsIfNeeded(list: List<Achievement>) {
            if (list.isEmpty()) {
                viewModelScope.launch {
                    val defaultAchievements =
                        listOf(
                            Achievement(
                                id = "10k_steps",
                                title = "Pasos Legendarios",
                                description = "Dar 10.000 pasos en un solo día.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                            Achievement(
                                id = "first_session",
                                title = "Primer Paso",
                                description = "Completar la primera sesión de rehabilitación.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                            Achievement(
                                id = "master_rom",
                                title = "Flexibilidad Suprema",
                                description = "Alcanzar un rango de movimiento (ROM) mayor o igual a 120°.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                        )
                    achievementRepository.insertAchievements(defaultAchievements)
                }
            }
        }

        private suspend fun prepareMockDataIfNeeded() {
            val user = userRepository.getUser().firstOrNull()
            if (user == null) {
                val defaultUser =
                    User(
                        id = "user_imanol",
                        name = "Imanol",
                        email = "imanol@example.com",
                        sessionToken = "token_123",
                    )
                userRepository.saveUser(defaultUser)
            }

            val sessions = rehabRepository.getSessions("user_imanol").firstOrNull() ?: emptyList()
            if (sessions.isEmpty()) {
                val currentTime = System.currentTimeMillis()
                val dayInMillis = 24 * 60 * 60 * 1000L

                val mockSessions =
                    listOf(
                        Session(
                            userId = "user_imanol",
                            exerciseId = "ex_knee_flexion",
                            dateTimestamp = currentTime - 3 * dayInMillis,
                            durationSeconds = 600,
                            averageRom = 85f,
                            successfulReps = 10,
                        ),
                        Session(
                            userId = "user_imanol",
                            exerciseId = "ex_knee_flexion",
                            dateTimestamp = currentTime - 2 * dayInMillis,
                            durationSeconds = 900,
                            averageRom = 98f,
                            successfulReps = 15,
                        ),
                        Session(
                            userId = "user_imanol",
                            exerciseId = "ex_knee_flexion",
                            dateTimestamp = currentTime - dayInMillis,
                            durationSeconds = 1200,
                            averageRom = 112f,
                            successfulReps = 20,
                        ),
                    )

                for (session in mockSessions) {
                    rehabRepository.saveSession(session)
                }
            }
        }
    }
