package ar.edu.unlam.mobile.scaffolding.data.mappers

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.UserEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.User

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
