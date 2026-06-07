package ar.edu.unlam.mobile.scaffolding.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val targetAngle: Float,
    val repetitions: Int,
    val sets: Int,
)
