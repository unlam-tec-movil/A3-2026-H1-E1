package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.location.Location
import ar.edu.unlam.mobile.scaffolding.application.port.inn.routing.GetRouteUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Path
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Points
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import com.google.android.gms.maps.model.LatLng
import com.maptiler.maptilersdk.map.style.MTStyle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private val getRouteUseCase = mockk<GetRouteUseCase>(relaxed = true)

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
                getRouteUseCase,
            )
    }

    @Test
    fun `initial state should have loading true and empty clinics`() =
        runTest(testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            val state = viewModel.mapScreenUiState.value
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
            val state = viewModel.mapScreenUiState.value
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

            val state = viewModel.mapScreenUiState.value
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
            val state = viewModel.mapScreenUiState.value
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
            val state = viewModel.mapScreenUiState.value
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
            val state = viewModel.mapScreenUiState.value
            assertFalse(state.isLoadingClinics)
            assertFalse(state.clinicsLoadSuccess)
            assertEquals(errorMessage, state.clinicsLoadError)
        }

    @Test
    fun `onSearchBarInputChange should update searchBarText`() =
        runTest(context = testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()
            viewModel.onSearchBarInputChange("test")

            assertEquals("test", viewModel.mapScreenUiState.value.searchBarText)
        }

    @Test
    fun `filteredClinics should store clincs by the searchBarText value`() =
        runTest(context = testDispatcher) {
            val clinics =
                listOf<Clinic>(
                    Clinic(
                        id = 1,
                        name = "Hospital 1",
                        address = "",
                        phone = "",
                        website = "",
                        lat = 0.2,
                        lng = 0.0,
                    ),
                    Clinic(
                        id = 2,
                        name = "Clinica 2",
                        address = "",
                        phone = "",
                        website = "",
                        lat = 0.2,
                        lng = 0.0,
                    ),
                )
            every { getClinicsStoredUseCase() } returns flowOf(clinics)
            createViewModel()
            advanceUntilIdle()

            viewModel.onSearchBarInputChange("inica")

            val filtered = viewModel.mapScreenUiState.value.filteredClinics
            assertEquals(1, filtered.size)
            assertEquals("Clinica 2", filtered[0].name)
        }

    @Test
    fun `onSelectedClinicChange should update selectedClinic`() =
        runTest(context = testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()
            val clinic =
                Clinic(
                    id = 1,
                    name = "Play Station",
                    address = "Addresss 1",
                    phone = "+123",
                    website = "Website",
                    lat = -55.0,
                    lng = -35.1,
                )
            viewModel.onClinicSelectedChange(clinic, null)
            assertEquals(clinic, viewModel.mapScreenUiState.value.selectedClinic)
        }

    @Test
    fun `onCreateRouteClinic should call getRouteUseCase and update style`() =
        runTest(context = testDispatcher) {
            val mockLocation = mockk<Location>(relaxed = true)
            every { mockLocation.latitude } returns -34.0
            every { mockLocation.longitude } returns -58.0
            every { observerLocationUseCase() } returns flowOf(mockLocation)
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            val clinic =
                Clinic(
                    1,
                    name = "Leonardo Kinesiologia",
                    address = "The last ronnin",
                    phone = "01",
                    website = "comic",
                    lat = -34.1,
                    lng = -58.1,
                )
            val mockStyle = mockk<MTStyle>(relaxed = true)
            val mockRouteResponse =
                RouteResponse(
                    paths =
                        listOf(
                            Path(
                                points =
                                    Points(
                                        type = "LineString",
                                        coordinates = listOf(listOf(-58.0, -34.0), listOf(-58.1, -34.1)),
                                    ),
                                distance = 100.0,
                            ),
                        ),
                )
            coEvery {
                getRouteUseCase.invoke(
                    origin = any(),
                    destination = any(),
                )
            } returns mockRouteResponse
            viewModel.onCreateRouteClick(mockStyle, clinic)
            advanceUntilIdle()

            coVerify { getRouteUseCase.invoke(LatLng(-34.0, -58.0), LatLng(-34.1, -58.1)) }
            verify { mockStyle.removeLayerById("basic-polyline") }
            verify { mockStyle.removeSourceById("basic-polyline-source") }
            verify { mockStyle.polylineHelper() }
        }

    @Test
    fun `setUpClusters should add GeoJson source to style when clinics exists`() =
        runTest(context = testDispatcher) {
            val clinics =
                listOf(
                    Clinic(
                        1,
                        "Clinic 1",
                        "Address 1",
                        "123",
                        "web1",
                        0.0,
                        0.0,
                    ),
                )
            every { getClinicsStoredUseCase() } returns flowOf(clinics)
            createViewModel()
            val mockStyle = mockk<MTStyle>(relaxed = true)

            viewModel.setupClusters(mockStyle)
            advanceUntilIdle()

            verify { mockStyle.addSource(any()) }
        }
}
