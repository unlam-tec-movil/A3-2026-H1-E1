package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers

import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ExerciseEntity

fun ExerciseEntity.toDomain(): Exercise =
    Exercise(
        id = id,
        name = name,
        description = description,
        targetAngle = targetAngle,
        repetitions = repetitions,
        sets = sets,
    )

fun Exercise.toEntity(): ExerciseEntity =
    ExerciseEntity(
        id = id,
        name = name,
        description = description,
        targetAngle = targetAngle,
        repetitions = repetitions,
        sets = sets,
    )
