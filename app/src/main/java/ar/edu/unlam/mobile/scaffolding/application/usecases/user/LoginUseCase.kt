package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.AchievementRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val userRepository: UserRepository,
        private val sessionPreferences: SessionPreferences,
        private val rehabRepository: RehabRepository,
        private val achievementRepository: AchievementRepository,
        private val stepCounterDataSource: StepCounterDataSource,
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

        suspend fun loginWithMock(email: String) {
            val token = "mock_token_${email.substringBefore("@")}"
            sessionPreferences.saveSessionToken(token)

            val user =
                when (email) {
                    "juan.perez@gambapp.com" ->
                        User(
                            id = "user_juan",
                            name = "Juan Pérez",
                            email = email,
                            sessionToken = token,
                        )
                    "maria.rodriguez@gambapp.com" ->
                        User(
                            id = "user_maria",
                            name = "María Rodríguez",
                            email = email,
                            sessionToken = token,
                        )
                    "carlos.gomez@gambapp.com" ->
                        User(
                            id = "user_carlos",
                            name = "Carlos Gómez",
                            email = email,
                            sessionToken = token,
                        )
                    else -> throw Exception("Usuario mock no encontrado")
                }

            // Save user to repository
            userRepository.saveUser(user)

            // Clear and seed mock data
            val currentTime = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L

            rehabRepository.clearSessionsByUser(user.id)

            when (user.id) {
                "user_juan" -> {
                    stepCounterDataSource.saveStepsToday(1500)

                    val juanSessions =
                        listOf(
                            Session(
                                userId = user.id,
                                exerciseId = "ex_knee_flexion",
                                dateTimestamp = currentTime - 3 * dayInMillis,
                                durationSeconds = 300,
                                averageRom = 65f,
                                successfulReps = 5,
                            ),
                            Session(
                                userId = user.id,
                                exerciseId = "ex_knee_flexion",
                                dateTimestamp = currentTime - 2 * dayInMillis,
                                durationSeconds = 360,
                                averageRom = 70f,
                                successfulReps = 6,
                            ),
                            Session(
                                userId = user.id,
                                exerciseId = "ex_knee_flexion",
                                dateTimestamp = currentTime - dayInMillis,
                                durationSeconds = 480,
                                averageRom = 72f,
                                successfulReps = 8,
                            ),
                        )
                    for (session in juanSessions) {
                        rehabRepository.saveSession(session)
                    }

                    achievementRepository.insertAchievements(
                        listOf(
                            Achievement(
                                id = "10k_steps",
                                title = "Pasos Legendarios",
                                description = "Dar 10.000 pasos en un solo día.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                            Achievement(
                                id = "first_session",
                                title = "Primer Paso",
                                description = "Completar la primera sesión de rehabilitación.",
                                isUnlocked = true,
                                unlockedAtTimestamp = currentTime - 3 * dayInMillis,
                            ),
                            Achievement(
                                id = "master_rom",
                                title = "Flexibilidad Suprema",
                                description = "Alcanzar un rango de movimiento (ROM) mayor o igual a 120°.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                        ),
                    )
                }
                "user_maria" -> {
                    stepCounterDataSource.saveStepsToday(5500)

                    val mariaSessions =
                        listOf(
                            Session(
                                userId = user.id,
                                exerciseId = "ex_knee_extension",
                                dateTimestamp = currentTime - 5 * dayInMillis,
                                durationSeconds = 600,
                                averageRom = 80f,
                                successfulReps = 10,
                            ),
                            Session(
                                userId = user.id,
                                exerciseId = "ex_knee_extension",
                                dateTimestamp = currentTime - 4 * dayInMillis,
                                durationSeconds = 720,
                                averageRom = 85f,
                                successfulReps = 12,
                            ),
                            Session(
                                userId = user.id,
                                exerciseId = "ex_knee_extension",
                                dateTimestamp = currentTime - 3 * dayInMillis,
                                durationSeconds = 720,
                                averageRom = 90f,
                                successfulReps = 12,
                            ),
                            Session(
                                userId = user.id,
                                exerciseId = "ex_assisted_squats",
                                dateTimestamp = currentTime - 2 * dayInMillis,
                                durationSeconds = 900,
                                averageRom = 95f,
                                successfulReps = 15,
                            ),
                            Session(
                                userId = user.id,
                                exerciseId = "ex_assisted_squats",
                                dateTimestamp = currentTime - dayInMillis,
                                durationSeconds = 900,
                                averageRom = 102f,
                                successfulReps = 15,
                            ),
                        )
                    for (session in mariaSessions) {
                        rehabRepository.saveSession(session)
                    }

                    achievementRepository.insertAchievements(
                        listOf(
                            Achievement(
                                id = "10k_steps",
                                title = "Pasos Legendarios",
                                description = "Dar 10.000 pasos en un solo día.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                            Achievement(
                                id = "first_session",
                                title = "Primer Paso",
                                description = "Completar la primera sesión de rehabilitación.",
                                isUnlocked = true,
                                unlockedAtTimestamp = currentTime - 5 * dayInMillis,
                            ),
                            Achievement(
                                id = "master_rom",
                                title = "Flexibilidad Suprema",
                                description = "Alcanzar un rango de movimiento (ROM) mayor o igual a 120°.",
                                isUnlocked = false,
                                unlockedAtTimestamp = null,
                            ),
                        ),
                    )
                }
                "user_carlos" -> {
                    stepCounterDataSource.saveStepsToday(12500)

                    val carlosSessions =
                        (1..10).map { i ->
                            val date = currentTime - (11 - i) * dayInMillis
                            val exercise = if (i % 2 == 0) "ex_heel_raises" else "ex_knee_flexion"
                            val rom = 85f + (i * 3.7f)
                            Session(
                                userId = user.id,
                                exerciseId = exercise,
                                dateTimestamp = date,
                                durationSeconds = 600L + i * 60L,
                                averageRom = rom.coerceAtMost(180f),
                                successfulReps = 8 + i,
                            )
                        }
                    for (session in carlosSessions) {
                        rehabRepository.saveSession(session)
                    }

                    achievementRepository.insertAchievements(
                        listOf(
                            Achievement(
                                id = "10k_steps",
                                title = "Pasos Legendarios",
                                description = "Dar 10.000 pasos en un solo día.",
                                isUnlocked = true,
                                unlockedAtTimestamp = currentTime - dayInMillis,
                            ),
                            Achievement(
                                id = "first_session",
                                title = "Primer Paso",
                                description = "Completar la primera sesión de rehabilitación.",
                                isUnlocked = true,
                                unlockedAtTimestamp = currentTime - 10 * dayInMillis,
                            ),
                            Achievement(
                                id = "master_rom",
                                title = "Flexibilidad Suprema",
                                description = "Alcanzar un rango de movimiento (ROM) mayor o igual a 120°.",
                                isUnlocked = true,
                                unlockedAtTimestamp = currentTime - dayInMillis,
                            ),
                        ),
                    )
                }
            }
        }
    }
