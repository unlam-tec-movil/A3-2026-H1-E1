package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit.PoseDetectionDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.PoseResult
import ar.edu.unlam.mobile.scaffolding.domain.model.SensorReading
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.domain.usecase.CalculateJointAngleUseCase
import ar.edu.unlam.mobile.scaffolding.domain.usecase.SyncMotorUseCase
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.device.sensor.AccelerometerDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RehabSessionViewModelTest {
    private val cameraSession = mockk<CameraSessionPort>(relaxed = true)
    private val poseDetectionDataSource = mockk<PoseDetectionDataSource>(relaxed = true)
    private val calculateJointAngleUseCase = mockk<CalculateJointAngleUseCase>(relaxed = true)
    private val syncMotorUseCase = mockk<SyncMotorUseCase>(relaxed = true)
    private val rehabRepository = mockk<RehabRepository>(relaxed = true)
    private val accelerometerDataSource = mockk<AccelerometerDataSource>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val poseFlow = MutableSharedFlow<PoseResult>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { poseDetectionDataSource.poseResult } returns poseFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): RehabSessionViewModel =
        RehabSessionViewModel(
            cameraSession = cameraSession,
            poseDetectionDataSource = poseDetectionDataSource,
            calculateJointAngleUseCase = calculateJointAngleUseCase,
            syncMotorUseCase = syncMotorUseCase,
            rehabRepository = rehabRepository,
            accelerometerDataSource = accelerometerDataSource,
        )

    @Test
    fun `initial fallDetected should be false`() {
        every { accelerometerDataSource.getReadingsFlow() } returns flowOf()
        val viewModel = buildViewModel()
        assertFalse(viewModel.fallDetected.value)
    }

    @Test
    fun `fallDetected should become true when accelerometer emits a fall reading`() =
        runTest(testDispatcher) {
            val fallReading = SensorReading(x = 20f, y = 20f, z = 20f, magnitude = 34.64f)
            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(fallReading)
            every { accelerometerDataSource.isFallDetected(fallReading) } returns true

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.fallDetected.value)
        }

    @Test
    fun `dismissFallAlert should reset fallDetected to false`() =
        runTest(testDispatcher) {
            val fallReading = SensorReading(x = 20f, y = 20f, z = 20f, magnitude = 34.64f)
            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(fallReading)
            every { accelerometerDataSource.isFallDetected(fallReading) } returns true

            val viewModel = buildViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.fallDetected.value)

            viewModel.dismissFallAlert()
            assertFalse(viewModel.fallDetected.value)
        }
}
