package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.preferences.SessionPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SplashUiState {
    object Loading : SplashUiState

    data class Ready(
        val onboardingCompleted: Boolean,
    ) : SplashUiState
}

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        sessionPreferences: SessionPreferences,
    ) : ViewModel() {
        val uiState: StateFlow<SplashUiState> =
            sessionPreferences.isOnboardingCompleted
                .map { completed -> SplashUiState.Ready(onboardingCompleted = completed) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = SplashUiState.Loading,
                )
    }
