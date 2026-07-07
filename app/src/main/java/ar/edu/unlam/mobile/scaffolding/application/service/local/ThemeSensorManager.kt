package ar.edu.unlam.mobile.scaffolding.application.service.local

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.LightSensorDataSource
import ar.edu.unlam.mobile.scaffolding.data.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeSensorManager
    @Inject
    constructor(
        private val lightSensor: LightSensorDataSource,
        private val sessionPreferences: SessionPreferences,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private var job: Job? = null

        fun startListening() {
            if (job != null) {
                return
            }
            job =
                scope.launch {
                    lightSensor.getLuxFlow().collect { lux ->
                        val isDark = lux < 800
                        sessionPreferences.setDarkMode(isDark)
                    }
                }
        }

        fun measureLight() {
            scope.launch {
                lightSensor.getLuxFlow().collect { lux ->

                    val isDark = lux < 800f
                    sessionPreferences.setDarkMode(isDark)
                }
            }
        }

        fun stopListening() {
            job?.cancel()
            job = null
        }
    }
