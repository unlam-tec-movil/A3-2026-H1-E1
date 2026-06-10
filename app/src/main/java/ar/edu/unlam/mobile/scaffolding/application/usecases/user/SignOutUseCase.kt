package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.preferences.SessionPreferences
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SignOutUseCase
    @Inject
    constructor(
        private val sessionPreferences: SessionPreferences,
        private val userRepository: UserRepository,
        private val firebaseAuth: FirebaseAuth,
    ) {
        suspend operator fun invoke() {
            sessionPreferences.clearSession()
            userRepository.clearUser()
            firebaseAuth.signOut()
        }
    }
