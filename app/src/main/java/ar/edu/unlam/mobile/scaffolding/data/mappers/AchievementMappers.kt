package ar.edu.unlam.mobile.scaffolding.data.mappers

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.AchievementEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement

fun AchievementEntity.toDomain(): Achievement =
    Achievement(
        id = id,
        title = title,
        description = description,
        isUnlocked = isUnlocked,
        unlockedAtTimestamp = unlockedAtTimestamp,
    )

fun Achievement.toEntity(): AchievementEntity =
    AchievementEntity(
        id = id,
        title = title,
        description = description,
        isUnlocked = isUnlocked,
        unlockedAtTimestamp = unlockedAtTimestamp,
    )
