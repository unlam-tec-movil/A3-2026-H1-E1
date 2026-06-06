package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import ar.edu.unlam.mobile.scaffolding.application.usecases.user.LoginUseCase
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
class LoginViewModelTest {
    private val loginUseCase = mockk<LoginUseCase>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Estado inicial

    @Test
    fun `initial uiState should be Idle`() {
        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }

    @Test
    fun `initial formState should have empty fields and no errors`() {
        val form = viewModel.formState.value
        assertEquals("", form.email)
        assertEquals("", form.password)
        assertNull(form.emailError)
        assertNull(form.passwordError)
        assertEquals(false, form.passwordVisible)
    }

    // Validación de email

    @Test
    fun `onEmailChange should set emailError when format is invalid`() {
        viewModel.onEmailChange("not_an_email")

        assertEquals("Formato de correo inválido", viewModel.formState.value.emailError)
    }

    @Test
    fun `onEmailChange should clear emailError when format is valid`() {
        viewModel.onEmailChange("not_an_email")
        viewModel.onEmailChange("valid@test.com")

        assertNull(viewModel.formState.value.emailError)
    }

    @Test
    fun `onEmailChange should not set error for blank input`() {
        viewModel.onEmailChange("")

        assertNull(viewModel.formState.value.emailError)
    }

    // Validación de contraseña

    @Test
    fun `onPasswordChange should set passwordError when password is blank`() {
        viewModel.onPasswordChange("a")
        viewModel.onPasswordChange("")

        // Blank no dispara validación (igual que email)
        assertNull(viewModel.formState.value.passwordError)
    }

    @Test
    fun `onPasswordChange should clear passwordError when password is not blank`() {
        viewModel.onPasswordChange("pass")

        assertNull(viewModel.formState.value.passwordError)
    }

    // Toggle visibilidad contraseña

    @Test
    fun `onTogglePasswordVisibility should flip passwordVisible`() {
        assertEquals(false, viewModel.formState.value.passwordVisible)
        viewModel.onTogglePasswordVisibility()
        assertEquals(true, viewModel.formState.value.passwordVisible)
        viewModel.onTogglePasswordVisibility()
        assertEquals(false, viewModel.formState.value.passwordVisible)
    }

    // Login

    @Test
    fun `onLogin should set form errors and stay Idle when email is empty`() =
        runTest(testDispatcher) {
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()
            advanceUntilIdle()

            assertEquals("El correo no puede estar vacío", viewModel.formState.value.emailError)
            assertTrue(viewModel.uiState.value is LoginUiState.Idle)
            coVerify(exactly = 0) { loginUseCase(any(), any()) }
        }

    @Test
    fun `onLogin should set form errors and stay Idle when password is empty`() =
        runTest(testDispatcher) {
            viewModel.onEmailChange("user@test.com")
            viewModel.onLogin()
            advanceUntilIdle()

            assertEquals("La contraseña no puede estar vacía", viewModel.formState.value.passwordError)
            assertTrue(viewModel.uiState.value is LoginUiState.Idle)
            coVerify(exactly = 0) { loginUseCase(any(), any()) }
        }

    @Test
    fun `onLogin should set email format error when email is invalid`() =
        runTest(testDispatcher) {
            viewModel.onEmailChange("bad_email")
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()
            advanceUntilIdle()

            assertEquals("Formato de correo inválido", viewModel.formState.value.emailError)
            assertTrue(viewModel.uiState.value is LoginUiState.Idle)
        }


    @Test
    fun `onLogin should emit Success when LoginUseCase succeeds`() =
        runTest(testDispatcher) {
            coEvery { loginUseCase("user@test.com", "password123") } returns "firebase_token"

            viewModel.onEmailChange("user@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is LoginUiState.Success)
        }

    @Test
    fun `onLogin should call LoginUseCase with trimmed email`() =
        runTest(testDispatcher) {
            coEvery { loginUseCase("user@test.com", "password123") } returns "token"

            viewModel.onEmailChange("  user@test.com  ")
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()
            advanceUntilIdle()

            coVerify { loginUseCase("user@test.com", "password123") }
        }


    @Test
    fun `onLogin should emit Error with Credenciales incorrectas when LoginUseCase throws`() =
        runTest(testDispatcher) {
            coEvery { loginUseCase(any(), any()) } throws Exception("Credenciales incorrectas")

            viewModel.onEmailChange("user@test.com")
            viewModel.onPasswordChange("wrong_password")
            viewModel.onLogin()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is LoginUiState.Error)
            assertEquals("Credenciales incorrectas", (state as LoginUiState.Error).message)
        }

    @Test
    fun `onLogin should emit Error with fallback message when exception has no message`() =
        runTest(testDispatcher) {
            coEvery { loginUseCase(any(), any()) } throws Exception()

            viewModel.onEmailChange("user@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is LoginUiState.Error)
            assertEquals("Error desconocido", (state as LoginUiState.Error).message)
        }


    @Test
    fun `onErrorConsumed should reset uiState to Idle`() =
        runTest(testDispatcher) {
            coEvery { loginUseCase(any(), any()) } throws Exception("Credenciales incorrectas")

            viewModel.onEmailChange("user@test.com")
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is LoginUiState.Error)

            viewModel.onErrorConsumed()

            assertTrue(viewModel.uiState.value is LoginUiState.Idle)
        }
}
