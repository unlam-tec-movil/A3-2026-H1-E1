package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.PostSessionViewModel

@Composable
fun PostSessionScreen(
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostSessionViewModel = hiltViewModel(),
) {
    val session by viewModel.lastSession.collectAsState()
    val exercise by viewModel.exercise.collectAsState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadLastSession()
        visible = true
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = spring()) + expandVertically(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color.Green,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Sesión Completada!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = exercise?.name ?: "Ejercicio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ResultRow(label = "Repeticiones", value = "${session?.successfulReps ?: 0}")
                    ResultRow(label = "Duración", value = "${session?.durationSeconds ?: 0} seg")
                    ResultRow(label = "ROM Promedio", value = String.format("%.1f°", session?.averageRom ?: 0f))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = spring(dampingRatio = 0.5f)),
        ) {
            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Guardar y Volver")
            }
        }
    }
}

@Composable
fun ResultRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
