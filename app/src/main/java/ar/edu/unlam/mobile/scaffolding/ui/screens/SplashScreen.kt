package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.SplashUiState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.SplashViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.Loading -> Unit
            is SplashUiState.Ready -> {
                if (state.onboardingCompleted) {
                    onNavigateToLogin()
                } else {
                    onNavigateToOnboarding()
                }
            }
        }
    }

    // Show animated logo while the state is still Loading
    if (uiState is SplashUiState.Loading) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.splash_logo),
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = true,
            speed = 1f,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(ElectricIndigo),
            contentAlignment = Alignment.Center,
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp),
            )
        }
    }
}
