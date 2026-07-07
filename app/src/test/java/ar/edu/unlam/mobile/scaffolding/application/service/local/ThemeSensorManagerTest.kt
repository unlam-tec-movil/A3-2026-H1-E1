package ar.edu.unlam.mobile.scaffolding.application.service.local

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.LightSensorDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ThemeSensorManagerTest {
    private val mockedLightSensor = mockk<LightSensorDataSource>()
    private val sessionManager = mockk<SessionPreferences>(relaxed = true)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sensor should active dark mode when detects lux bellow the limit`() =
        runTest {
            // current threshold is 800
            every { mockedLightSensor.getLuxFlow() } returns flowOf(100f)

            val manager = ThemeSensorManager(mockedLightSensor, sessionManager, this)

            manager.startListening()
            advanceUntilIdle()
            coVerify { sessionManager.setDarkMode(true) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sensor should active dark mode when detects lux above the limit`() =
        runTest {
            // current threshold is 800
            every { mockedLightSensor.getLuxFlow() } returns flowOf(1000f)

            val manager = ThemeSensorManager(mockedLightSensor, sessionManager, this)

            manager.startListening()
            advanceUntilIdle()
            coVerify { sessionManager.setDarkMode(false) }
        }
}
