package ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
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
            MutableSharedFlow<Pose>(
                replay = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val poseResult: SharedFlow<Pose> = _poseResult.asSharedFlow()

        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees,
                    )

                detector
                    .process(image)
                    .addOnSuccessListener { pose ->
                        _poseResult.tryEmit(pose)
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
