package ar.edu.unlam.mobile.scaffolding.data.datasources.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXSessionAdapter(
    private val context: Context,
) : CameraSessionPort {
    private var cameraProvider: ProcessCameraProvider? = null
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun start(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        analyzer: ImageAnalysis.Analyzer?,
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            val provider = providerFuture.get()
            cameraProvider = provider

            val preview =
                Preview.Builder().build().also {
                    it.setSurfaceProvider(surfaceProvider)
                }

            val imageAnalysis =
                ImageAnalysis
                    .Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        if (analyzer != null) {
                            it.setAnalyzer(analysisExecutor, analyzer)
                        }
                    }

            provider.unbindAll()

            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis,
            )
        }, ContextCompat.getMainExecutor(context))
    }

    override fun stop() {
        cameraProvider?.unbindAll()
        cameraProvider = null
    }
}
