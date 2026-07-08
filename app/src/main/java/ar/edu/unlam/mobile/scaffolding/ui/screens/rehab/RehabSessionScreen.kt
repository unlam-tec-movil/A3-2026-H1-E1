package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.unlam.mobile.scaffolding.R
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.PoseResult
import ar.edu.unlam.mobile.scaffolding.domain.usecase.JointPrecision
import ar.edu.unlam.mobile.scaffolding.ui.components.SkeletonOverlay
import ar.edu.unlam.mobile.scaffolding.ui.theme.AmberWarning
import ar.edu.unlam.mobile.scaffolding.ui.theme.CoralDanger
import ar.edu.unlam.mobile.scaffolding.ui.theme.DarkBg
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RehabSessionViewModel

@Composable
fun RehabSessionScreen(
    exerciseId: String,
    onNavigateToPostSession: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RehabSessionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val currentAngle by viewModel.currentAngle.collectAsState()
    val poseResult by viewModel.poseResult.collectAsState()
    val precision by viewModel.precision.collectAsState()
    val currentExercise by viewModel.currentExercise.collectAsState()
    val repetitionCount by viewModel.repetitionCount.collectAsState()
    val isSessionFinished by viewModel.isSessionFinished.collectAsState()
    val fallDetected by viewModel.fallDetected.collectAsState()
    val showFatigueAlert by viewModel.showFatigueAlert.collectAsState()
    val fatigueRestTimer by viewModel.fatigueRestTimer.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showNextDialog by remember { mutableStateOf(false) }

    if (isSessionFinished) {
        LaunchedEffect(Unit) {
            onNavigateToPostSession()
        }
    }

    if (showFatigueAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFatigueAlert() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "¿Descansamos?",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { viewModel.dismissFatigueAlert() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Detectamos fatiga. Te recomendamos descansar 60 segundos.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", fatigueRestTimer / 60, fatigueRestTimer % 60),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissFatigueAlert() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Omitir y continuar")
                }
            },
            shape = RoundedCornerShape(16.dp),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(32.dp),
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.rehab_session_exit_title)) },
            text = {
                Text(
                    text = stringResource(R.string.rehab_session_exit_message),
                )
            },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text(stringResource(R.string.rehab_session_exit_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showNextDialog) {
        AlertDialog(
            onDismissRequest = { showNextDialog = false },
            title = { Text(stringResource(R.string.rehab_session_next_title)) },
            text = {
                Text(
                    text = stringResource(R.string.rehab_session_next_message),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNextDialog = false
                        onNavigateToPostSession()
                    },
                ) {
                    Text(stringResource(R.string.rehab_session_next_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNextDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (fallDetected) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFallAlert() },
            title = { Text("¡Movimiento brusco detectado!") },
            text = {
                Text(
                    text = "Hemos pausado la sesión para tu seguridad. ¿Te encuentras bien?",
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissFallAlert() },
                ) {
                    Text("Estoy bien")
                }
            },
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            hasCameraPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    RehabSessionContent(
        hasCameraPermission = hasCameraPermission,
        currentAngle = currentAngle,
        poseResult = poseResult,
        precision = precision,
        exercise = currentExercise,
        reps = repetitionCount,
        onSurfaceReady = { owner, surfaceProvider ->
            viewModel.startCamera(owner, surfaceProvider)
        },
        onBackClick = { showExitDialog = true },
        onNextClick = { showNextDialog = true },
        modifier = modifier,
    )
}

@Composable
fun RehabSessionContent(
    hasCameraPermission: Boolean,
    currentAngle: Float,
    poseResult: PoseResult?,
    precision: JointPrecision,
    exercise: Exercise?,
    reps: Int,
    onSurfaceReady: (androidx.lifecycle.LifecycleOwner, androidx.camera.core.Preview.SurfaceProvider) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreviewComponent(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceReady = onSurfaceReady,
                )
                SkeletonOverlay(
                    poseResult = poseResult,
                    precision = precision,
                    modifier = Modifier.fillMaxSize(),
                )

                // Navigation and Exercise Info Header
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(DarkBg.copy(alpha = 0.75f))
                            .padding(top = 12.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier =
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        shape = CircleShape,
                                    ).size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.rehab_session_back_content_desc),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Text(
                            text = exercise?.name ?: stringResource(R.string.rehab_session_loading),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )

                        IconButton(
                            onClick = onNextClick,
                            modifier =
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        shape = CircleShape,
                                    ).size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.rehab_session_next_confirm),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.rehab_session_reps, reps, exercise?.repetitions ?: 0),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )

                        // Exercise Illustration Square
                        Box(
                            modifier =
                                Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (exercise?.illustrationRes != null) {
                                Image(
                                    painter = painterResource(id = exercise.illustrationRes),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            } else {
                                Text(
                                    text = "💪",
                                    fontSize = 24.sp,
                                )
                            }
                        }
                    }
                }

                // Precision Feedback
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .background(
                                color =
                                    when (precision) {
                                        JointPrecision.IDEAL -> EmeraldIdeal.copy(alpha = 0.85f)
                                        JointPrecision.WARNING -> AmberWarning.copy(alpha = 0.85f)
                                        JointPrecision.ERROR -> CoralDanger.copy(alpha = 0.85f)
                                    },
                                shape = RoundedCornerShape(16.dp),
                            ).padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = "${currentAngle.toInt()}°",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        } else {
            Text(text = stringResource(R.string.rehab_session_camera_permission_required))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RehabSessionScreenPreview() {
    GambAppTheme {
        RehabSessionContent(
            hasCameraPermission = true,
            currentAngle = 45f,
            poseResult = null,
            precision = JointPrecision.IDEAL,
            exercise = null,
            reps = 5,
            onSurfaceReady = { _, _ -> },
            onBackClick = {},
            onNextClick = {},
        )
    }
}
