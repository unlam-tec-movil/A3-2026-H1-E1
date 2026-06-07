package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.domain.usecase.JointPrecision
import ar.edu.unlam.mobile.scaffolding.ui.components.SkeletonOverlay
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RehabSessionViewModel
import com.google.mlkit.vision.pose.Pose

@Composable
fun RehabSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: RehabSessionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val currentAngle by viewModel.currentAngle.collectAsState()
    val pose by viewModel.pose.collectAsState()
    val precision by viewModel.precision.collectAsState()
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

    RehabSessionContent(
        hasCameraPermission = hasCameraPermission,
        currentAngle = currentAngle,
        pose = pose,
        precision = precision,
        onSurfaceReady = { owner, surfaceProvider ->
            viewModel.startCamera(owner, surfaceProvider)
        },
        modifier = modifier,
    )
}

@Composable
fun RehabSessionContent(
    hasCameraPermission: Boolean,
    currentAngle: Float,
    pose: Pose?,
    precision: JointPrecision,
    onSurfaceReady: (androidx.lifecycle.LifecycleOwner, androidx.camera.core.Preview.SurfaceProvider) -> Unit,
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
                    pose = pose,
                    precision = precision,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                text = "Ángulo: ${currentAngle.toInt()}°",
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp),
                style = MaterialTheme.typography.headlineLarge,
                color =
                    when (precision) {
                        JointPrecision.IDEAL -> Color.Green
                        JointPrecision.WARNING -> Color.Yellow
                        JointPrecision.ERROR -> Color.Red
                    },
            )
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
            pose = null,
            precision = JointPrecision.IDEAL,
            onSurfaceReady = { _, _ -> },
        )
    }
}
