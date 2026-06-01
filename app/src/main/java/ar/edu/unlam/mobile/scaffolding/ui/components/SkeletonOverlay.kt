package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

@Composable
fun SkeletonOverlay(
    pose: Pose?,
    colorFeedback: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (pose == null) return@Canvas

        val drawConnection = { firstId: Int, secondId: Int ->
            val firstLandmark = pose.getPoseLandmark(firstId)
            val secondLandmark = pose.getPoseLandmark(secondId)
            if (firstLandmark != null && secondLandmark != null) {
                drawLine(
                    color = colorFeedback,
                    start = Offset(firstLandmark.position.x, firstLandmark.position.y),
                    end = Offset(secondLandmark.position.x, secondLandmark.position.y),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round,
                )
            }
        }

        // Conectar brazo derecho (Hombro -> Codo -> Muñeca)
        drawConnection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        drawConnection(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

        // Conectar brazo izquierdo (Hombro -> Codo -> Muñeca)
        drawConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        drawConnection(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
    }
}
