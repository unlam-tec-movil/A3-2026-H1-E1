package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.SplashUiState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.SplashViewModel

// Pantalla de Splash lógica
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.Loading -> Unit // espera
            is SplashUiState.Ready -> {
                if (state.onboardingCompleted) {
                    onNavigateToLogin()
                } else {
                    onNavigateToOnboarding()
                }
            }
        }
    }
}
