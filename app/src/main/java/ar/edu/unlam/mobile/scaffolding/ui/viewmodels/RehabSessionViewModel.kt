package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RehabSessionViewModel
    @Inject
    constructor(
        private val cameraSession: CameraSessionPort,
    ) : ViewModel() {
        fun startCamera(
            lifecycleOwner: LifecycleOwner,
            surfaceProvider: Preview.SurfaceProvider,
        ) {
            cameraSession.start(lifecycleOwner, surfaceProvider)
        }

        override fun onCleared() {
            cameraSession.stop()
            super.onCleared()
        }
    }
