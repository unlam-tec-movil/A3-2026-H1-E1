package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
    val navigateToLogin: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val sessionPreferences: SessionPreferences,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        fun nextPage() {
            val current = _uiState.value.currentPage
            if (current < _uiState.value.totalPages - 1) {
                _uiState.value = _uiState.value.copy(currentPage = current + 1)
            }
        }

        fun goToPage(page: Int) {
            _uiState.value = _uiState.value.copy(currentPage = page)
        }

        // Último slide guarda flag y navega a Login.
        fun completeOnboarding() {
            viewModelScope.launch {
                sessionPreferences.setOnboardingCompleted(true)
                _uiState.value = _uiState.value.copy(navigateToLogin = true)
            }
        }

        // Botón "Saltar" navega directo a Login sin guardar el flag.
        fun skipOnboarding() {
            _uiState.value = _uiState.value.copy(navigateToLogin = true)
        }

        fun onNavigationConsumed() {
            _uiState.value = _uiState.value.copy(navigateToLogin = false)
        }
    }
