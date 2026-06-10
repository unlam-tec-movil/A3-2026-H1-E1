package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.PoseResult
import ar.edu.unlam.mobile.scaffolding.domain.usecase.JointPrecision
import ar.edu.unlam.mobile.scaffolding.ui.components.SkeletonOverlay
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

    var showExitDialog by remember { mutableStateOf(false) }
    var showNextDialog by remember { mutableStateOf(false) }

    if (isSessionFinished) {
        LaunchedEffect(Unit) {
            onNavigateToPostSession()
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Deseas salir?") },
            text = {
                Text(
                    text = "Se perderá el progreso de esta sesión si sales ahora.",
                )
            },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (showNextDialog) {
        AlertDialog(
            onDismissRequest = { showNextDialog = false },
            title = { Text("¿Siguiente ejercicio?") },
            text = {
                Text(
                    text = "Podrás continuar con la siguiente actividad de tu rutina.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNextDialog = false
                        onNavigateToPostSession()
                    },
                ) {
                    Text("Siguiente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNextDialog = false }) {
                    Text("Cancelar")
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
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                            )
                        }

                        Text(
                            text = exercise?.name ?: "Cargando...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )

                        IconButton(onClick = onNextClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Siguiente",
                                tint = Color.White,
                            )
                        }
                    }

                    Text(
                        text = "Repeticiones: $reps / ${exercise?.repetitions ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
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
                                        JointPrecision.IDEAL -> Color.Green.copy(alpha = 0.8f)
                                        JointPrecision.WARNING -> Color.Yellow.copy(alpha = 0.8f)
                                        JointPrecision.ERROR -> Color.Red.copy(alpha = 0.8f)
                                    },
                                shape = RoundedCornerShape(16.dp),
                            ).padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = "${currentAngle.toInt()}°",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        } else {
            Text(text = "Se necesita permiso de cámara para iniciar la sesión.")
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
