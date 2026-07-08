package ar.edu.unlam.mobile.scaffolding.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.ui.theme.CyanWave
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.OnboardingViewModel

// Modelo de datos de cada slide
private data class OnboardingSlide(
    val emoji: String,
    val titleRes: Int,
    val descriptionRes: Int,
)

private val slides =
    listOf(
        OnboardingSlide(
            emoji = "🏋️",
            titleRes = R.string.onboarding_slide1_title,
            descriptionRes = R.string.onboarding_slide1_desc,
        ),
        OnboardingSlide(
            emoji = "📡",
            titleRes = R.string.onboarding_slide2_title,
            descriptionRes = R.string.onboarding_slide2_desc,
        ),
        OnboardingSlide(
            emoji = "🔐",
            titleRes = R.string.onboarding_slide3_title,
            descriptionRes = R.string.onboarding_slide3_desc,
        ),
        OnboardingSlide(
            emoji = "💓",
            titleRes = R.string.onboarding_slide4_title,
            descriptionRes = R.string.onboarding_slide4_desc,
        ),
    )

// OnboardingScreen
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionsLauncher =
        rememberLauncherForActivityResult(
            contract = viewModel.getPermissionContract(),
        ) { granted ->
            val allGranted = granted.containsAll(viewModel.getHealthPermissions())
            viewModel.updatePermissionsStatus(allGranted)
        }

    // Consume la señal de navegación
    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            viewModel.onNavigationConsumed()
            onNavigateToLogin()
        }
    }

    // Swipe horizontal para cambiar slide
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(uiState.currentPage) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragAccumulator < -80f -> viewModel.nextPage()
                                dragAccumulator > 80f && uiState.currentPage > 0 ->
                                    viewModel.goToPage(uiState.currentPage - 1)
                            }
                            dragAccumulator = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragAccumulator += dragAmount
                        },
                    )
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Botón "Saltar" — visible en todos los slides
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = viewModel::skipOnboarding) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Contenido animado del slide
            AnimatedContent(
                targetState = uiState.currentPage,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (
                        slideInHorizontally(
                            animationSpec = tween(350),
                            initialOffsetX = { it * direction },
                        ) + fadeIn(tween(350))
                    ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec = tween(350),
                                targetOffsetX = { -it * direction },
                            ) + fadeOut(tween(350))
                        )
                },
                label = "OnboardingSlide",
            ) { page ->
                SlideContent(slide = slides[page])
            }

            Spacer(modifier = Modifier.weight(1f))

            // Dots indicadores
            DotsIndicator(
                totalDots = uiState.totalPages,
                selectedDot = uiState.currentPage,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón principal
            val isLastPage = uiState.currentPage == uiState.totalPages - 1
            val isHealthConnectPage = uiState.currentPage == 3

            if (isHealthConnectPage) {
                HealthConnectActionButtons(
                    status = uiState.healthConnectStatus,
                    hasPermissions = uiState.hasHealthConnectPermissions,
                    onInstall = {
                        val intent =
                            Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                                setPackage("com.android.vending")
                            }
                        context.startActivity(intent)
                    },
                    onRequestPermissions = {
                        permissionsLauncher.launch(viewModel.getHealthPermissions())
                    },
                    onContinue = viewModel::completeOnboarding,
                )
            } else {
                Button(
                    onClick = {
                        if (isLastPage) viewModel.completeOnboarding() else viewModel.nextPage()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text =
                            if (isLastPage) {
                                stringResource(R.string.onboarding_start)
                            } else {
                                stringResource(R.string.onboarding_next)
                            },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HealthConnectActionButtons(
    status: Int,
    hasPermissions: Boolean,
    onInstall: () -> Unit,
    onRequestPermissions: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (status) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Text(
                    text = "Health Connect no está disponible en este dispositivo.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                ) {
                    Text("Continuar sin Health Connect", color = Color.White)
                }
            }

            2 -> { // SDK_AVAILABLE_V2 o similar (dependiendo de la versión de la lib)
                Button(
                    onClick = onInstall,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                ) {
                    Text("Instalar Health Connect", color = Color.White)
                }
            }

            else -> {
                if (!hasPermissions) {
                    Button(
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    ) {
                        Text("Vincular Salud", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    ) {
                        Text("¡Sincronizado! Empezar", color = Color.White)
                    }
                }
            }
        }
    }
}

// Componentes internos
@Composable
private fun SlideContent(slide: OnboardingSlide) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = slide.emoji,
            fontSize = 80.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(slide.titleRes),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(slide.descriptionRes),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun DotsIndicator(
    totalDots: Int,
    selectedDot: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedDot
            Box(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .background(if (isSelected) ElectricIndigo else CyanWave.copy(alpha = 0.35f)),
            )
        }
    }
}
