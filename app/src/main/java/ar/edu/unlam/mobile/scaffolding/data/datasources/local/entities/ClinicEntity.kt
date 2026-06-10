package ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storedClinics")
data class ClinicEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val website: String,
    val lat: Double,
    val lng: Double,
)
