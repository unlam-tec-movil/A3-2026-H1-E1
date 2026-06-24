package ar.edu.unlam.mobile.scaffolding.application.service.local

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.sensor.MeasurableSensorPort
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.data.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeSensorManager
    @Inject
    constructor(
        private val lightSensor: MeasurableSensorPort,
        private val sessionPreferences: SessionPreferences,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        fun startListening() {
            lightSensor.setOnSensorValueChangedListener { values ->
                val lux = values.firstOrNull() ?: return@setOnSensorValueChangedListener
                val isDark = lux < 40.0

                scope.launch {
                    sessionPreferences.setDarkMode(isDark)
                }
            }
            lightSensor.startListening()
        }

        fun stopListening() {
            lightSensor.stopListening()
        }
    }
