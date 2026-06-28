package ar.edu.unlam.mobile.scaffolding.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterService
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.ui.components.dashboard.AchievementsCard
import ar.edu.unlam.mobile.scaffolding.ui.components.dashboard.ActiveRoutineBanner
import ar.edu.unlam.mobile.scaffolding.ui.components.dashboard.LastSessionCard
import ar.edu.unlam.mobile.scaffolding.ui.components.dashboard.RomProgressCard
import ar.edu.unlam.mobile.scaffolding.ui.components.dashboard.StepsCard
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.DashboardViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

const val DASHBOARD_SCREEN_ROUTE = "dashboard"

@Composable
fun DashboardScreen(
    onNavigateToRoutineList: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val activityRecognitionGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
                } else {
                    true
                }

            if (activityRecognitionGranted) {
                val serviceIntent = Intent(context, StepCounterService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }

    LaunchedEffect(Unit) {
        val needsActivityRecognition =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) !=
                PackageManager.PERMISSION_GRANTED

        val needsPostNotifications =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        if (needsActivityRecognition || needsPostNotifications) {
            val list = mutableListOf<String>()
            if (needsActivityRecognition) list.add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (needsPostNotifications) list.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionLauncher.launch(list.toTypedArray())
        } else {
            val serviceIntent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    val entranceProgress by animateFloatAsState(
        targetValue = if (uiState.isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "DashboardEntranceProgress",
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        } else if (uiState.error != null) {
            Text(
                text = "Error: ${uiState.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            val density = LocalDensity.current
            val yOffsetPx = remember { with(density) { 32.dp.toPx() } }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding() + 16.dp))

                // Header (Greeting & Avatar)
                DashboardHeader(
                    userName = uiState.userName,
                    modifier =
                        Modifier.graphicsLayer {
                            val progress = (entranceProgress / 0.6f).coerceIn(0f, 1f)
                            val easedProgress = FastOutSlowInEasing.transform(progress)
                            alpha = easedProgress
                            translationY = -yOffsetPx * (1f - easedProgress)
                        },
                )

                // Active Routine Banner CTA
                ActiveRoutineBanner(
                    onNavigateToRoutineList = onNavigateToRoutineList,
                    modifier =
                        Modifier.graphicsLayer {
                            val progress = ((entranceProgress - 0.08f) / 0.6f).coerceIn(0f, 1f)
                            val easedProgress = FastOutSlowInEasing.transform(progress)
                            alpha = easedProgress
                            translationY = yOffsetPx * (1f - easedProgress)
                        },
                )

                // Card 1: ROM Progress Ring Card
                RomProgressCard(
                    maxRom = uiState.maxRom,
                    targetRom = uiState.targetRom,
                    onClick = onNavigateToProgress,
                    modifier =
                        Modifier.graphicsLayer {
                            val progress = ((entranceProgress - 0.15f) / 0.6f).coerceIn(0f, 1f)
                            val easedProgress = FastOutSlowInEasing.transform(progress)
                            alpha = easedProgress
                            translationY = yOffsetPx * (1f - easedProgress)
                        },
                )

                // Card 2: Steps Summary Card
                StepsCard(
                    currentSteps = uiState.currentSteps,
                    targetSteps = uiState.targetSteps,
                    modifier =
                        Modifier.graphicsLayer {
                            val progress = ((entranceProgress - 0.3f) / 0.6f).coerceIn(0f, 1f)
                            val easedProgress = FastOutSlowInEasing.transform(progress)
                            alpha = easedProgress
                            translationY = yOffsetPx * (1f - easedProgress)
                        },
                )

                // Card 3: Achievements Card
                AchievementsCard(
                    unlockedCount = uiState.unlockedAchievementsCount,
                    totalCount = 3,
                    onClick = onNavigateToAchievements,
                    modifier =
                        Modifier.graphicsLayer {
                            val progress = ((entranceProgress - 0.38f) / 0.6f).coerceIn(0f, 1f)
                            val easedProgress = FastOutSlowInEasing.transform(progress)
                            alpha = easedProgress
                            translationY = yOffsetPx * (1f - easedProgress)
                        },
                )

                // Card 4: Last Active Session Card
                LastSessionCard(
                    lastSession = uiState.lastSession,
                    onClick = onNavigateToProgress,
                    modifier =
                        Modifier.graphicsLayer {
                            val progress = ((entranceProgress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                            val easedProgress = FastOutSlowInEasing.transform(progress)
                            alpha = easedProgress
                            translationY = yOffsetPx * (1f - easedProgress)
                        },
                )

                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 16.dp))
            }
        }

        // Celebratory Dialog for Newly Unlocked Achievement
        uiState.newlyUnlockedAchievement?.let { achievement ->
            AchievementUnlockedDialog(
                achievement = achievement,
                onDismiss = { viewModel.dismissUnlockPopup() },
            )
        }
    }
}

@Composable
fun DashboardHeader(
    userName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "¡Hola, $userName! 👋",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            )
            Text(
                text = "Tu resumen de salud para hoy",
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    ),
            )
        }
    }
}

@Composable
fun AchievementUnlockedDialog(
    achievement: Achievement,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
            ) {
                Text("¡Genial!", color = Color.White)
            }
        },
        title = {
            Text(
                text = "¡Logro Desbloqueado!",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            var animateScale by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                animateScale = true
            }
            val scale by animateFloatAsState(
                targetValue = if (animateScale) 1.2f else 0f,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                label = "PopupMedalScale",
            )

            val emoji =
                when (achievement.id) {
                    "10k_steps" -> "🏃‍♂️"
                    "first_session" -> "🏋️"
                    "master_rom" -> "🔥"
                    else -> "🏆"
                }

            val badgeColor =
                when (achievement.id) {
                    "10k_steps" -> Color(0xFFF59E0B)
                    "first_session" -> Color(0xFF059669)
                    "master_rom" -> Color(0xFF7C3AED)
                    else -> ElectricIndigo
                }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }.clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(3.dp, badgeColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val achievementComposition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.achievement_unlocked),
                    )
                    val achievementProgress by animateLottieCompositionAsState(
                        composition = achievementComposition,
                        iterations = 1,
                        isPlaying = true,
                        speed = 1f,
                    )
                    LottieAnimation(
                        composition = achievementComposition,
                        progress = { achievementProgress },
                        modifier = Modifier.size(80.dp),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
