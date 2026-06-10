package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateUserUseCaseTest {
    private val firebaseAuth = mockk<FirebaseAuth>()
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val sessionPreferences = mockk<SessionPreferences>(relaxed = true)

    private lateinit var useCase: CreateUserUseCase

    private val authResultTask = mockk<Task<AuthResult>>()
    private val authResult = mockk<AuthResult>()
    private val firebaseUser = mockk<FirebaseUser>()
    private val tokenResultTask = mockk<Task<GetTokenResult>>()
    private val tokenResult = mockk<GetTokenResult>()

    @Before
    fun setUp() {
        useCase =
            CreateUserUseCase(
                firebaseAuth = firebaseAuth,
                userRepository = userRepository,
                sessionPreferences = sessionPreferences,
            )

        // Email no existe en Room por defecto
        coEvery { userRepository.getUserByEmail(any()) } returns null

        // Firebase createUserWithEmailAndPassword exitoso
        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns authResultTask
        every { authResultTask.isComplete } returns true
        every { authResultTask.isCanceled } returns false
        every { authResultTask.exception } returns null
        every { authResultTask.result } returns authResult
        every { authResult.user } returns firebaseUser

        every { firebaseUser.uid } returns "new_uid_123"
        every { firebaseUser.email } returns "nuevo@test.com"
        every { firebaseUser.getIdToken(false) } returns tokenResultTask

        every { tokenResultTask.isComplete } returns true
        every { tokenResultTask.isCanceled } returns false
        every { tokenResultTask.exception } returns null
        every { tokenResultTask.result } returns tokenResult
        every { tokenResult.token } returns "new_token_abc"
    }

    @Test
    fun `invoke should return token on successful registration`() =
        runTest {
            val token = useCase("Juan Pérez", "nuevo@test.com", "password123")

            assertEquals("new_token_abc", token)
        }

    @Test
    fun `invoke should save token in SessionPreferences on success`() =
        runTest {
            useCase("Juan Pérez", "nuevo@test.com", "password123")

            coVerify { sessionPreferences.saveSessionToken("new_token_abc") }
        }

    @Test
    fun `invoke should save user in UserRepository with correct data`() =
        runTest {
            useCase("Juan Pérez", "nuevo@test.com", "password123")

            coVerify {
                userRepository.saveUser(
                    match { user ->
                        user.id == "new_uid_123" &&
                            user.name == "Juan Pérez" &&
                            user.email == "nuevo@test.com" &&
                            user.sessionToken == "new_token_abc"
                    },
                )
            }
        }

    @Test
    fun `invoke should trim name and email before saving`() =
        runTest {
            useCase("  Juan Pérez  ", "  nuevo@test.com  ", "password123")

            coVerify {
                userRepository.saveUser(
                    match { user ->
                        user.name == "Juan Pérez" && user.email == "nuevo@test.com"
                    },
                )
            }
        }

    @Test
    fun `invoke should throw El email ya esta registrado when email exists in Room`() =
        runTest {
            coEvery { userRepository.getUserByEmail("nuevo@test.com") } returns
                User("existing_id", "Otro", "nuevo@test.com", null)

            val exception =
                runCatching {
                    useCase("Juan Pérez", "nuevo@test.com", "password123")
                }.exceptionOrNull()

            assertEquals("El email ya está registrado", exception?.message)
        }

    @Test
    fun `invoke should not call Firebase when email already exists in Room`() =
        runTest {
            coEvery { userRepository.getUserByEmail(any()) } returns
                User("existing_id", "Otro", "nuevo@test.com", null)

            runCatching { useCase("Juan Pérez", "nuevo@test.com", "password123") }

            coVerify(exactly = 0) { firebaseAuth.createUserWithEmailAndPassword(any(), any()) }
        }

    @Test
    fun `invoke should throw exception when Firebase registration fails`() =
        runTest {
            every { authResultTask.exception } returns Exception("EMAIL_ALREADY_IN_USE")
            every { authResultTask.result } throws Exception("EMAIL_ALREADY_IN_USE")

            val exception =
                runCatching {
                    useCase("Juan Pérez", "nuevo@test.com", "password123")
                }.exceptionOrNull()

            assertEquals("EMAIL_ALREADY_IN_USE", exception?.message)
        }

    @Test
    fun `invoke should throw exception when FirebaseUser is null after registration`() =
        runTest {
            every { authResult.user } returns null

            val exception =
                runCatching {
                    useCase("Juan Pérez", "nuevo@test.com", "password123")
                }.exceptionOrNull()

            assertEquals("No se pudo crear la cuenta", exception?.message)
        }

    @Test
    fun `invoke should throw exception when token is null`() =
        runTest {
            every { tokenResult.token } returns null

            val exception =
                runCatching {
                    useCase("Juan Pérez", "nuevo@test.com", "password123")
                }.exceptionOrNull()

            assertEquals("No se pudo obtener el token de sesión", exception?.message)
        }

    @Test
    fun `invoke should not save user when Firebase registration fails`() =
        runTest {
            every { authResultTask.exception } returns Exception("FIREBASE_ERROR")
            every { authResultTask.result } throws Exception("FIREBASE_ERROR")

            runCatching { useCase("Juan Pérez", "nuevo@test.com", "password123") }

            coVerify(exactly = 0) { userRepository.saveUser(any()) }
        }

    @Test
    fun `invoke should not save token when Firebase registration fails`() =
        runTest {
            every { authResultTask.exception } returns Exception("FIREBASE_ERROR")
            every { authResultTask.result } throws Exception("FIREBASE_ERROR")

            runCatching { useCase("Juan Pérez", "nuevo@test.com", "password123") }

            coVerify(exactly = 0) { sessionPreferences.saveSessionToken(any()) }
        }
}
