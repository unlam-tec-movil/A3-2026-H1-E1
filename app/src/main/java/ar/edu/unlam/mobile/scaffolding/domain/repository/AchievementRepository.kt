package ar.edu.unlam.mobile.scaffolding.domain.repository

import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAchievements(): Flow<List<Achievement>>

    suspend fun unlockAchievement(id: String)

    suspend fun insertAchievements(achievements: List<Achievement>)
}
