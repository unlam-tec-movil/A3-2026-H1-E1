package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import ar.edu.unlam.mobile.scaffolding.application.usecases.user.CreateUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val createUserUseCase = mockk<CreateUserUseCase>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(createUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Estado inicial
    @Test
    fun `initial uiState should be Idle`() {
        assertTrue(viewModel.uiState.value is RegisterUiState.Idle)
    }

    @Test
    fun `initial formState should have empty fields and no errors`() {
        val form = viewModel.formState.value
        assertEquals("", form.name)
        assertEquals("", form.email)
        assertEquals("", form.password)
        assertEquals("", form.confirmPassword)
        assertNull(form.nameError)
        assertNull(form.emailError)
        assertNull(form.passwordError)
        assertNull(form.confirmPasswordError)
        assertEquals(false, form.passwordVisible)
        assertEquals(false, form.confirmPasswordVisible)
    }

    // Validación de nombre
    @Test
    fun `onNameChange should set nameError when name is too short`() {
        viewModel.onNameChange("J")

        assertEquals("El nombre debe tener al menos 2 caracteres", viewModel.formState.value.nameError)
    }

    @Test
    fun `onNameChange should clear nameError when name is valid`() {
        viewModel.onNameChange("J")
        viewModel.onNameChange("Juan")

        assertNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `onNameChange should not set error for blank input`() {
        viewModel.onNameChange("")

        assertNull(viewModel.formState.value.nameError)
    }

    // Validación de email
    @Test
    fun `onEmailChange should set emailError when format is invalid`() {
        viewModel.onEmailChange("bad_email")

        assertEquals("Formato de correo inválido", viewModel.formState.value.emailError)
    }

    @Test
    fun `onEmailChange should clear emailError when format is valid`() {
        viewModel.onEmailChange("bad_email")
        viewModel.onEmailChange("valid@test.com")

        assertNull(viewModel.formState.value.emailError)
    }

    // Validación de contraseña
    @Test
    fun `onPasswordChange should set passwordError when shorter than 8 chars`() {
        viewModel.onPasswordChange("1234567")

        assertEquals("La contraseña debe tener al menos 8 caracteres", viewModel.formState.value.passwordError)
    }

    @Test
    fun `onPasswordChange should clear passwordError when password has 8 or more chars`() {
        viewModel.onPasswordChange("1234567")
        viewModel.onPasswordChange("12345678")

        assertNull(viewModel.formState.value.passwordError)
    }

    @Test
    fun `onPasswordChange should revalidate confirmPassword if already filled`() {
        viewModel.onConfirmPasswordChange("different_pass")
        viewModel.onPasswordChange("password123")

        assertEquals("Las contraseñas no coinciden", viewModel.formState.value.confirmPasswordError)
    }

    // Validación de confirmar contraseña
    @Test
    fun `onConfirmPasswordChange should set error when passwords do not match`() {
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("other_pass")

        assertEquals("Las contraseñas no coinciden", viewModel.formState.value.confirmPasswordError)
    }

    @Test
    fun `onConfirmPasswordChange should clear error when passwords match`() {
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("other_pass")
        viewModel.onConfirmPasswordChange("password123")

        assertNull(viewModel.formState.value.confirmPasswordError)
    }

    // Toggle visibilidad

    @Test
    fun `onTogglePasswordVisibility should flip passwordVisible`() {
        assertEquals(false, viewModel.formState.value.passwordVisible)
        viewModel.onTogglePasswordVisibility()
        assertEquals(true, viewModel.formState.value.passwordVisible)
        viewModel.onTogglePasswordVisibility()
        assertEquals(false, viewModel.formState.value.passwordVisible)
    }

    @Test
    fun `onToggleConfirmPasswordVisibility should flip confirmPasswordVisible`() {
        assertEquals(false, viewModel.formState.value.confirmPasswordVisible)
        viewModel.onToggleConfirmPasswordVisibility()
        assertEquals(true, viewModel.formState.value.confirmPasswordVisible)
    }

    // onRegister: validación previa

    @Test
    fun `onRegister should set all field errors and stay Idle when all fields are empty`() =
        runTest(testDispatcher) {
            viewModel.onRegister()
            advanceUntilIdle()

            val form = viewModel.formState.value
            assertEquals("El nombre no puede estar vacío", form.nameError)
            assertEquals("El correo no puede estar vacío", form.emailError)
            assertEquals("La contraseña no puede estar vacía", form.passwordError)
            assertEquals("Confirmá tu contraseña", form.confirmPasswordError)
            assertTrue(viewModel.uiState.value is RegisterUiState.Idle)
            coVerify(exactly = 0) { createUserUseCase(any(), any(), any()) }
        }

    @Test
    fun `onRegister should set passwordError when password is too short`() =
        runTest(testDispatcher) {
            viewModel.onNameChange("Juan")
            viewModel.onEmailChange("juan@test.com")
            viewModel.onPasswordChange("1234")
            viewModel.onConfirmPasswordChange("1234")
            viewModel.onRegister()
            advanceUntilIdle()

            assertEquals(
                "La contraseña debe tener al menos 8 caracteres",
                viewModel.formState.value.passwordError,
            )
            coVerify(exactly = 0) { createUserUseCase(any(), any(), any()) }
        }

    @Test
    fun `onRegister should set confirmPasswordError when passwords do not match`() =
        runTest(testDispatcher) {
            viewModel.onNameChange("Juan")
            viewModel.onEmailChange("juan@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("different")
            viewModel.onRegister()
            advanceUntilIdle()

            assertEquals("Las contraseñas no coinciden", viewModel.formState.value.confirmPasswordError)
            coVerify(exactly = 0) { createUserUseCase(any(), any(), any()) }
        }

    // onRegister: flujo exitoso
    @Test
    fun `onRegister should emit Success when CreateUserUseCase succeeds`() =
        runTest(testDispatcher) {
            coEvery { createUserUseCase("Juan", "juan@test.com", "password123") } returns "token"

            viewModel.onNameChange("Juan")
            viewModel.onEmailChange("juan@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onRegister()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is RegisterUiState.Success)
        }

    @Test
    fun `onRegister should call CreateUserUseCase with trimmed name and email`() =
        runTest(testDispatcher) {
            coEvery { createUserUseCase("Juan", "juan@test.com", "password123") } returns "token"

            viewModel.onNameChange("  Juan  ")
            viewModel.onEmailChange("  juan@test.com  ")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onRegister()
            advanceUntilIdle()

            coVerify { createUserUseCase("Juan", "juan@test.com", "password123") }
        }

    // onRegister: flujo de error
    @Test
    fun `onRegister should emit Error with El email ya esta registrado when use case throws`() =
        runTest(testDispatcher) {
            coEvery { createUserUseCase(any(), any(), any()) } throws
                Exception("El email ya está registrado")

            viewModel.onNameChange("Juan")
            viewModel.onEmailChange("juan@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onRegister()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is RegisterUiState.Error)
            assertEquals("El email ya está registrado", (state as RegisterUiState.Error).message)
        }

    @Test
    fun `onRegister should emit Error with fallback message when exception has no message`() =
        runTest(testDispatcher) {
            coEvery { createUserUseCase(any(), any(), any()) } throws Exception()

            viewModel.onNameChange("Juan")
            viewModel.onEmailChange("juan@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onRegister()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is RegisterUiState.Error)
            assertEquals("Error desconocido", (state as RegisterUiState.Error).message)
        }

    // onErrorConsumed
    @Test
    fun `onErrorConsumed should reset uiState to Idle`() =
        runTest(testDispatcher) {
            coEvery { createUserUseCase(any(), any(), any()) } throws Exception("El email ya está registrado")

            viewModel.onNameChange("Juan")
            viewModel.onEmailChange("juan@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onRegister()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is RegisterUiState.Error)

            viewModel.onErrorConsumed()

            assertTrue(viewModel.uiState.value is RegisterUiState.Idle)
        }
}
