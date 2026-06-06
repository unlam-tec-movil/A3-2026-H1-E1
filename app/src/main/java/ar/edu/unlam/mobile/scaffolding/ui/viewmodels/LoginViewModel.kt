package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.usecases.user.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UiState
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

// Form state
data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordVisible: Boolean = false,
)

// ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    fun onEmailChange(value: String) {
        _formState.value = _formState.value.copy(
            email = value,
            emailError = if (value.isNotBlank()) validateEmail(value) else null,
        )
    }

    fun onPasswordChange(value: String) {
        _formState.value = _formState.value.copy(
            password = value,
            passwordError = if (value.isNotBlank()) validatePassword(value) else null,
        )
    }

    fun onTogglePasswordVisibility() {
        _formState.value = _formState.value.copy(
            passwordVisible = !_formState.value.passwordVisible,
        )
    }

    fun onLogin() {
        val form = _formState.value

        // Validación completa antes de enviar
        val emailErr = validateEmail(form.email)
        val passErr = validatePassword(form.password)

        if (emailErr != null || passErr != null) {
            _formState.value = form.copy(emailError = emailErr, passwordError = passErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            _uiState.value = runCatching {
                loginUseCase(email = form.email.trim(), password = form.password)
                LoginUiState.Success
            }.getOrElse { e ->
                LoginUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun onErrorConsumed() {
        _uiState.value = LoginUiState.Idle
    }

    // Validaciones
    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El correo no puede estar vacío"
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email.trim())) return "Formato de correo inválido"
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña no puede estar vacía"
        return null
    }
}
