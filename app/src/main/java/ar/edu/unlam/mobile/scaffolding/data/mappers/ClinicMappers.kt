package ar.edu.unlam.mobile.scaffolding.data.mappers

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.AppClinicEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic

fun AppClinicEntity.toDomain(): Clinic =
    Clinic(
        id = id,
        name = name,
        address = address,
        phone = phone,
        website = website,
        lat = lat,
        lng = lng,
    )

fun Clinic.toAppEntity(): AppClinicEntity =
    AppClinicEntity(
        id = id,
        name = name,
        address = address,
        phone = phone,
        website = website,
        lat = lat,
        lng = lng,
    )
