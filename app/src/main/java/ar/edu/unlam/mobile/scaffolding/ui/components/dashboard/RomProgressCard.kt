package ar.edu.unlam.mobile.scaffolding.ui.components.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.unlam.mobile.scaffolding.ui.theme.CyanWave
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo

@Composable
fun RomProgressCard(
    maxRom: Float,
    targetRom: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Rango de Movimiento (ROM)",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                modifier = Modifier.align(Alignment.Start),
            )
            Text(
                text = "Progreso del ROM máximo alcanzado",
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(24.dp))

            RomProgressRing(
                maxRom = maxRom,
                targetRom = targetRom,
                modifier = Modifier.size(200.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElectricIndigo.copy(alpha = 0.08f))
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🎯",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp),
                )
                val missingDegrees = (targetRom - maxRom).coerceAtLeast(0f).toInt()
                val message =
                    if (missingDegrees > 0) {
                        "Estás a sólo $missingDegrees° de alcanzar tu meta óptima de $targetRom°."
                    } else {
                        "¡Excelente! Has alcanzado tu meta óptima de $targetRom°."
                    }
                Text(
                    text = message,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = ElectricIndigo,
                        ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun RomProgressRing(
    maxRom: Float,
    targetRom: Float,
    modifier: Modifier = Modifier,
) {
    val percentage = if (targetRom > 0) (maxRom / targetRom).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "RomProgressAnimation",
    )

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.aspectRatio(1f),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 14.dp.toPx()
            val innerRadius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                color = trackColor,
                radius = innerRadius,
                center = center,
                style = Stroke(width = strokeWidth),
            )

            drawArc(
                brush =
                    Brush.linearGradient(
                        colors = listOf(ElectricIndigo, CyanWave),
                    ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${maxRom.toInt()}°",
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Máximo ROM",
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
            )
            Text(
                text = "Meta: ${targetRom.toInt()}°",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanWave,
                    ),
            )
        }
    }
}
