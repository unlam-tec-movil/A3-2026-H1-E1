package ar.edu.unlam.mobile.scaffolding.domain.ports.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner

interface CameraSessionPort {
    fun start(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        analyzer: ImageAnalysis.Analyzer? = null,
    )

    fun stop()
}
