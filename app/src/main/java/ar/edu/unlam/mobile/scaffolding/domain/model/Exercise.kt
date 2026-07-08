package ar.edu.unlam.mobile.scaffolding.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    // e.g., ["RIGHT_SHOULDER", "RIGHT_ELBOW", "RIGHT_WRIST"]
    val targetJoints: List<String>,
    val startAngle: Float,
    val endAngle: Float,
    val toleranceIdeal: Float = 5f,
    val toleranceWarning: Float = 15f,
    val repetitions: Int,
    val sets: Int,
    // e.g., "Brazo Derecho"
    val bodyPart: String,
    val illustrationRes: Int? = null,
)
