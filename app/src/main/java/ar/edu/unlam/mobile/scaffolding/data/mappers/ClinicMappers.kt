package ar.edu.unlam.mobile.scaffolding.data.mappers

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.ClinicEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic

fun Clinic.toEntity(): ClinicEntity =
    ClinicEntity(
        id = id,
        name = name,
        address = address,
        phone = phone,
        website = website,
        lat = lat,
        lng = lng,
    )

fun ClinicEntity.toDomain(): Clinic =
    Clinic(
        id = id,
        name = name,
        address = address,
        phone = phone,
        website = website,
        lat = lat,
        lng = lng,
    )
