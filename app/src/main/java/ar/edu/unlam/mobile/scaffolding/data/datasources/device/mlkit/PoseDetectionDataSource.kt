package ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class PoseDetectionDataSource : ImageAnalysis.Analyzer {
    private val options =
        PoseDetectorOptions
            .Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    private val detector = PoseDetection.getClient(options)

    private val _poseResult = MutableSharedFlow<Pose>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val poseResult: SharedFlow<Pose> = _poseResult

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                com.google.mlkit.vision.common.InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees,
                )
            detector
                .process(image)
                .addOnSuccessListener { pose ->
                    _poseResult.tryEmit(pose)
                }.addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
