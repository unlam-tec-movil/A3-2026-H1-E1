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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.StepCounterService
import ar.edu.unlam.mobile.scaffolding.domain.model.Achievement
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.ui.theme.AmberWarning
import ar.edu.unlam.mobile.scaffolding.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import ar.edu.unlam.mobile.scaffolding.ui.theme.CyanWave
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun RomProgressCard(
    maxRom: Float,
    targetRom: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Rango de Movimiento (ROM)",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                modifier = Modifier.align(Alignment.Start),
            )
            Text(
                text = "Progreso del ROM máximo alcanzado",
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom ROM Progress Ring component
            RomProgressRing(
                maxRom = maxRom,
                targetRom = targetRom,
                modifier = Modifier.size(200.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Highlight message/insight
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElectricIndigo.copy(alpha = 0.08f))
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🎯",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp),
                )
                val missingDegrees = (targetRom - maxRom).coerceAtLeast(0f).toInt()
                val message =
                    if (missingDegrees > 0) {
                        "Estás a sólo $missingDegrees° de alcanzar tu meta óptima de $targetRom°."
                    } else {
                        "¡Excelente! Has alcanzado tu meta óptima de $targetRom°."
                    }
                Text(
                    text = message,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = ElectricIndigo,
                        ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun RomProgressRing(
    maxRom: Float,
    targetRom: Float,
    modifier: Modifier = Modifier,
) {
    // Percentage from 0.0 to 1.0
    val percentage = if (targetRom > 0) (maxRom / targetRom).coerceIn(0f, 1f) else 0f

    // Animate the progress sweep
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "RomProgressAnimation",
    )

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.aspectRatio(1f),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 14.dp.toPx()
            val innerRadius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Draw background track
            drawCircle(
                color = trackColor,
                radius = innerRadius,
                center = center,
                style = Stroke(width = strokeWidth),
            )

            // Draw progress arc (start from -90 degrees, i.e., 12 o'clock, sweep clockwise)
            drawArc(
                brush =
                    Brush.linearGradient(
                        colors = listOf(ElectricIndigo, CyanWave),
                    ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        // Central text inside the ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${maxRom.toInt()}°",
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Máximo ROM",
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
            )
            Text(
                text = "Meta: ${targetRom.toInt()}°",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanWave,
                    ),
            )
        }
    }
}

@Composable
fun StepsCard(
    currentSteps: Int,
    targetSteps: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Pasos Diarios",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                    Text(
                        text = "Actividad física registrada hoy",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            ),
                    )
                }

                // Decorative step icon container
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldIdeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🚶",
                        fontSize = 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Steps text info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%,d", currentSteps),
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                )
                Text(
                    text = String.format(Locale.getDefault(), "Meta: %,d", targetSteps),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Steps Progress Bar
            val stepsProgress = if (targetSteps > 0) currentSteps.toFloat() / targetSteps else 0f
            LinearProgressIndicator(
                progress = { stepsProgress.coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                color = EmeraldIdeal,
                trackColor = Color.LightGray.copy(alpha = 0.25f),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Steps stats details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatInfoItem(emoji = "🔥", label = "Calorías", value = "324 kcal")
                StatInfoItem(emoji = "🚶", label = "Distancia", value = "5.2 km")
                StatInfoItem(emoji = "⏱️", label = "Tiempo activo", value = "45 min")
            }
        }
    }
}

@Composable
fun StatInfoItem(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 6.dp),
        )
        Column {
            Text(
                text = value,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
            )
            Text(
                text = label,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
            )
        }
    }
}

@Composable
fun LastSessionCard(
    lastSession: Session?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Última Sesión Activa",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                    Text(
                        text = "Resultados de tu último entrenamiento",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            ),
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberWarning.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🏋️",
                        fontSize = 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (lastSession != null) {
                // Formatting Date
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val sessionDate = dateFormat.format(Date(lastSession.dateTimestamp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Fecha del entrenamiento",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            ),
                    )
                    Text(
                        text = sessionDate,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                }

                HorizontalDivider()

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Duración",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                ),
                        )
                        val durationMin = lastSession.durationSeconds / 60
                        val durationSec = lastSession.durationSeconds % 60
                        Text(
                            text = if (durationSec > 0) "${durationMin}m ${durationSec}s" else "$durationMin min",
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                        )
                    }

                    Column {
                        Text(
                            text = "ROM Promedio",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                ),
                        )
                        Text(
                            text = "${lastSession.averageRom.toInt()}°",
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricIndigo,
                                ),
                        )
                    }

                    Column {
                        Text(
                            text = "Rep. Exitosas",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                ),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldIdeal,
                                modifier =
                                    Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp),
                            )
                            Text(
                                text = "${lastSession.successfulReps}",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No hay registros de sesiones de entrenamiento aún.",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.LightGray.copy(alpha = 0.3f),
    thickness: Dp = 1.dp,
) {
    Spacer(
        modifier =
            modifier
                .fillMaxWidth()
                .height(thickness)
                .background(color),
    )
}

@Composable
fun ActiveRoutineBanner(
    onNavigateToRoutineList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onNavigateToRoutineList() },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors = listOf(ElectricIndigo, CyanWave),
                            ),
                    ).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tu Rutina de Hoy 🏋️",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ver los ejercicios y objetivos asignados para hoy",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                        ),
                )
            }

            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Ver Rutina",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
fun AchievementsCard(
    unlockedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Tus Logros y Medallas 🏆",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )
                    Text(
                        text = "Gamificación y progreso del tratamiento",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "$unlockedCount / $totalCount",
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricIndigo,
                            ),
                    )
                    Text(
                        text = "Desbloqueados",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            ),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiniMedalBadge(emoji = "🏃‍♂️", isUnlocked = unlockedCount >= 1, color = Color(0xFFF59E0B))
                    MiniMedalBadge(emoji = "🏋️", isUnlocked = unlockedCount >= 2, color = Color(0xFF059669))
                    MiniMedalBadge(emoji = "🔥", isUnlocked = unlockedCount >= 3, color = Color(0xFF7C3AED))
                }
            }
        }
    }
}

@Composable
fun MiniMedalBadge(
    emoji: String,
    isUnlocked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) color.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                ).border(
                    width = 1.5.dp,
                    color = if (isUnlocked) color else Color.LightGray.copy(alpha = 0.4f),
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp,
            modifier =
                Modifier.graphicsLayer {
                    if (!isUnlocked) alpha = 0.4f
                },
        )
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
