package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.data.datasources.device.health.HealthConnectDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val navigateToLogin: Boolean = false,
    val healthConnectStatus: Int = HealthConnectClient.SDK_UNAVAILABLE,
    val hasHealthConnectPermissions: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val sessionPreferences: SessionPreferences,
        private val healthConnectDataSource: HealthConnectDataSource,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        init {
            checkHealthConnectStatus()
        }

        private fun checkHealthConnectStatus() {
            viewModelScope.launch {
                val status = healthConnectDataSource.getSdkStatus()
                val hasPermissions = healthConnectDataSource.hasAllPermissions()
                _uiState.update {
                    it.copy(
                        healthConnectStatus = status,
                        hasHealthConnectPermissions = hasPermissions,
                    )
                }
            }
        }

        fun updatePermissionsStatus(granted: Boolean) {
            _uiState.update { it.copy(hasHealthConnectPermissions = granted) }
        }

        fun getHealthPermissions() = healthConnectDataSource.permissions

        fun getPermissionContract() = healthConnectDataSource.getPermissionRequestContract()

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
