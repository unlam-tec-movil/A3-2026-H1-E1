package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import ar.edu.unlam.mobile.scaffolding.domain.model.PoseResult
import ar.edu.unlam.mobile.scaffolding.domain.usecase.JointPrecision
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.max

@Composable
fun SkeletonOverlay(
    poseResult: PoseResult?,
    precision: JointPrecision,
    modifier: Modifier = Modifier,
) {
    if (poseResult == null) return

    val pose = poseResult.pose
    val targetColor =
        when (precision) {
            JointPrecision.IDEAL -> Color.Green
            JointPrecision.WARNING -> Color.Yellow
            JointPrecision.ERROR -> Color.Red
        }

    val colorFeedback by animateColorAsState(targetValue = targetColor, label = "colorFeedback")

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Ajustamos las dimensiones de la imagen según la rotación
        val isRotated = poseResult.imageRotation == 90 || poseResult.imageRotation == 270
        // ML Kit con InputImage.fromMediaImage(mediaImage, rotation) entrega coordenadas
        // ya rotadas al espacio "upright" (derecho).
        val logicalWidth = if (isRotated) poseResult.imageHeight else poseResult.imageWidth
        val logicalHeight = if (isRotated) poseResult.imageWidth else poseResult.imageHeight

        val scale = max(canvasWidth / logicalWidth.toFloat(), canvasHeight / logicalHeight.toFloat())
        val offsetX = (canvasWidth - logicalWidth * scale) / 2f
        val offsetY = (canvasHeight - logicalHeight * scale) / 2f

        fun getCanvasOffset(landmark: PoseLandmark?): Offset? {
            if (landmark == null) return null

            // Para cámara frontal espejada, invertimos el eje X en el espacio lógico (ya rotado)
            val rawX = landmark.position.x
            val rawY = landmark.position.y

            val mirroredX = logicalWidth - rawX

            return Offset(
                x = mirroredX * scale + offsetX,
                y = rawY * scale + offsetY,
            )
        }

        // Definimos las conexiones incluyendo la cabeza
        val connections =
            listOf(
                // Cabeza
                PoseLandmark.LEFT_EYE to PoseLandmark.RIGHT_EYE,
                PoseLandmark.LEFT_EYE to PoseLandmark.LEFT_EAR,
                PoseLandmark.RIGHT_EYE to PoseLandmark.RIGHT_EAR,
                PoseLandmark.NOSE to PoseLandmark.LEFT_EYE,
                PoseLandmark.NOSE to PoseLandmark.RIGHT_EYE,
                // Cuerpo superior
                PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
                PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
                PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
                PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
                // Tronco
                PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
                PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
                PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
                // Piernas
                PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
                PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
                PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
                PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE,
            )

        connections.forEach { (start, end) ->
            val startOff = getCanvasOffset(pose.getPoseLandmark(start))
            val endOff = getCanvasOffset(pose.getPoseLandmark(end))
            if (startOff != null && endOff != null) {
                drawLine(
                    color = colorFeedback.copy(alpha = 0.7f), // Color más mate/transparente
                    start = startOff,
                    end = endOff,
                    strokeWidth = 20f, // Líneas aún más gruesas
                    cap = StrokeCap.Round,
                )
            }
        }

        pose.allPoseLandmarks.forEach { landmark ->
            getCanvasOffset(landmark)?.let {
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 8f,
                    center = it,
                )
            }
        }
    }
}
