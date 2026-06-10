package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UpdateUserUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(newName: String) {
            val trimmed = newName.trim()
            require(trimmed.length >= 2) { "El nombre debe tener al menos 2 caracteres" }

            val current =
                userRepository.getUser().firstOrNull()
                    ?: throw IllegalStateException("No hay usuario logueado")

            userRepository.saveUser(current.copy(name = trimmed))
        }
    }
