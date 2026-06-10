package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostSessionViewModel
    @Inject
    constructor(
        private val rehabRepository: RehabRepository,
    ) : ViewModel() {
        private val _lastSession = MutableStateFlow<Session?>(null)
        val lastSession: StateFlow<Session?> = _lastSession.asStateFlow()

        private val _exercise = MutableStateFlow<Exercise?>(null)
        val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

        fun loadLastSession(userId: String = "user_imanol") {
            viewModelScope.launch {
                rehabRepository.getSessions(userId).collectLatest { sessions ->
                    val session = sessions.firstOrNull()
                    _lastSession.value = session
                    session?.let {
                        rehabRepository.getExerciseById(it.exerciseId).collectLatest { exercise ->
                            _exercise.value = exercise
                        }
                    }
                }
            }
        }
    }
