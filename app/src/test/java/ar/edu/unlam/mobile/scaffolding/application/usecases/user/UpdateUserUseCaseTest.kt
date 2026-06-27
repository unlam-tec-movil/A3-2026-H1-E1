package ar.edu.unlam.mobile.scaffolding.application.usecases.user

import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateUserUseCaseTest {
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var useCase: UpdateUserUseCase

    private val existingUser = User("uid_1", "Juan Pérez", "juan@test.com", "token_abc")

    @Before
    fun setUp() {
        useCase = UpdateUserUseCase(userRepository = userRepository)
        every { userRepository.getUser() } returns flowOf(existingUser)
    }

    // Happy path

    @Test
    fun `invoke should call saveUser with trimmed new name`() =
        runTest {
            useCase("Ana Gómez")

            coVerify { userRepository.saveUser(existingUser.copy(name = "Ana Gómez")) }
        }

    @Test
    fun `invoke should trim whitespace from the new name before saving`() =
        runTest {
            useCase("  María  ")

            coVerify { userRepository.saveUser(existingUser.copy(name = "María")) }
        }

    @Test
    fun `invoke should preserve all other user fields unchanged`() =
        runTest {
            useCase("Nuevo Nombre")

            coVerify {
                userRepository.saveUser(
                    match { user ->
                        user.id == "uid_1" &&
                            user.email == "juan@test.com" &&
                            user.sessionToken == "token_abc" &&
                            user.name == "Nuevo Nombre"
                    },
                )
            }
        }

    @Test
    fun `invoke should accept a name with exactly 2 characters`() =
        runTest {
            useCase("Jo")

            coVerify { userRepository.saveUser(existingUser.copy(name = "Jo")) }
        }

    // Validation failures

    @Test
    fun `invoke should throw IllegalArgumentException when name has only 1 character`() =
        runTest {
            val exception =
                runCatching { useCase("J") }.exceptionOrNull()

            assert(exception is IllegalArgumentException) {
                "Expected IllegalArgumentException but got ${exception?.javaClass?.simpleName}"
            }
            assertEquals(
                "El nombre debe tener al menos 2 caracteres",
                exception?.message,
            )
        }

    @Test
    fun `invoke should throw when name is blank after trimming`() =
        runTest {
            val exception =
                runCatching { useCase("   ") }.exceptionOrNull()

            assert(exception is IllegalArgumentException)
        }

    @Test
    fun `invoke should not call saveUser when name is too short`() =
        runTest {
            runCatching { useCase("X") }

            coVerify(exactly = 0) { userRepository.saveUser(any()) }
        }

    // No logged-in user

    @Test
    fun `invoke should throw IllegalStateException when no user is stored in Room`() =
        runTest {
            every { userRepository.getUser() } returns flowOf(null)

            val exception =
                runCatching { useCase("Juan Pablo") }.exceptionOrNull()

            assert(exception is IllegalStateException) {
                "Expected IllegalStateException but got ${exception?.javaClass?.simpleName}"
            }
            assertEquals("No hay usuario logueado", exception?.message)
        }

    @Test
    fun `invoke should not call saveUser when no user is logged in`() =
        runTest {
            every { userRepository.getUser() } returns flowOf(null)

            runCatching { useCase("Juan Pablo") }

            coVerify(exactly = 0) { userRepository.saveUser(any()) }
        }

    // Repository failure

    @Test
    fun `invoke should propagate exception when saveUser throws`() =
        runTest {
            coEvery { userRepository.saveUser(any()) } throws RuntimeException("DB error")

            val exception =
                runCatching { useCase("Juan Pablo") }.exceptionOrNull()

            assertEquals("DB error", exception?.message)
        }
}
