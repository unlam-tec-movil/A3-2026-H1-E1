package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit.PoseDetectionDataSource
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.usecase.CalculateJointAngleUseCase
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RehabSessionViewModel
    @Inject
    constructor(
        private val cameraSession: CameraSessionPort,
        private val poseDetectionDataSource: PoseDetectionDataSource,
        private val calculateJointAngleUseCase: CalculateJointAngleUseCase,
    ) : ViewModel() {
        private val _currentAngle = MutableStateFlow(0f)
        val currentAngle: StateFlow<Float> = _currentAngle.asStateFlow()

        private val _pose = MutableStateFlow<Pose?>(null)
        val pose: StateFlow<Pose?> = _pose.asStateFlow()

        init {
            viewModelScope.launch {
                poseDetectionDataSource.poseResult.collect { pose ->
                    _pose.value = pose
                    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                    val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
                    val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

                    if (rightShoulder != null && rightElbow != null && rightWrist != null) {
                        val angle =
                            calculateJointAngleUseCase.execute(
                                firstPointX = rightShoulder.position.x,
                                firstPointY = rightShoulder.position.y,
                                midPointX = rightElbow.position.x,
                                midPointY = rightElbow.position.y,
                                lastPointX = rightWrist.position.x,
                                lastPointY = rightWrist.position.y,
                            )
                        _currentAngle.value = angle
                    }
                }
            }
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
