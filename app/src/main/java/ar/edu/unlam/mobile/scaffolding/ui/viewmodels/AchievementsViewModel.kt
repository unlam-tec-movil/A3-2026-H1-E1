package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel
    @Inject
    constructor(
        private val achievementRepository: AchievementRepository,
    ) : ViewModel() {
        val achievements: StateFlow<List<Achievement>> =
            achievementRepository
                .getAchievements()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        init {
            seedAchievementsIfNeeded()
        }

        private fun seedAchievementsIfNeeded() {
            viewModelScope.launch {
                val currentList = achievementRepository.getAchievements().first()
                if (currentList.isEmpty()) {
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
    }
