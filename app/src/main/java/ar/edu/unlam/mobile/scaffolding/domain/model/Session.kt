package ar.edu.unlam.mobile.scaffolding.domain.model

data class Session(
    val id: Long = 0L,
    val userId: String,
    val exerciseId: String,
    val dateTimestamp: Long,
    val durationSeconds: Long,
    val averageRom: Float,
    val successfulReps: Int,
)
