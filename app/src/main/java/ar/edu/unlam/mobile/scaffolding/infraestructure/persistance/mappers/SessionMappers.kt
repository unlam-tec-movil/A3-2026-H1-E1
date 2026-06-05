package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers

import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.SessionEntity

fun SessionEntity.toDomain(): Session =
    Session(
        id = id,
        userId = userId,
        dateTimestamp = dateTimestamp,
        durationSeconds = durationSeconds,
        averageRom = averageRom,
        successfulReps = successfulReps,
    )

fun Session.toEntity(): SessionEntity =
    SessionEntity(
        id = id,
        userId = userId,
        dateTimestamp = dateTimestamp,
        durationSeconds = durationSeconds,
        averageRom = averageRom,
        successfulReps = successfulReps,
    )
