package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import ar.edu.unlam.mobile.scaffolding.data.datasources.device.mlkit.PoseDetectionDataSource
import ar.edu.unlam.mobile.scaffolding.domain.model.SensorReading
import ar.edu.unlam.mobile.scaffolding.domain.ports.camera.CameraSessionPort
import ar.edu.unlam.mobile.scaffolding.domain.usecase.CalculateJointAngleUseCase
import ar.edu.unlam.mobile.scaffolding.domain.usecase.SyncMotorUseCase
import ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.device.sensor.AccelerometerDataSource
import com.google.mlkit.vision.pose.Pose
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
    private val accelerometerDataSource = mockk<AccelerometerDataSource>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val poseFlow = MutableSharedFlow<Pose>()

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
            accelerometerDataSource = accelerometerDataSource,
        )

    // Estado inicial
    @Test
    fun `initial fallDetected should be false`() {
        every { accelerometerDataSource.getReadingsFlow() } returns flowOf()

        val viewModel = buildViewModel()

        assertFalse(viewModel.fallDetected.value)
    }

    @Test
    fun `initial targetAngle should be null`() {
        every { accelerometerDataSource.getReadingsFlow() } returns flowOf()

        val viewModel = buildViewModel()

        org.junit.Assert.assertNull(viewModel.targetAngle.value)
    }

    // Detección de caída

    @Test
    fun `fallDetected should become true when accelerometer emits a fall reading`() =
        runTest(testDispatcher) {
            val fallReading =
                SensorReading(x = 20f, y = 20f, z = 20f, magnitude = 34.64f) // > 24.5 m/s²

            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(fallReading)
            every { accelerometerDataSource.isFallDetected(fallReading) } returns true

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.fallDetected.value)
        }

    @Test
    fun `fallDetected should remain false for normal accelerometer readings`() =
        runTest(testDispatcher) {
            val normalReading =
                SensorReading(x = 0f, y = 0f, z = 9.8f, magnitude = 9.8f) // ≈ 1 G

            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(normalReading)
            every { accelerometerDataSource.isFallDetected(normalReading) } returns false

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.fallDetected.value)
        }

    @Test
    fun `fallDetected should become true only when isFallDetected returns true`() =
        runTest(testDispatcher) {
            val reading = SensorReading(x = 5f, y = 5f, z = 5f, magnitude = 8.66f)

            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(reading)
            every { accelerometerDataSource.isFallDetected(reading) } returns true

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.fallDetected.value)
        }

    @Test
    fun `fallDetected should stay false when isFallDetected returns false`() =
        runTest(testDispatcher) {
            val reading = SensorReading(x = 5f, y = 5f, z = 5f, magnitude = 8.66f)

            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(reading)
            every { accelerometerDataSource.isFallDetected(reading) } returns false

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.fallDetected.value)
        }

    // dismissFallAlert

    @Test
    fun `dismissFallAlert should reset fallDetected to false`() =
        runTest(testDispatcher) {
            val fallReading =
                SensorReading(x = 20f, y = 20f, z = 20f, magnitude = 34.64f)

            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(fallReading)
            every { accelerometerDataSource.isFallDetected(fallReading) } returns true

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.fallDetected.value)

            viewModel.dismissFallAlert()

            assertFalse(viewModel.fallDetected.value)
        }

    @Test
    fun `dismissFallAlert should be safe to call when no fall was detected`() =
        runTest(testDispatcher) {
            every { accelerometerDataSource.getReadingsFlow() } returns flowOf()

            val viewModel = buildViewModel()

            // No debe lanzar excepción
            viewModel.dismissFallAlert()

            assertFalse(viewModel.fallDetected.value)
        }

    // Múltiples lecturas

    @Test
    fun `fallDetected should become true when any reading in sequence is a fall`() =
        runTest(testDispatcher) {
            val normal = SensorReading(x = 0f, y = 0f, z = 9.8f, magnitude = 9.8f)
            val fall = SensorReading(x = 20f, y = 20f, z = 20f, magnitude = 34.64f)

            every { accelerometerDataSource.getReadingsFlow() } returns flowOf(normal, fall)
            every { accelerometerDataSource.isFallDetected(normal) } returns false
            every { accelerometerDataSource.isFallDetected(fall) } returns true

            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.fallDetected.value)
        }

    // setTargetAngle

    @Test
    fun `setTargetAngle should update targetAngle state`() {
        every { accelerometerDataSource.getReadingsFlow() } returns flowOf()

        val viewModel = buildViewModel()
        viewModel.setTargetAngle(90f)

        org.junit.Assert.assertEquals(90f, viewModel.targetAngle.value)
    }

    @Test
    fun `setTargetAngle should allow updating angle multiple times`() {
        every { accelerometerDataSource.getReadingsFlow() } returns flowOf()

        val viewModel = buildViewModel()
        viewModel.setTargetAngle(45f)
        viewModel.setTargetAngle(120f)

        org.junit.Assert.assertEquals(120f, viewModel.targetAngle.value)
    }

    @Test
    fun `precision should not update when targetAngle is null`() =
        runTest(testDispatcher) {
            every { accelerometerDataSource.getReadingsFlow() } returns flowOf()

            val viewModel = buildViewModel()

            // targetAngle es null — syncMotorUseCase no debe ser llamado
            io.mockk.coVerify(exactly = 0) { syncMotorUseCase.execute(any(), any()) }
            org.junit.Assert.assertEquals(
                ar.edu.unlam.mobile.scaffolding.domain.usecase.JointPrecision.IDEAL,
                viewModel.precision.value,
            )
        }
}
