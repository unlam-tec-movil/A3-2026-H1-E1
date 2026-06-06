package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.preferences.SessionPreferences
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {
    private val firebaseAuth = mockk<FirebaseAuth>()
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val sessionPreferences = mockk<SessionPreferences>(relaxed = true)

    private lateinit var useCase: LoginUseCase

    // Mocks de Firebase
    private val authResultTask = mockk<Task<AuthResult>>()
    private val authResult = mockk<AuthResult>()
    private val firebaseUser = mockk<FirebaseUser>()
    private val tokenResultTask = mockk<Task<GetTokenResult>>()
    private val tokenResult = mockk<GetTokenResult>()

    @Before
    fun setUp() {
        useCase =
            LoginUseCase(
                firebaseAuth = firebaseAuth,
                userRepository = userRepository,
                sessionPreferences = sessionPreferences,
            )

        // Encadenado completo: auth → user → token
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns authResultTask
        every { authResultTask.isComplete } returns true
        every { authResultTask.isCanceled } returns false
        every { authResultTask.exception } returns null
        every { authResultTask.result } returns authResult
        every { authResult.user } returns firebaseUser

        every { firebaseUser.uid } returns "uid_test"
        every { firebaseUser.email } returns "user@test.com"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseUser.getIdToken(false) } returns tokenResultTask

        every { tokenResultTask.isComplete } returns true
        every { tokenResultTask.isCanceled } returns false
        every { tokenResultTask.exception } returns null
        every { tokenResultTask.result } returns tokenResult
        every { tokenResult.token } returns "firebase_token_123"
    }

    @Test
    fun `invoke should return token on successful authentication`() =
        runTest {
            val token = useCase("user@test.com", "password123")

            assertEquals("firebase_token_123", token)
        }

    @Test
    fun `invoke should save token in SessionPreferences on success`() =
        runTest {
            useCase("user@test.com", "password123")

            coVerify { sessionPreferences.saveSessionToken("firebase_token_123") }
        }

    @Test
    fun `invoke should save user in UserRepository on success`() =
        runTest {
            useCase("user@test.com", "password123")

            coVerify {
                userRepository.saveUser(
                    match { user ->
                        user.id == "uid_test" &&
                            user.email == "user@test.com" &&
                            user.sessionToken == "firebase_token_123"
                    },
                )
            }
        }

    @Test
    fun `invoke should save user with email prefix as name when displayName is null`() =
        runTest {
            every { firebaseUser.displayName } returns null

            useCase("user@test.com", "password123")

            coVerify {
                userRepository.saveUser(
                    match { user -> user.name == "user" },
                )
            }
        }

    @Test
    fun `invoke should throw exception with Credenciales incorrectas when Firebase fails`() =
        runTest {
            every { authResultTask.isComplete } returns true
            every { authResultTask.isCanceled } returns false
            every { authResultTask.exception } returns Exception("INVALID_PASSWORD")
            every { authResultTask.result } throws Exception("INVALID_PASSWORD")

            val exception =
                runCatching {
                    useCase("user@test.com", "wrong_password")
                }.exceptionOrNull()

            assertEquals("Credenciales incorrectas", exception?.message)
        }

    @Test
    fun `invoke should throw exception when FirebaseUser is null after auth`() =
        runTest {
            every { authResult.user } returns null

            val exception =
                runCatching {
                    useCase("user@test.com", "password123")
                }.exceptionOrNull()

            assertEquals("Credenciales incorrectas", exception?.message)
        }

    @Test
    fun `invoke should throw exception when token is null`() =
        runTest {
            every { tokenResult.token } returns null

            val exception =
                runCatching {
                    useCase("user@test.com", "password123")
                }.exceptionOrNull()

            assertEquals("No se pudo obtener el token de sesión", exception?.message)
        }

    @Test
    fun `invoke should not save user when authentication fails`() =
        runTest {
            every { authResultTask.exception } returns Exception("INVALID_PASSWORD")
            every { authResultTask.result } throws Exception("INVALID_PASSWORD")

            runCatching { useCase("user@test.com", "wrong_password") }

            coVerify(exactly = 0) { userRepository.saveUser(any()) }
        }

    @Test
    fun `invoke should not save token when authentication fails`() =
        runTest {
            every { authResultTask.exception } returns Exception("INVALID_PASSWORD")
            every { authResultTask.result } throws Exception("INVALID_PASSWORD")

            runCatching { useCase("user@test.com", "wrong_password") }

            coVerify(exactly = 0) { sessionPreferences.saveSessionToken(any()) }
        }
}
