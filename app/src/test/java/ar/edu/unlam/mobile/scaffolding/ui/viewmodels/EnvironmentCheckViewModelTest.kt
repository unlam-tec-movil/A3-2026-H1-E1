package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import ar.edu.unlam.mobile.scaffolding.data.datasources.sensor.LightSensorDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EnvironmentCheckViewModelTest {
    private val lightSensorDataSource = mockk<LightSensorDataSource>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = EnvironmentCheckViewModel(lightSensorDataSource)

    // Estado inicial
    @Test
    fun `initial currentLux should be null`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()

        val vm = buildViewModel()

        assertNull(vm.uiState.value.currentLux)
    }

    @Test
    fun `initial lightLevel should be POOR`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()

        val vm = buildViewModel()

        assertEquals(LightLevel.POOR, vm.uiState.value.lightLevel)
    }

    @Test
    fun `initial sensorUnavailable should be false`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()

        val vm = buildViewModel()

        assertTrue(!vm.uiState.value.sensorUnavailable)
    }

    // Actualizaciones de lux
    @Test
    fun `uiState should update currentLux when sensor emits value`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns flowOf(250f)

            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals(250f, vm.uiState.value.currentLux)
        }

    @Test
    fun `uiState should classify 350 lux as GOOD`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns flowOf(350f)

            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals(LightLevel.GOOD, vm.uiState.value.lightLevel)
        }

    @Test
    fun `uiState should classify 180 lux as FAIR`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns flowOf(180f)

            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals(LightLevel.FAIR, vm.uiState.value.lightLevel)
        }

    @Test
    fun `uiState should classify 99 lux as POOR`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns flowOf(99f)

            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals(LightLevel.POOR, vm.uiState.value.lightLevel)
        }

    @Test
    fun `uiState should reflect last emission when sensor emits multiple values`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns flowOf(50f, 150f, 400f)

            val vm = buildViewModel()
            advanceUntilIdle()

            assertEquals(400f, vm.uiState.value.currentLux)
            assertEquals(LightLevel.GOOD, vm.uiState.value.lightLevel)
        }

    // Sensor no disponible

    @Test
    fun `sensorUnavailable should become true when flow throws UnsupportedOperationException`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns
                flow { throw UnsupportedOperationException("Sin sensor") }

            val vm = buildViewModel()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.sensorUnavailable)
        }

    @Test
    fun `currentLux should remain null when sensor is unavailable`() =
        runTest(testDispatcher) {
            every { lightSensorDataSource.getLuxFlow() } returns
                flow { throw UnsupportedOperationException("Sin sensor") }

            val vm = buildViewModel()
            advanceUntilIdle()

            assertNull(vm.uiState.value.currentLux)
        }

    // classifyLux

    @Test
    fun `classifyLux should return GOOD for 300 lux`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()
        val vm = buildViewModel()

        assertEquals(LightLevel.GOOD, vm.classifyLux(300f))
    }

    @Test
    fun `classifyLux should return GOOD for values above 300 lux`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()
        val vm = buildViewModel()

        assertEquals(LightLevel.GOOD, vm.classifyLux(1000f))
    }

    @Test
    fun `classifyLux should return FAIR for 100 lux`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()
        val vm = buildViewModel()

        assertEquals(LightLevel.FAIR, vm.classifyLux(100f))
    }

    @Test
    fun `classifyLux should return FAIR for values between 100 and 299 lux`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()
        val vm = buildViewModel()

        assertEquals(LightLevel.FAIR, vm.classifyLux(200f))
    }

    @Test
    fun `classifyLux should return POOR for 99 lux`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()
        val vm = buildViewModel()

        assertEquals(LightLevel.POOR, vm.classifyLux(99f))
    }

    @Test
    fun `classifyLux should return POOR for 0 lux`() {
        every { lightSensorDataSource.getLuxFlow() } returns flowOf()
        val vm = buildViewModel()

        assertEquals(LightLevel.POOR, vm.classifyLux(0f))
    }
}
