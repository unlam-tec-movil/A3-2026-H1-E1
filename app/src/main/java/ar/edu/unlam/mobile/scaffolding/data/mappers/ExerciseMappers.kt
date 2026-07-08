package ar.edu.unlam.mobile.scaffolding.data.mappers

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.ExerciseEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise

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
        illustrationRes = null, // illustrationRes sigue desactivado por ahora
    )

/*
private fun getIllustrationForExercise(id: String): Int {
    return when (id) {
        "bicep_curl" -> ar.edu.unlam.mobile.scaffolding.R.drawable.circle_mark
        "squat" -> ar.edu.unlam.mobile.scaffolding.R.drawable.circle_mark
        else -> ar.edu.unlam.mobile.scaffolding.R.drawable.circle_mark
    }
}
*/

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
