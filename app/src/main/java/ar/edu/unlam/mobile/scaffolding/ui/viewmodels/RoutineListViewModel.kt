package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineListUiState(
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val rehabRepository: RehabRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineListUiState())
    val uiState: StateFlow<RoutineListUiState> = _uiState.asStateFlow()

    init {
        loadRoutineData()
    }

    private fun loadRoutineData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                prepareMockExercisesIfNeeded()
                
                rehabRepository.getExercises()
                    .catch { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Error desconocido") }
                    }
                    .collect { exercisesList ->
                        _uiState.update { 
                            it.copy(
                                exercises = exercisesList,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Error desconocido") }
            }
        }
    }

    private suspend fun prepareMockExercisesIfNeeded() {
        val currentExercises = rehabRepository.getExercises().firstOrNull() ?: emptyList()
        if (currentExercises.isEmpty()) {
            val mockExercises = listOf(
                Exercise(
                    id = "ex_knee_flexion",
                    name = "Flexión de Rodilla",
                    description = "Mantén la espalda recta y dobla lentamente la rodilla hacia atrás intentando alcanzar el ángulo objetivo.",
                    targetAngle = 110f,
                    repetitions = 10,
                    sets = 3
                ),
                Exercise(
                    id = "ex_knee_extension",
                    name = "Extensión de Rodilla",
                    description = "Sentado en una silla firme, extiende la pierna hacia el frente de manera controlada manteniéndola alineada.",
                    targetAngle = 0f,
                    repetitions = 12,
                    sets = 3
                ),
                Exercise(
                    id = "ex_assisted_squats",
                    name = "Sentadillas Asistidas",
                    description = "Apoya las manos en un soporte y desciende lentamente cuidando que las rodillas no pasen la punta de los pies.",
                    targetAngle = 90f,
                    repetitions = 8,
                    sets = 4
                ),
                Exercise(
                    id = "ex_heel_raises",
                    name = "Elevación de Talones",
                    description = "Elévate sobre las puntas de tus pies, sostén un segundo la contracción y desciende suavemente.",
                    targetAngle = 30f,
                    repetitions = 15,
                    sets = 3
                )
            )
            rehabRepository.insertExercises(mockExercises)
        }
    }
}
