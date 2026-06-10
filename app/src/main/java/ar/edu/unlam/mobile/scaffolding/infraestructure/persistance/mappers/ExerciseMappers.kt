package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers

import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ExerciseEntity

fun ExerciseEntity.toDomain(): Exercise =
    Exercise(
        id = id,
        name = name,
        description = description,
        targetJoints = targetJoints.split(","),
        startAngle = startAngle,
        endAngle = endAngle,
        toleranceIdeal = toleranceIdeal,
        toleranceWarning = toleranceWarning,
        repetitions = repetitions,
        sets = sets,
        bodyPart = bodyPart,
    )

fun Exercise.toEntity(): ExerciseEntity =
    ExerciseEntity(
        id = id,
        name = name,
        description = description,
        targetJoints = targetJoints.joinToString(","),
        startAngle = startAngle,
        endAngle = endAngle,
        toleranceIdeal = toleranceIdeal,
        toleranceWarning = toleranceWarning,
        repetitions = repetitions,
        sets = sets,
        bodyPart = bodyPart,
    )
