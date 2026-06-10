package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.ui.theme.AmberWarning
import ar.edu.unlam.mobile.scaffolding.ui.theme.CoralDanger
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.EnvironmentCheckUiState
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.EnvironmentCheckViewModel
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.LightLevel

// EnvironmentCheckScreen
@Composable
fun EnvironmentCheckScreen(
    onNavigateToRehab: () -> Unit,
    viewModel: EnvironmentCheckViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EnvironmentCheckContent(
        uiState = uiState,
        onContinue = onNavigateToRehab,
        onContinueAnyway = onNavigateToRehab,
    )
}

// EnvironmentCheckContent
@Composable
internal fun EnvironmentCheckContent(
    uiState: EnvironmentCheckUiState,
    onContinue: () -> Unit,
    onContinueAnyway: () -> Unit,
) {
    val isOptimal = uiState.lightLevel != LightLevel.POOR
    val lux = uiState.currentLux

    // Escala del sol animada: 0.6 sin datos → escala según lux hasta 1.4
    val sunScale by animateFloatAsState(
        targetValue =
            when {
                uiState.sensorUnavailable -> 1.0f
                lux == null -> 0.6f
                else -> (0.6f + (lux / 600f).coerceIn(0f, 0.8f))
            },
        animationSpec = tween(durationMillis = 600),
        label = "SunScaleAnimation",
    )

    val semaphoreColor =
        when (uiState.lightLevel) {
            LightLevel.GOOD -> EmeraldIdeal
            LightLevel.FAIR -> AmberWarning
            LightLevel.POOR -> CoralDanger
        }

    val semaphoreLabel =
        when (uiState.lightLevel) {
            LightLevel.GOOD -> "Iluminación óptima"
            LightLevel.FAIR -> "Iluminación aceptable"
            LightLevel.POOR -> "Iluminación insuficiente"
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Título
            Text(
                text = "Verificación del entorno",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Text(
                text =
                    "Comprobamos las condiciones de iluminación para que la " +
                        "cámara pueda detectar tu postura correctamente.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            // Ícono de sol animado con escala proporcional a los lux
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .scale(sunScale)
                        .clip(CircleShape)
                        .background(semaphoreColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "☀️",
                    fontSize = 56.sp,
                )
            }

            // Mensaje de sensor no disponible
            if (uiState.sensorUnavailable) {
                Text(
                    text = "Sensor no disponible",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            // Semáforo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = semaphoreColor.copy(alpha = 0.08f),
                    ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(semaphoreColor),
                    )
                    Column {
                        Text(
                            text = semaphoreLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = semaphoreColor,
                        )
                        Text(
                            text =
                                when (uiState.lightLevel) {
                                    LightLevel.GOOD -> "Las condiciones son ideales para la sesión"
                                    LightLevel.FAIR -> "La sesión puede continuar"
                                    LightLevel.POOR -> "Mejorá la iluminación para resultados precisos"
                                },
                            fontSize = 12.sp,
                            color = semaphoreColor.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Advertencia bloqueante si lux < 100
            if (uiState.lightLevel == LightLevel.POOR && lux != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = CoralDanger.copy(alpha = 0.08f),
                        ),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Text(
                        text =
                            "⚠️  La detección de postura puede ser imprecisa con poca luz. " +
                                "Acercate a una ventana o encendé una lámpara.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = CoralDanger,
                        textAlign = TextAlign.Start,
                        lineHeight = 19.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botones
            if (isOptimal || uiState.sensorUnavailable) {
                // Condición aceptable (FAIR, GOOD o sensor no disponible) → botón principal habilitado
                Button(
                    onClick = onContinue,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            } else {
                // Lux < 100 → botón principal deshabilitado + opción de continuar igual
                Button(
                    onClick = {},
                    enabled = false,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            disabledContainerColor = ElectricIndigo.copy(alpha = 0.35f),
                        ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }

                OutlinedButton(
                    onClick = onContinueAnyway,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = CoralDanger,
                        ),
                ) {
                    Text(
                        text = "Continuar igual",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

// Previews
@Preview(name = "GOOD · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewGoodLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            EnvironmentCheckContent(
                uiState = EnvironmentCheckUiState(currentLux = 450f, lightLevel = LightLevel.GOOD),
                onContinue = {},
                onContinueAnyway = {},
            )
        }
    }
}

@Preview(name = "FAIR · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewFairLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            EnvironmentCheckContent(
                uiState = EnvironmentCheckUiState(currentLux = 180f, lightLevel = LightLevel.FAIR),
                onContinue = {},
                onContinueAnyway = {},
            )
        }
    }
}

@Preview(name = "POOR · Light — advertencia", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewPoorLight() {
    GambAppTheme(darkTheme = false) {
        Surface {
            EnvironmentCheckContent(
                uiState = EnvironmentCheckUiState(currentLux = 40f, lightLevel = LightLevel.POOR),
                onContinue = {},
                onContinueAnyway = {},
            )
        }
    }
}

@Preview(name = "Sensor no disponible · Light", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSensorUnavailable() {
    GambAppTheme(darkTheme = false) {
        Surface {
            EnvironmentCheckContent(
                uiState = EnvironmentCheckUiState(sensorUnavailable = true),
                onContinue = {},
                onContinueAnyway = {},
            )
        }
    }
}

@Preview(name = "Midiendo · GOOD · Dark", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewGoodDark() {
    GambAppTheme(darkTheme = true) {
        Surface {
            EnvironmentCheckContent(
                uiState = EnvironmentCheckUiState(currentLux = 320f, lightLevel = LightLevel.GOOD),
                onContinue = {},
                onContinueAnyway = {},
            )
        }
    }
}
