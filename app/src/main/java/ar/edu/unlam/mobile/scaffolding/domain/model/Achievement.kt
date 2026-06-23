package ar.edu.unlam.mobile.scaffolding.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val unlockedAtTimestamp: Long?,
)
