package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit.PoseDetectionDataSource
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.AccelerometerDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.PoseResult
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.usecase.CalculateJointAngleUseCase
import ar.edu.unlam.mobile.scaffolding.domain.usecase.JointPrecision
import ar.edu.unlam.mobile.scaffolding.domain.usecase.SyncMotorUseCase
import com.google.mlkit.vision.pose.PoseLandmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RehabSessionViewModel
    @Inject
    constructor(
        private val cameraSession: CameraSessionPort,
        private val poseDetectionDataSource: PoseDetectionDataSource,
        private val calculateJointAngleUseCase: CalculateJointAngleUseCase,
        private val syncMotorUseCase: SyncMotorUseCase,
        private val rehabRepository: RehabRepository,
        private val accelerometerDataSource: AccelerometerDataSource,
    ) : ViewModel() {
        private val _currentAngle = MutableStateFlow(0f)
        val currentAngle: StateFlow<Float> = _currentAngle.asStateFlow()

        private val _precision = MutableStateFlow(JointPrecision.IDEAL)
        val precision: StateFlow<JointPrecision> = _precision.asStateFlow()

        private val _poseResult = MutableStateFlow<PoseResult?>(null)
        val poseResult: StateFlow<PoseResult?> = _poseResult.asStateFlow()

        private val _fallDetected = MutableStateFlow(false)
        val fallDetected: StateFlow<Boolean> = _fallDetected.asStateFlow()

        private val _currentExercise = MutableStateFlow<Exercise?>(null)
        val currentExercise: StateFlow<Exercise?> = _currentExercise.asStateFlow()

        private val _repetitionCount = MutableStateFlow(0)
        val repetitionCount: StateFlow<Int> = _repetitionCount.asStateFlow()

        private val _isSessionFinished = MutableStateFlow(false)
        val isSessionFinished: StateFlow<Boolean> = _isSessionFinished.asStateFlow()

        private var wasAtTarget = false
        private var sessionStartTime = 0L

        // ROM tracking
        private val repsMaxAngles = mutableListOf<Float>()
        private var currentRepMaxAngle = 0f

        init {
            sessionStartTime = System.currentTimeMillis()
            viewModelScope.launch {
                poseDetectionDataSource.poseResult.collectLatest { result ->
                    if (_isSessionFinished.value) return@collectLatest
                    _poseResult.value = result
                    val exercise = _currentExercise.value ?: return@collectLatest

                    val landmarks =
                        exercise.targetJoints.mapNotNull { name ->
                            val landmarkType = getLandmarkTypeByName(name)
                            result.pose.getPoseLandmark(landmarkType)
                        }

                    if (landmarks.size == 3) {
                        val angle =
                            calculateJointAngleUseCase.execute(
                                firstPointX = landmarks[0].position.x,
                                firstPointY = landmarks[0].position.y,
                                midPointX = landmarks[1].position.x,
                                midPointY = landmarks[1].position.y,
                                lastPointX = landmarks[2].position.x,
                                lastPointY = landmarks[2].position.y,
                            )
                        _currentAngle.value = angle

                        // Track max angle for current rep
                        if (angle > currentRepMaxAngle) {
                            currentRepMaxAngle = angle
                        }

                        _precision.value = syncMotorUseCase.execute(angle, exercise)

                        val (completed, newWasAtTarget) =
                            syncMotorUseCase.checkRepetition(
                                angle,
                                exercise.startAngle,
                                exercise.endAngle,
                                wasAtTarget,
                            )

                        if (completed) {
                            _repetitionCount.value += 1
                            repsMaxAngles.add(currentRepMaxAngle)
                            currentRepMaxAngle = 0f

                            if (_repetitionCount.value >= exercise.repetitions) {
                                finishSession()
                            }
                        }
                        wasAtTarget = newWasAtTarget
                    }
                }
            }

            viewModelScope.launch {
                accelerometerDataSource.getReadingsFlow().collect { reading ->
                    if (accelerometerDataSource.isFallDetected(reading)) {
                        _fallDetected.value = true
                    }
                }
            }
        }

        fun dismissFallAlert() {
            _fallDetected.value = false
        }

        private fun finishSession() {
            if (_isSessionFinished.value) return
            _isSessionFinished.value = true
            val exercise = _currentExercise.value ?: return

            viewModelScope.launch {
                val duration = (System.currentTimeMillis() - sessionStartTime) / 1000
                val averageRom = if (repsMaxAngles.isNotEmpty()) repsMaxAngles.average().toFloat() else 0f

                val session =
                    ar.edu.unlam.mobile.scaffolding.domain.model.Session(
                        userId = "user_imanol",
                        exerciseId = exercise.id,
                        dateTimestamp = System.currentTimeMillis(),
                        durationSeconds = duration,
                        averageRom = averageRom,
                        successfulReps = _repetitionCount.value,
                    )
                // Note: The card mentions saving when "Guardar" is pressed.
                // We'll keep this save here as a "draft" or final, but
                // PostSessionScreen will handle the final interaction.
                rehabRepository.saveSession(session)
            }
        }

        fun loadExercise(exerciseId: String) {
            viewModelScope.launch {
                rehabRepository.getExerciseById(exerciseId).collect { exercise ->
                    _currentExercise.value = exercise
                }
            }
        }

        private fun getLandmarkTypeByName(name: String): Int =
            when (name) {
                "RIGHT_SHOULDER" -> PoseLandmark.RIGHT_SHOULDER
                "RIGHT_ELBOW" -> PoseLandmark.RIGHT_ELBOW
                "RIGHT_WRIST" -> PoseLandmark.RIGHT_WRIST
                "RIGHT_HIP" -> PoseLandmark.RIGHT_HIP
                "RIGHT_KNEE" -> PoseLandmark.RIGHT_KNEE
                "RIGHT_ANKLE" -> PoseLandmark.RIGHT_ANKLE
                "LEFT_SHOULDER" -> PoseLandmark.LEFT_SHOULDER
                "LEFT_ELBOW" -> PoseLandmark.LEFT_ELBOW
                "LEFT_WRIST" -> PoseLandmark.LEFT_WRIST
                "LEFT_HIP" -> PoseLandmark.LEFT_HIP
                "LEFT_KNEE" -> PoseLandmark.LEFT_KNEE
                "LEFT_ANKLE" -> PoseLandmark.LEFT_ANKLE
                else -> PoseLandmark.RIGHT_ELBOW
            }

        fun startCamera(
            lifecycleOwner: LifecycleOwner,
            surfaceProvider: Preview.SurfaceProvider,
        ) {
            cameraSession.start(lifecycleOwner, surfaceProvider, poseDetectionDataSource)
        }

        override fun onCleared() {
            cameraSession.stop()
            super.onCleared()
        }
    }
