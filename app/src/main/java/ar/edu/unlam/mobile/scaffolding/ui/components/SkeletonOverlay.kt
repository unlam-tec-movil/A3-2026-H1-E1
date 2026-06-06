package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

@Composable
fun SkeletonOverlay(
    pose: Pose?,
    colorFeedback: Color,
    modifier: Modifier = Modifier,
) {
    if (pose == null) return

    // Landmarks brazo derecho
    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

    // Landmarks brazo izquierdo
    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

    // Animación de puntos para fluidez (Derecha)
    val rsX by animateFloatAsState(targetValue = rightShoulder?.position?.x ?: 0f, label = "rsX")
    val rsY by animateFloatAsState(targetValue = rightShoulder?.position?.y ?: 0f, label = "rsY")
    val reX by animateFloatAsState(targetValue = rightElbow?.position?.x ?: 0f, label = "reX")
    val reY by animateFloatAsState(targetValue = rightElbow?.position?.y ?: 0f, label = "reY")
    val rwX by animateFloatAsState(targetValue = rightWrist?.position?.x ?: 0f, label = "rwX")
    val rwY by animateFloatAsState(targetValue = rightWrist?.position?.y ?: 0f, label = "rwY")

    // Animación de puntos para fluidez (Izquierda)
    val lsX by animateFloatAsState(targetValue = leftShoulder?.position?.x ?: 0f, label = "lsX")
    val lsY by animateFloatAsState(targetValue = leftShoulder?.position?.y ?: 0f, label = "lsY")
    val leX by animateFloatAsState(targetValue = leftElbow?.position?.x ?: 0f, label = "leX")
    val leY by animateFloatAsState(targetValue = leftElbow?.position?.y ?: 0f, label = "leY")
    val lwX by animateFloatAsState(targetValue = leftWrist?.position?.x ?: 0f, label = "lwX")
    val lwY by animateFloatAsState(targetValue = leftWrist?.position?.y ?: 0f, label = "lwY")

    Canvas(modifier = modifier.fillMaxSize()) {
        // Dibujar Brazo Derecho
        if (rightShoulder != null && rightElbow != null) {
            drawLine(
                color = colorFeedback,
                start = Offset(rsX, rsY),
                end = Offset(reX, reY),
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
        }
        if (rightElbow != null && rightWrist != null) {
            drawLine(
                color = colorFeedback,
                start = Offset(reX, reY),
                end = Offset(rwX, rwY),
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
        }

        // Dibujar Brazo Izquierdo
        if (leftShoulder != null && leftElbow != null) {
            drawLine(
                color = colorFeedback,
                start = Offset(lsX, lsY),
                end = Offset(leX, leY),
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
        }
        if (leftElbow != null && leftWrist != null) {
            drawLine(
                color = colorFeedback,
                start = Offset(leX, leY),
                end = Offset(lwX, lwY),
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkeletonOverlayPreview() {
    GambAppTheme {
        SkeletonOverlay(
            pose = null,
            colorFeedback = Color.Green,
        )
    }
}
