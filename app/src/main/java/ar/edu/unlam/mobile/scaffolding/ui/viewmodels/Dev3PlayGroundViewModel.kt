package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Dev3PlayGroundViewModel
    @Inject
    constructor(
        private val observerLocationUseCase: ObserverLocationUseCase,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _locationUiState = MutableStateFlow(LocationUiSate())
        val locationUiState = _locationUiState.asStateFlow()

        fun onLocationPermissionGranted() {
            viewModelScope.launch {
                observerLocationUseCase().collect { location ->
                    _locationUiState.update { currentState ->
                        currentState.copy(location = location)
                    }
                }
            }
        }
    }

data class LocationUiSate(
    val location: Location? = null,
)
