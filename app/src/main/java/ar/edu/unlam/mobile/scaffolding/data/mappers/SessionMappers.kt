package ar.edu.unlam.mobile.scaffolding.data.mappers

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.SessionEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Session

fun SessionEntity.toDomain(): Session =
    Session(
        id = id,
        userId = userId,
        exerciseId = exerciseId,
        dateTimestamp = dateTimestamp,
        durationSeconds = durationSeconds,
        averageRom = averageRom,
        successfulReps = successfulReps,
    )

fun Session.toEntity(): SessionEntity =
    SessionEntity(
        id = id,
        userId = userId,
        exerciseId = exerciseId,
        dateTimestamp = dateTimestamp,
        durationSeconds = durationSeconds,
        averageRom = averageRom,
        successfulReps = successfulReps,
    )
