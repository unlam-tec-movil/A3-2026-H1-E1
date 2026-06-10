package ar.edu.unlam.mobile.scaffolding.domain.model

import com.google.mlkit.vision.pose.Pose

data class PoseResult(
    val pose: Pose,
    val imageWidth: Int,
    val imageHeight: Int,
    val imageRotation: Int,
)
