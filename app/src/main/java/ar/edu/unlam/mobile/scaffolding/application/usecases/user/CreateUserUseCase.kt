package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Registra un nuevo usuario con Firebase Auth, guarda sus datos en Room
 * y persiste el token de sesión en DataStore.
 */
class CreateUserUseCase
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val userRepository: UserRepository,
        private val sessionPreferences: SessionPreferences,
    ) {
        suspend operator fun invoke(
            name: String,
            email: String,
            password: String,
        ): String {
            // Verificar si el email existe en Room antes de llamar a Firebase
            val existing = userRepository.getUserByEmail(email.trim())
            if (existing != null) throw Exception("El email ya está registrado")

            // Crear cuenta en Firebase Auth
            val result =
                runCatching {
                    firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
                }.getOrElse { e ->
                    throw Exception(e.message ?: "No se pudo crear la cuenta")
                }

            val firebaseUser =
                result.user
                    ?: throw Exception("No se pudo crear la cuenta")

            val token =
                firebaseUser.getIdToken(false).await().token
                    ?: throw Exception("No se pudo obtener el token de sesión")

            // Persistir token en DataStore
            sessionPreferences.saveSessionToken(token)

            // Guardar usuario en Room
            userRepository.saveUser(
                User(
                    id = firebaseUser.uid,
                    name = name.trim(),
                    email = firebaseUser.email ?: email.trim(),
                    sessionToken = token,
                ),
            )

            return token
        }
    }
