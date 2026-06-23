package ar.edu.unlam.mobile.scaffolding.data.repositories

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.AchievementDao
import ar.edu.unlam.mobile.scaffolding.data.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.data.mappers.toEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl
    @Inject
    constructor(
        private val achievementDao: AchievementDao,
    ) : AchievementRepository {
        override fun getAchievements(): Flow<List<Achievement>> =
            achievementDao.getAllAchievements().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun unlockAchievement(id: String) {
            val achievement = achievementDao.getAchievementById(id)
            if (achievement != null && !achievement.isUnlocked) {
                achievementDao.updateUnlockStatus(id, true, System.currentTimeMillis())
            }
        }

        override suspend fun insertAchievements(achievements: List<Achievement>) {
            achievementDao.insertAchievements(achievements.map { it.toEntity() })
        }
    }
