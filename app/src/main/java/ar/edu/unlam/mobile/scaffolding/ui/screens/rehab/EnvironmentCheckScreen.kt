package ar.edu.unlam.mobile.scaffolding.ui.screens.rehab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.RoutineListViewModel

@Composable
fun EnvironmentCheckScreen(
    exerciseId: String,
    onNavigateToSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val exercise = uiState.exercises.find { it.id == exerciseId }

    val exerciseName = exercise?.name ?: "Ejercicio"
    val bodyPart = exercise?.bodyPart ?: "el área indicada"

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Preparación de la Sesión",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ejercicio: $exerciseName",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instrucciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CheckItem(text = "Asegúrate de que tu $bodyPart esté visible en la cámara.")
                CheckItem(text = "Ubica el móvil a unos 2 metros de distancia.")
                CheckItem(text = "Busca un lugar con buena iluminación.")
                CheckItem(text = "Mantén el cuerpo entero dentro del encuadre.")
            }
        }

        Button(
            onClick = { onNavigateToSession(exerciseId) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Comenzar Ejercicio", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CheckItem(text: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun EnvironmentCheckPreview() {
    GambAppTheme {
        EnvironmentCheckScreen(exerciseId = "bicep_curl", onNavigateToSession = {})
    }
}
