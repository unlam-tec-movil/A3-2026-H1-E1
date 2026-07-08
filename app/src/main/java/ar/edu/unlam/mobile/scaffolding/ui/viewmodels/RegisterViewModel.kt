package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.application.usecases.user.CreateUserUseCase
import ar.edu.unlam.mobile.scaffolding.ui.utils.UiText
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
        val message: UiText,
    ) : RegisterUiState
}

// Form state
data class RegisterFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null,
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
                        RegisterUiState.Error(
                            e.message?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.unknown_error),
                        )
                    }
            }
        }

        fun onErrorConsumed() {
            _uiState.value = RegisterUiState.Idle
        }

        // ---------------------------------------------------------------------------
        // Validaciones
        // ---------------------------------------------------------------------------

        private fun validateName(name: String): UiText? {
            if (name.isBlank()) return UiText.StringResource(R.string.register_error_empty_name)
            if (name.trim().length < 2) return UiText.StringResource(R.string.register_error_short_name)
            return null
        }

        private fun validateEmail(email: String): UiText? {
            if (email.isBlank()) return UiText.StringResource(R.string.register_error_empty_email)
            val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (!regex.matches(email.trim())) return UiText.StringResource(R.string.register_error_invalid_email)
            return null
        }

        private fun validatePassword(password: String): UiText? {
            if (password.isBlank()) return UiText.StringResource(R.string.register_error_empty_password)
            if (password.length < 8) return UiText.StringResource(R.string.register_error_short_password)
            return null
        }

        private fun validateConfirmPassword(
            password: String,
            confirm: String,
        ): UiText? {
            if (confirm.isBlank()) return UiText.StringResource(R.string.register_error_confirm_password)
            if (password != confirm) return UiText.StringResource(R.string.register_error_passwords_mismatch)
            return null
        }
    }
