package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.usecases.user.SignOutUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.user.UpdateUserUseCase
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.preferences.SessionPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UiState
data class ProfileUiState(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val initials: String = "",
    val recentSessions: List<Session> = emptyList(),
    val isDarkMode: Boolean = false,
    val isEditingName: Boolean = false,
    val editNameValue: String = "",
    val editNameError: String? = null,
    val isSavingName: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val navigateToLogin: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

// ViewModel
@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val userRepository: UserRepository,
        private val rehabRepository: RehabRepository,
        private val sessionPreferences: SessionPreferences,
        private val updateUserUseCase: UpdateUserUseCase,
        private val signOutUseCase: SignOutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        init {
            loadProfile()
        }

        private fun loadProfile() {
            viewModelScope.launch {
                combine(
                    userRepository.getUser(),
                    sessionPreferences.isDarkMode,
                ) { user, darkMode ->
                    val userId = user?.id ?: ""
                    Triple(user, darkMode, userId)
                }.collect { (user, darkMode, userId) ->
                    if (userId.isNotEmpty()) {
                        observeSessions(userId)
                    }
                    _uiState.update { current ->
                        current.copy(
                            userId = userId,
                            name = user?.name ?: "",
                            email = user?.email ?: "",
                            initials = buildInitials(user?.name ?: ""),
                            isDarkMode = darkMode,
                            isLoading = false,
                        )
                    }
                }
            }
        }

        private fun observeSessions(userId: String) {
            viewModelScope.launch {
                rehabRepository
                    .getSessions(userId)
                    .collect { sessions ->
                        val recent =
                            sessions
                                .sortedByDescending { it.dateTimestamp }
                                .take(10)
                        _uiState.update { it.copy(recentSessions = recent) }
                    }
            }
        }

        // Edit name
        fun onStartEditName() {
            _uiState.update {
                it.copy(
                    isEditingName = true,
                    editNameValue = it.name,
                    editNameError = null,
                )
            }
        }

        fun onEditNameChange(value: String) {
            _uiState.update {
                it.copy(
                    editNameValue = value,
                    editNameError = if (value.isNotBlank()) validateName(value) else null,
                )
            }
        }

        fun onSaveName() {
            val name = _uiState.value.editNameValue
            val error = validateName(name)
            if (error != null) {
                _uiState.update { it.copy(editNameError = error) }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isSavingName = true) }
                runCatching { updateUserUseCase(name) }
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSavingName = false,
                                isEditingName = false,
                                editNameError = null,
                            )
                        }
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isSavingName = false,
                                editNameError = e.message ?: "Error al guardar",
                            )
                        }
                    }
            }
        }

        fun onCancelEditName() {
            _uiState.update { it.copy(isEditingName = false, editNameError = null) }
        }

        // Dark mode
        fun onToggleDarkMode() {
            viewModelScope.launch {
                val newValue = !_uiState.value.isDarkMode
                sessionPreferences.setDarkMode(newValue)
            }
        }

        // Sign out
        fun onSignOutRequest() {
            _uiState.update { it.copy(showSignOutDialog = true) }
        }

        fun onSignOutDismiss() {
            _uiState.update { it.copy(showSignOutDialog = false) }
        }

        fun onSignOutConfirm() {
            viewModelScope.launch {
                _uiState.update { it.copy(showSignOutDialog = false) }
                runCatching { signOutUseCase() }
                _uiState.update { it.copy(navigateToLogin = true) }
            }
        }

        fun onNavigationConsumed() {
            _uiState.update { it.copy(navigateToLogin = false) }
        }

        // Helpers
        private fun buildInitials(name: String): String {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }

        private fun validateName(name: String): String? {
            if (name.isBlank()) return "El nombre no puede estar vacío"
            if (name.trim().length < 2) return "El nombre debe tener al menos 2 caracteres"
            return null
        }
    }
