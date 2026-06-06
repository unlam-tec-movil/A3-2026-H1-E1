package ar.edu.unlam.mobile.scaffolding.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val sessionToken: String?,
)
