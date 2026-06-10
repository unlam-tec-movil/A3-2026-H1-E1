package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.location.Location
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Dev3PlayGroundViewModelTest {
    private val observerLocationUseCase = mockk<ObserverLocationUseCase>(relaxed = true)
    private val getClinicsFromAssetsUseCase = mockk<GetClinicsFromAssetsUseCase>(relaxed = true)
    private val populateClinicsDbUseCase = mockk<PopulateClinicsDbUseCase>(relaxed = true)
    private val getClinicsStoredUseCase = mockk<GetClinicsStoredUseCase>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: Dev3PlayGroundViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel =
            Dev3PlayGroundViewModel(
                observerLocationUseCase,
                getClinicsFromAssetsUseCase,
                populateClinicsDbUseCase,
                getClinicsStoredUseCase,
            )
    }

    @Test
    fun `initial state should have loading true and empty clinics`() =
        runTest(testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            val state = viewModel.locationUiState.value
            assertTrue(state.isLoadingClinics)
            assertTrue(state.clinics.isEmpty())
        }

    @Test
    fun `when clinics are already stored, it should load them into state`() =
        runTest(testDispatcher) {
            // Arrange
            val storedClinics =
                listOf(
                    Clinic(1, "Clinic 1", "Address 1", "123", "web1", -34.0, -58.0),
                )
            every { getClinicsStoredUseCase() } returns flowOf(storedClinics)

            // Act
            createViewModel()
            advanceUntilIdle()

            // Assert
            val state = viewModel.locationUiState.value
            assertFalse(state.isLoadingClinics)
            assertTrue(state.clinicsLoadSuccess)
            assertEquals(storedClinics, state.clinics)
            coVerify(exactly = 0) { getClinicsFromAssetsUseCase() }
            coVerify(exactly = 0) { populateClinicsDbUseCase(any()) }
        }

    @Test
    fun `when no clinics are stored, it should fetch from assets and populate db`() =
        runTest(testDispatcher) {
            // Arrange
            val clinicsFromAssets =
                listOf(
                    Clinic(1, "Asset Clinic", "Addr", "phone", "web", 0.0, 0.0),
                )
            // Emit empty first to trigger the population
            every { getClinicsStoredUseCase() } returns flowOf(emptyList(), clinicsFromAssets)
            coEvery { getClinicsFromAssetsUseCase() } returns clinicsFromAssets

            // Act
            createViewModel()
            advanceUntilIdle()

            // Assert
            coVerify { getClinicsFromAssetsUseCase() }
            coVerify { populateClinicsDbUseCase(clinicsFromAssets) }

            val state = viewModel.locationUiState.value
            assertEquals(clinicsFromAssets, state.clinics)
        }

    @Test
    fun `onLocationPermissionGranted should start observing location`() =
        runTest(testDispatcher) {
            // Arrange
            val mockLocation = mockk<Location>(relaxed = true)
            every { observerLocationUseCase() } returns flowOf(mockLocation)
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            // Act
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            // Assert
            val state = viewModel.locationUiState.value
            assertTrue(state.permissionGranted)
            assertTrue(state.showMap)
            assertEquals(mockLocation, state.location)
        }

    @Test
    fun `onPermissionCheckComplete should update permission state`() =
        runTest(testDispatcher) {
            // Arrange
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            // Act
            viewModel.onPermissionCheckComplete(true)

            // Assert
            val state = viewModel.locationUiState.value
            assertTrue(state.permissionGranted)
            assertFalse(state.isLoadingPermission)
        }

    @Test
    fun `when getClinicsStoredUseCase fails, it should update state with error`() =
        runTest(testDispatcher) {
            // Arrange
            val errorMessage = "Database error"
            every { getClinicsStoredUseCase() } throws Exception(errorMessage)

            // Act
            createViewModel()
            advanceUntilIdle()

            // Assert
            val state = viewModel.locationUiState.value
            assertFalse(state.isLoadingClinics)
            assertFalse(state.clinicsLoadSuccess)
            assertEquals(errorMessage, state.clinicsLoadError)
        }
}
