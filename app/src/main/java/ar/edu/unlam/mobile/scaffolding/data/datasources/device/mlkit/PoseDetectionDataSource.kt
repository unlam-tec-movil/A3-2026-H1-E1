package ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import ar.edu.unlam.mobile.scaffolding.domain.model.PoseResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseDetectionDataSource
    @Inject
    constructor() : ImageAnalysis.Analyzer {
        private val options =
            PoseDetectorOptions
                .Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()

        private val detector = PoseDetection.getClient(options)

        private val _poseResult =
            MutableSharedFlow<PoseResult>(
                replay = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val poseResult: SharedFlow<PoseResult> = _poseResult.asSharedFlow()

        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees,
                    )

                val width = imageProxy.width
                val height = imageProxy.height
                val rotation = imageProxy.imageInfo.rotationDegrees

                detector
                    .process(image)
                    .addOnSuccessListener { pose ->
                        _poseResult.tryEmit(
                            PoseResult(
                                pose = pose,
                                imageWidth = width,
                                imageHeight = height,
                                imageRotation = rotation,
                            ),
                        )
                    }.addOnFailureListener {
                        // Failures can be logged here if needed
                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
