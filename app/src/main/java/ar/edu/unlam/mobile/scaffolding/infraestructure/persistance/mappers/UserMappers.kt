package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers

import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.UserEntity

fun UserEntity.toDomain(): User =
    User(
        id = id,
        name = name,
        email = email,
        sessionToken = sessionToken,
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        name = name,
        email = email,
        sessionToken = sessionToken,
    )
