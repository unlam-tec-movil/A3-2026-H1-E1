package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.usecases.user.CreateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UiState
sealed interface RegisterUiState {
    object Idle : RegisterUiState

    object Loading : RegisterUiState

    object Success : RegisterUiState

    data class Error(
        val message: String,
    ) : RegisterUiState
}

// Form state
data class RegisterFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
)

// ViewModel
@HiltViewModel
class RegisterViewModel
    @Inject
    constructor(
        private val createUserUseCase: CreateUserUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
        val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

        private val _formState = MutableStateFlow(RegisterFormState())
        val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

        fun onNameChange(value: String) {
            _formState.value =
                _formState.value.copy(
                    name = value,
                    nameError = if (value.isNotBlank()) validateName(value) else null,
                )
        }

        fun onEmailChange(value: String) {
            _formState.value =
                _formState.value.copy(
                    email = value,
                    emailError = if (value.isNotBlank()) validateEmail(value) else null,
                )
        }

        fun onPasswordChange(value: String) {
            _formState.value =
                _formState.value.copy(
                    password = value,
                    passwordError = if (value.isNotBlank()) validatePassword(value) else null,
                    // Revalidar confirmación si ya fue tocada
                    confirmPasswordError =
                        if (_formState.value.confirmPassword.isNotBlank()) {
                            validateConfirmPassword(value, _formState.value.confirmPassword)
                        } else {
                            _formState.value.confirmPasswordError
                        },
                )
        }

        fun onConfirmPasswordChange(value: String) {
            _formState.value =
                _formState.value.copy(
                    confirmPassword = value,
                    confirmPasswordError =
                        if (value.isNotBlank()) {
                            validateConfirmPassword(_formState.value.password, value)
                        } else {
                            null
                        },
                )
        }

        fun onTogglePasswordVisibility() {
            _formState.value =
                _formState.value.copy(passwordVisible = !_formState.value.passwordVisible)
        }

        fun onToggleConfirmPasswordVisibility() {
            _formState.value =
                _formState.value.copy(
                    confirmPasswordVisible = !_formState.value.confirmPasswordVisible,
                )
        }

        fun onRegister() {
            val form = _formState.value

            val nameErr = validateName(form.name)
            val emailErr = validateEmail(form.email)
            val passErr = validatePassword(form.password)
            val confirmErr = validateConfirmPassword(form.password, form.confirmPassword)

            if (nameErr != null || emailErr != null || passErr != null || confirmErr != null) {
                _formState.value =
                    form.copy(
                        nameError = nameErr,
                        emailError = emailErr,
                        passwordError = passErr,
                        confirmPasswordError = confirmErr,
                    )
                return
            }

            viewModelScope.launch {
                _uiState.value = RegisterUiState.Loading
                _uiState.value =
                    runCatching {
                        createUserUseCase(
                            name = form.name.trim(),
                            email = form.email.trim(),
                            password = form.password,
                        )
                        RegisterUiState.Success
                    }.getOrElse { e ->
                        RegisterUiState.Error(e.message ?: "Error desconocido")
                    }
            }
        }

        fun onErrorConsumed() {
            _uiState.value = RegisterUiState.Idle
        }

        // ---------------------------------------------------------------------------
        // Validaciones
        // ---------------------------------------------------------------------------

        private fun validateName(name: String): String? {
            if (name.isBlank()) return "El nombre no puede estar vacío"
            if (name.trim().length < 2) return "El nombre debe tener al menos 2 caracteres"
            return null
        }

        private fun validateEmail(email: String): String? {
            if (email.isBlank()) return "El correo no puede estar vacío"
            val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (!regex.matches(email.trim())) return "Formato de correo inválido"
            return null
        }

        private fun validatePassword(password: String): String? {
            if (password.isBlank()) return "La contraseña no puede estar vacía"
            if (password.length < 8) return "La contraseña debe tener al menos 8 caracteres"
            return null
        }

        private fun validateConfirmPassword(
            password: String,
            confirm: String,
        ): String? {
            if (confirm.isBlank()) return "Confirmá tu contraseña"
            if (password != confirm) return "Las contraseñas no coinciden"
            return null
        }
    }
