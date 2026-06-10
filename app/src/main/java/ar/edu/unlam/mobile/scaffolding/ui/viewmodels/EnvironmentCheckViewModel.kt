package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.LightSensorDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// Semáforo de iluminación
enum class LightLevel {
    // >= 300 lux
    GOOD,

    // 100–299 lux
    FAIR,

    // < 100 lux
    POOR,
}

// UiState
data class EnvironmentCheckUiState(
    val currentLux: Float? = null,
    val lightLevel: LightLevel = LightLevel.POOR,
    val sensorUnavailable: Boolean = false,
)

// ViewModel
@HiltViewModel
class EnvironmentCheckViewModel
    @Inject
    constructor(
        private val lightSensorDataSource: LightSensorDataSource,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(EnvironmentCheckUiState())
        val uiState: StateFlow<EnvironmentCheckUiState> = _uiState.asStateFlow()

        companion object {
            const val THRESHOLD_GOOD = 300f
            const val THRESHOLD_FAIR = 100f
        }

        init {
            viewModelScope.launch {
                lightSensorDataSource
                    .getLuxFlow()
                    .catch { e ->
                        if (e is UnsupportedOperationException) {
                            _uiState.value = _uiState.value.copy(sensorUnavailable = true)
                        }
                    }.collect { lux ->
                        _uiState.value =
                            _uiState.value.copy(
                                currentLux = lux,
                                lightLevel = classifyLux(lux),
                            )
                    }
            }
        }

        fun classifyLux(lux: Float): LightLevel =
            when {
                lux >= THRESHOLD_GOOD -> LightLevel.GOOD
                lux >= THRESHOLD_FAIR -> LightLevel.FAIR
                else -> LightLevel.POOR
            }
    }
