package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraPreviewComponent(
    modifier: Modifier = Modifier,
    onSurfaceReady: (LifecycleOwner, Preview.SurfaceProvider) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView =
        remember {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

    LaunchedEffect(previewView) {
        onSurfaceReady(lifecycleOwner, previewView.surfaceProvider)
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}

@ComposePreview(showBackground = true)
@Composable
fun CameraPreviewComponentPreview() {
    GambAppTheme {
        CameraPreviewComponent(
            onSurfaceReady = { _, _ -> },
        )
    }
}
