package ar.edu.unlam.mobile.scaffolding.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ar.edu.unlam.mobile.scaffolding.R

private val MARKER_SIZE = 40.dp
private val ICON_SIZE = 24.dp
private val RING_COLOR = Color(0xFFF32133).copy(alpha = 0.5f)

@Composable
fun PulseRingUserLocationMarker(
    iconRes: Int = R.drawable.ward_icon,
    ringColor: Color = RING_COLOR,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 2.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "pulseScale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "pulseAlpha",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(MARKER_SIZE),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                    .drawBehind { drawCircle(color = ringColor) },
        )

        Image(
            painter = painterResource(iconRes),
            contentDescription = "User location",
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .size(ICON_SIZE)
                    .clip(CircleShape),
        )
    }
}
