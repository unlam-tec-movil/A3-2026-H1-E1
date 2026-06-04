package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ClinicEntity

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
