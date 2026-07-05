package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import ar.edu.unlam.mobile.scaffolding.application.service.local.remote.routing.GetRouteUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.mapprefs.GetLastDestinationClinicIdUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.mapprefs.SaveLastDestinationClinicIdUseCase
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Path
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Points
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.toHex
import com.google.android.gms.maps.model.LatLng
import com.maptiler.maptilersdk.helpers.MTPolylineLayerHelper
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.style.MTStyle
import com.maptiler.maptilersdk.map.style.source.MTGeoJSONSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
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
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapScreenViewModelTest {
    private val mockedContext = mockk<Context>(relaxed = true)
    private val observerLocationUseCase = mockk<ObserverLocationUseCase>(relaxed = true)
    private val getClinicsFromAssetsUseCase = mockk<GetClinicsFromAssetsUseCase>(relaxed = true)
    private val populateClinicsDbUseCase = mockk<PopulateClinicsDbUseCase>(relaxed = true)
    private val getClinicsStoredUseCase = mockk<GetClinicsStoredUseCase>(relaxed = true)
    private val getRouteUseCase = mockk<GetRouteUseCase>(relaxed = true)
    private val saveLastDestinationClinicIdUseCase = mockk<SaveLastDestinationClinicIdUseCase>(relaxed = true)
    private val getLastDestinationClinicIdUseCase = mockk<GetLastDestinationClinicIdUseCase>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MapScreenViewModel
    private val mockStyle = mockk<MTStyle>(relaxed = true)
    private val mockMapController = mockk<MTMapViewController>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(Location::class)
        mockkStatic(Uri::class)
        mockkConstructor(MTGeoJSONSource::class)
        mockkConstructor(Intent::class)

        // Mock static helpers if they are extensions
        try {
            mockkStatic("com.maptiler.maptilersdk.helpers.MTStyleHelpersKt")
        } catch (e: Exception) {
        }

        // Setup Uri mocks to prevent it returning null
        val mockUri = mockk<Uri>(relaxed = true)
        every { Uri.fromParts(any(), any(), any()) } returns mockUri
        every { Uri.parse(any()) } returns mockUri
        every { Uri.encode(any()) } answers { it.invocation.args[0] as String }

        every { Location.distanceBetween(any(), any(), any(), any(), any()) } returns Unit
        every { mockMapController.style } returns mockStyle
        every { mockMapController.easeTo(any()) } returns Unit
        every { mockMapController.destroy() } returns Unit
        every { mockStyle.removeLayerById(any()) } returns Unit
        every { mockStyle.removeSourceById(any()) } returns Unit
        every { mockStyle.addSource(any()) } returns Unit

        // Relax the constructed intents to avoid stdObjectAnswer error
        every { anyConstructed<Intent>().action } returns null
        every { anyConstructed<Intent>().setData(any()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().setFlags(any()) } returns mockk(relaxed = true)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel =
            MapScreenViewModel(
                context = mockedContext,
                observerLocationUseCase = observerLocationUseCase,
                getClinicsStoredUseCase = getClinicsStoredUseCase,
                getRouteUseCase = getRouteUseCase,
                saveLastDestinationClinicIdUseCase = saveLastDestinationClinicIdUseCase,
                getLastDestinationClinicIdUseCase = getLastDestinationClinicIdUseCase,
            )

        // Inject the mock into the lazy property using reflection to avoid library initialization crashes
        try {
            // Find the lazy delegate field. It usually ends with $delegate
            val fields = MapScreenViewModel::class.java.getDeclaredFields()
            val delegateField = fields.find { it.name.contains("mapController") && it.name.contains("delegate") }
            if (delegateField != null) {
                delegateField.isAccessible = true
                delegateField.set(viewModel, lazyOf(mockMapController))
            } else {
                // Fallback for different naming conventions
                val field = MapScreenViewModel::class.java.getDeclaredField("mapController\$delegate")
                field.isAccessible = true
                field.set(viewModel, lazyOf(mockMapController))
            }
        } catch (e: Exception) {
            // Ignore if injection fails, constructor mocking should pick it up
        }
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
            every { getClinicsStoredUseCase() } returns
                flow {
                    throw Exception(errorMessage)
                }
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
            viewModel.onClinicSelectedChange(clinic)
            assertEquals(clinic, viewModel.mapScreenUiState.value.selectedClinic)
        }

    @Test
    fun `onCreateRouteClinic should call getRouteUseCase and update style`() =
        runTest(context = testDispatcher) {
            val mockLocation =
                mockk<Location>(relaxed = true) {
                    every { latitude } returns -34.0
                    every { longitude } returns -58.0
                }
            val targetClinic =
                Clinic(
                    1,
                    name = "Leonardo Kinesiologia",
                    address = "The last ronnin",
                    phone = "01",
                    website = "comic",
                    lat = -34.1,
                    lng = -58.1,
                )

            every { observerLocationUseCase() } returns flowOf(mockLocation)
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())

            // Mock polyline helper to avoid crash if it's an extension
            val mockHelper = mockk<com.maptiler.maptilersdk.helpers.MTPolylineLayerHelper>(relaxed = true)
            // Note: If polylineHelper() is an extension, we need mockkStatic

            createViewModel()
            viewModel.onLocationPermissionGranted()
            viewModel.onClinicSelectedChange(targetClinic)
            advanceUntilIdle()

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
                                time = 10000000L,
                            ),
                        ),
                )
            coEvery {
                getRouteUseCase.invoke(
                    origin = any(),
                    destination = any(),
                )
            } returns mockRouteResponse

            viewModel.onCreateRouteClick(Color.Red.toHex())
            advanceUntilIdle()

            coVerify { getRouteUseCase.invoke(LatLng(-34.0, -58.0), LatLng(-34.1, -58.1)) }

            val state = viewModel.mapScreenUiState.value
            Assert.assertNotNull(state.routeDistance)
            Assert.assertNotNull(state.routeTime)
        }

    @Test
    fun `setUpClusters should add GeoJson source to style when clinics exists`() =
        runTest(context = testDispatcher) {
            // 1. Arrange: Clinics AND Location
            val clinics = listOf(Clinic(1, "Test", "Addr", "123", "web", 0.0, 0.0))
            val mockLocation =
                mockk<Location>(relaxed = true) {
                    every { latitude } returns -34.0
                    every { longitude } returns -58.0
                }

            every { getClinicsStoredUseCase() } returns flowOf(clinics)
            every { observerLocationUseCase() } returns flowOf(mockLocation)

            createViewModel()

            // 2. Act: Ensure location is set in state
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            // 3. Run the function
            viewModel.setupClusters()
            advanceUntilIdle()

            // 4. Assert: Verify DIRECTLY on mockStyle
            verify { mockStyle.addSource(any()) }
        }

    @Test
    fun `init should load last saved clinic ID from use case`() =
        runTest(testDispatcher) {
            // Arrange
            val savedId = 42
            every { getLastDestinationClinicIdUseCase() } returns flowOf(savedId)
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())

            // Act
            createViewModel()
            advanceUntilIdle()

            // Assert
            assertEquals(savedId, viewModel.mapScreenUiState.value.lastSavedClinicId)
        }

    @Test
    fun `onRestoreLastRouteClick should select clinic and start route if ID exists`() =
        runTest(testDispatcher) {
            // 1. Arrange: Setup ID, Clinic, and a Mock Location
            val savedId = 1
            val mockLocation =
                mockk<Location>(relaxed = true) {
                    every { latitude } returns -34.0
                    every { longitude } returns -58.0
                }
            val clinics =
                listOf(
                    Clinic(
                        id = 1,
                        name = "Saved Clinic",
                        lat = -34.1,
                        lng = -58.1,
                        address = "",
                        phone = "",
                        website = "",
                    ),
                )

            // Mock UseCases
            every { getLastDestinationClinicIdUseCase() } returns flowOf(savedId)
            every { getClinicsStoredUseCase() } returns flowOf(clinics)
            every { observerLocationUseCase() } returns flowOf(mockLocation)

            // Mock Route Response to prevent crash on response.paths[0]
            coEvery { getRouteUseCase(any(), any()) } returns
                RouteResponse(
                    paths = listOf(mockk(relaxed = true)),
                )

            createViewModel()
            advanceUntilIdle() // Process init {}

            // 2. Act: Set location first
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle() // Process location collection

            // 3. Act: Restore route
            viewModel.onRestoreLastRouteClick("#FF0000")
            advanceUntilIdle() // Process route request

            // 4. Assert
            val state = viewModel.mapScreenUiState.value
            assertEquals(clinics[0], state.selectedClinic)
            assertTrue("Show route should be true", state.showRoute)
        }

    @Test
    fun `onCallTriggered should not do anything when clinic phone is empty`() =
        runTest(testDispatcher) {
            // Arrange
            val clinicWithoutPhone = Clinic(1, "Test Clinic", "Address", "", "web", -34.0, -58.0)
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()
            viewModel.onClinicSelectedChange(clinicWithoutPhone)

            // Act - should return early without crashing
            viewModel.onCallTriggered()

            // Assert - state should remain unchanged
            val state = viewModel.mapScreenUiState.value
            assertEquals(clinicWithoutPhone, state.selectedClinic)
        }

    @Test
    fun `onCallTriggered should not do anything when no clinic is selected`() =
        runTest(testDispatcher) {
            // Arrange
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()
            // No clinic selected

            // Act - should return early without crashing
            viewModel.onCallTriggered()

            // Assert - no clinic should be selected
            val state = viewModel.mapScreenUiState.value
            Assert.assertNull(state.selectedClinic)
        }

//    @Test
//    fun `onCallTriggered should start ACTION_DIAL intent when clinic has phone`() =
//        runTest(testDispatcher) {
//            // Arrange
//            val clinic = Clinic(1, "Test Clinic", "Address", "123456", "web", -34.0, -58.0)
//            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
//            // Specific mock for this test instance
//            every { anyConstructed<Intent>().action } returns Intent.ACTION_DIAL
//
//            createViewModel()
//            viewModel.onClinicSelectedChange(clinic)
//
//            // Act
//            viewModel.onCallTriggered()
//
//            // Assert
//            verify {
//                mockedContext.startActivity(
//                    match {
//                        it.action == Intent.ACTION_DIAL
//                    },
//                )
//            }
//        }
//
//    @Test
//    fun `onGoToConfigClick should start ACTION_APPLICATION_DETAILS_SETTINGS intent`() =
//        runTest(testDispatcher) {
//            // Arrange
//            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
//            every { mockedContext.packageName } returns "ar.edu.unlam.mobile.scaffolding"
//            // Specific mock for this test instance
//            every { anyConstructed<Intent>().action } returns Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//
//            createViewModel()
//
//            // Act
//            viewModel.onGoToConfigClick()
//
//            // Assert
//            verify {
//                mockedContext.startActivity(
//                    match {
//                        it.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    },
//                )
//            }
//        }

    @Test
    fun `onHideCardSheetAndRemoveRouteLayer should set showRoute to false and remove layers`() =
        runTest(testDispatcher) {
            // Arrange
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()
//            _mapScreenUiState.update { it.copy(showRoute = true) }

            // Act
            viewModel.onHideCardSheetAndRemoveRouteLayer()

            // Assert
            assertFalse(viewModel.mapScreenUiState.value.showRoute)
            verify { mockStyle.removeLayerById("basic-polyline") }
            verify { mockStyle.removeSourceById("basic-polyline-source") }
        }

    @Test
    fun `centerCameraOn should update map camera to target location and zoom`() =
        runTest(testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            val targetLngLat = LngLat(lng = -58.0, lat = -34.0)
            val zoom = 18.0

            viewModel.centerCameraOn(targetLngLat, zoom)

            verify {
                mockMapController.easeTo(
                    cameraOptions =
                        match {
                            it.zoom == zoom &&
                                it.center?.lng == -58.0 &&
                                it.center?.lat == -34.0
                        },
                )
            }
        }

    @Test
    fun `centerCameraOn should use default zoom when not provided`() =
        runTest(testDispatcher) {
            // Arrange
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            val targetLngLat = LngLat(lng = -58.0, lat = -34.0)

            // Act
            viewModel.centerCameraOn(targetLngLat)

            // Assert - Verify directly on the controller instance
            verify {
                mockMapController.easeTo(
                    cameraOptions =
                        match {
                            // Check zoom and coordinates specifically
                            it.zoom == 15.0 && it.center?.lng == -58.0
                        },
                )
            }
        }

    @Test
    fun `formatTravelTime should return minutes format for times under 60 minutes`() =
        runTest(testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            // Access via reflection since it's private
            val method = MapScreenViewModel::class.java.getDeclaredMethod("formatTravelTime", Long::class.java)
            method.isAccessible = true

            val result30Min = method.invoke(viewModel, 30 * 60000L) as String
            assertEquals("30 min", result30Min)

            val result1Min = method.invoke(viewModel, 1 * 60000L) as String
            assertEquals("1 min", result1Min)
        }

    @Test
    fun `formatTravelTime should return hours and minutes format for times 60 minutes or more`() =
        runTest(testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            val method = MapScreenViewModel::class.java.getDeclaredMethod("formatTravelTime", Long::class.java)
            method.isAccessible = true

            val result1Hour30Min = method.invoke(viewModel, 90 * 60000L) as String
            assertEquals("1h 30min", result1Hour30Min)

            val result2Hours = method.invoke(viewModel, 120 * 60000L) as String
            assertEquals("2h 0min", result2Hours)
        }

    @Test
    fun `onCreateRouteClick should not request new route if user location is null`() =
        runTest(testDispatcher) {
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            every { observerLocationUseCase() } returns flowOf<Location>()
            createViewModel()

            val clinic = Clinic(1, "Test", "Address", "123", "web", -34.1, -58.1)
            viewModel.onClinicSelectedChange(clinic)

            viewModel.onCreateRouteClick("#FF0000")
            advanceUntilIdle()

            coVerify(exactly = 0) { getRouteUseCase(any(), any()) }
        }

    @Test
    fun `onCreateRouteClick should not request new route if movement less than MIN_DISTANCE_FOR_NEW_ROUTE`() =
        runTest(testDispatcher) {
            val mockLocation =
                mockk<Location>(relaxed = true) {
                    every { latitude } returns -34.0
                    every { longitude } returns -58.0
                }

            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            every { observerLocationUseCase() } returns flowOf(mockLocation)
            createViewModel()
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            val clinic = Clinic(1, "Test", "Address", "123", "web", -34.1, -58.1)
            viewModel.onClinicSelectedChange(clinic)

            val mockRouteResponse = RouteResponse(paths = listOf(mockk(relaxed = true)))
            coEvery { getRouteUseCase(any(), any()) } returns mockRouteResponse

            // First route request
            viewModel.onCreateRouteClick("#FF0000")
            advanceUntilIdle()

            // Reset mock to count second invocation
            coEvery { getRouteUseCase(any(), any()) } returns mockRouteResponse

            // Second route request with small movement
            viewModel.onCreateRouteClick("#FF0000")
            advanceUntilIdle()

            // Should only be called once since movement is less than MIN_DISTANCE_FOR_NEW_ROUTE (20.0)
            coVerify(exactly = 1) { getRouteUseCase(any(), any()) }
        }

    @Test
    fun `onCreateRouteClick should handle route response errors gracefully`() =
        runTest(testDispatcher) {
            val mockLocation =
                mockk<Location>(relaxed = true) {
                    every { latitude } returns -34.0
                    every { longitude } returns -58.0
                }

            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            every { observerLocationUseCase() } returns flowOf(mockLocation)
            coEvery { getRouteUseCase(any(), any()) } throws Exception("Route service error")

            createViewModel()
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            val clinic = Clinic(1, "Test", "Address", "123", "web", -34.1, -58.1)
            viewModel.onClinicSelectedChange(clinic)

            viewModel.onCreateRouteClick("#FF0000")
            advanceUntilIdle()

            val state = viewModel.mapScreenUiState.value
            assertFalse(state.showRoute)
            assertEquals("Could not load route. Please try again later.", state.routeError)
        }

    @Test
    fun `onRestoreLastRouteClick should not do anything if saved clinic ID is null`() =
        runTest(testDispatcher) {
            every { getLastDestinationClinicIdUseCase() } returns flowOf(null)
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())

            createViewModel()
            advanceUntilIdle()

            val initialState = viewModel.mapScreenUiState.value
            viewModel.onRestoreLastRouteClick("#FF0000")

            val finalState = viewModel.mapScreenUiState.value
            assertEquals(initialState.selectedClinic, finalState.selectedClinic)
        }

    @Test
    fun `onRestoreLastRouteClick should not do anything if clinic with saved ID not found`() =
        runTest(testDispatcher) {
            val clinics = listOf(Clinic(1, "Clinic 1", "Address", "123", "web", -34.0, -58.0))
            every { getLastDestinationClinicIdUseCase() } returns flowOf(999) // ID doesn't exist
            every { getClinicsStoredUseCase() } returns flowOf(clinics)

            createViewModel()
            advanceUntilIdle()

            val initialState = viewModel.mapScreenUiState.value
            viewModel.onRestoreLastRouteClick("#FF0000")

            val finalState = viewModel.mapScreenUiState.value
            assertEquals(initialState.selectedClinic, finalState.selectedClinic)
        }

    @Test
    fun `setupClusters should update error state when clinics list is empty`() =
        runTest(testDispatcher) {
            // Arrange
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            val mockLocation = mockk<Location>(relaxed = true)
            every { observerLocationUseCase() } returns flowOf(mockLocation)

            createViewModel()
            viewModel.onLocationPermissionGranted()
            advanceUntilIdle()

            // Act
            viewModel.setupClusters()
            advanceUntilIdle()

            // Assert
            assertEquals("Failed to load clinics", viewModel.mapScreenUiState.value.clinicsLoadError)
        }

    @Test
    fun `onCleared should destroy mapController`() =
        runTest(testDispatcher) {
            // Arrange
            every { getClinicsStoredUseCase() } returns flowOf(emptyList())
            createViewModel()

            // Act - call protected onCleared using reflection
            val method = androidx.lifecycle.ViewModel::class.java.getDeclaredMethod("onCleared")
            method.isAccessible = true
            method.invoke(viewModel)

            // Assert
            verify { mockMapController.destroy() }
        }
}
