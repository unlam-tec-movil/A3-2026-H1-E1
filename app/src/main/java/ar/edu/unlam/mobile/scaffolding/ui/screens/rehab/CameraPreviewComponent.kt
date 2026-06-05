package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

@Composable
fun CameraPreviewComponent(
    modifier: Modifier = Modifier,
    onSurfaceReady: (LifecycleOwner, Preview.SurfaceProvider) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
