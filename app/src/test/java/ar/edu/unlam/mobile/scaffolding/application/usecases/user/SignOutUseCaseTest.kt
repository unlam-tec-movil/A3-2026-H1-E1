package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignOutUseCaseTest {
    private val sessionPreferences = mockk<SessionPreferences>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)

    private lateinit var useCase: SignOutUseCase

    @Before
    fun setUp() {
        useCase =
            SignOutUseCase(
                sessionPreferences = sessionPreferences,
                userRepository = userRepository,
                firebaseAuth = firebaseAuth,
            )
    }

    // Happy path

    @Test
    fun `invoke should call clearSession on SessionPreferences`() =
        runTest {
            useCase()

            coVerify { sessionPreferences.clearSession() }
        }

    @Test
    fun `invoke should call clearUser on UserRepository`() =
        runTest {
            useCase()

            coVerify { userRepository.clearUser() }
        }

    @Test
    fun `invoke should call signOut on FirebaseAuth`() =
        runTest {
            useCase()

            verify { firebaseAuth.signOut() }
        }

    @Test
    fun `invoke should call all three operations on a single invocation`() =
        runTest {
            useCase()

            coVerify(exactly = 1) { sessionPreferences.clearSession() }
            coVerify(exactly = 1) { userRepository.clearUser() }
            verify(exactly = 1) { firebaseAuth.signOut() }
        }

    // Ordering: clearSession happens before clearUser

    @Test
    fun `invoke should clear DataStore token before clearing Room user`() =
        runTest {
            val callOrder = mutableListOf<String>()

            coEvery { sessionPreferences.clearSession() } answers { callOrder.add("clearSession") }
            coEvery { userRepository.clearUser() } answers { callOrder.add("clearUser") }

            useCase()

            assert(callOrder[0] == "clearSession") {
                "clearSession should be called before clearUser, but order was: $callOrder"
            }
            assert(callOrder[1] == "clearUser") {
                "clearUser should be second in order, but order was: $callOrder"
            }
        }

    // Resilience: Firebase failure should not prevent DataStore/Room cleanup

    @Test
    fun `invoke should still clear session and user even when firebaseAuth signOut is a no-op`() =
        runTest {
            // Firebase signOut is already relaxed (no-op) — verify the other two are still called
            useCase()

            coVerify { sessionPreferences.clearSession() }
            coVerify { userRepository.clearUser() }
        }

    // Idempotency

    @Test
    fun `invoke called twice should call each operation exactly twice`() =
        runTest {
            useCase()
            useCase()

            coVerify(exactly = 2) { sessionPreferences.clearSession() }
            coVerify(exactly = 2) { userRepository.clearUser() }
            verify(exactly = 2) { firebaseAuth.signOut() }
        }
}
