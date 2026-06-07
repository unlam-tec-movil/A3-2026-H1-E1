package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.preferences.SessionPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val userRepository: UserRepository,
        private val sessionPreferences: SessionPreferences,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
        ): String {
            val result =
                runCatching {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                }.getOrElse {
                    throw Exception("Credenciales incorrectas")
                }

            val firebaseUser =
                result.user
                    ?: throw Exception("Credenciales incorrectas")

            val token =
                firebaseUser.getIdToken(false).await().token
                    ?: throw Exception("No se pudo obtener el token de sesión")

            sessionPreferences.saveSessionToken(token)

            userRepository.saveUser(
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "",
                    email = firebaseUser.email ?: email,
                    sessionToken = token,
                ),
            )

            return token
        }
    }
