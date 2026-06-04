package ar.edu.unlam.mobile.scaffolding.domain.model

data class Clinic(
    val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val website: String,
    val lat: Double,
    val lng: Double,
)
