package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.service.local.db.getNearestClinics
import ar.edu.unlam.mobile.scaffolding.application.service.local.remote.routing.GetRouteUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.mapprefs.GetLastDestinationClinicIdUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.mapprefs.SaveLastDestinationClinicIdUseCase
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.maptiler.maptilersdk.events.MTEvent
import com.maptiler.maptilersdk.helpers.MTDashArrayOption
import com.maptiler.maptilersdk.helpers.MTNumberOrZoomNumberValues
import com.maptiler.maptilersdk.helpers.MTPolylineLayerHelper
import com.maptiler.maptilersdk.helpers.MTPolylineLayerOptions
import com.maptiler.maptilersdk.helpers.MTStringOrZoomStringValues
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.options.MTCameraOptions
import com.maptiler.maptilersdk.map.style.source.MTGeoJSONSource
import com.maptiler.maptilersdk.map.types.MTData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --------------------------------------------------------------------------
// UI State
// --------------------------------------------------------------------------

data class LocationUiState(
    val showRoute: Boolean = false,
    val location: Location? = null,
    val showMap: Boolean = false,
    val isLoadingClinics: Boolean = true,
    val isLoadingPermission: Boolean = true,
    val permissionGranted: Boolean = false,
    val clinicsLoadSuccess: Boolean = false,
    val clinicsLoadError: String? = null,
    val clinics: List<Clinic> = emptyList(),
    val clinicsNear: List<Clinic> = emptyList(),
    val selectedClinic: Clinic? = null,
    val searchBarText: String = "",
    val routeDistance: Double? = null,
    val routeTime: String? = null,
    val mapInitialized: Boolean = false,
    val lastSavedClinicId: Int? = null,
    val routeError: String? = null,
    val lastRouteRequestLocation: LatLng? = null,
) {
    // Returns all clinics when search is empty; filters by name otherwise
    val filteredClinics: List<Clinic>
        get() =
            if (searchBarText.isEmpty()) {
                clinics
            } else {
                clinics.filter { it.name.contains(searchBarText, ignoreCase = true) }
            }
}

// --------------------------------------------------------------------------
// ViewModel
// --------------------------------------------------------------------------

@HiltViewModel
class MapScreenViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val observerLocationUseCase: ObserverLocationUseCase,
        private val getClinicsStoredUseCase: GetClinicsStoredUseCase,
        private val getRouteUseCase: GetRouteUseCase,
        private val saveLastDestinationClinicIdUseCase: SaveLastDestinationClinicIdUseCase,
        private val getLastDestinationClinicIdUseCase: GetLastDestinationClinicIdUseCase,
    ) : ViewModel() {
        companion object {
            private const val ROUTE_LAYER_ID = "basic-polyline"
            private const val ROUTE_SOURCE_ID = "basic-polyline-source"
            private const val NEARBY_DISPLAY_COUNT = 15
            private const val NEARBY_SORT_COUNT = 60
            private const val CLUSTER_COUNT = 12
            private const val DEFAULT_MAP_ZOOM = 15.0
            private const val MIN_DISTANCE_FOR_NEW_ROUTE = 20.0
        }

        val mapController: MTMapViewController by lazy {
            MTMapViewController(context).apply {
                delegate =
                    object : MTMapViewDelegate {
                        override fun onMapViewInitialized() {
                            _mapScreenUiState.update { it.copy(mapInitialized = true) }
                            if (_mapScreenUiState.value.location != null) {
                                setupClusters()
                            }
                        }

                        override fun onEventTriggered(
                            event: MTEvent,
                            data: MTData?,
                        ) = Unit
                    }
            }
        }

        private val _mapScreenUiState = MutableStateFlow(LocationUiState())
        val mapScreenUiState = _mapScreenUiState.asStateFlow()

        init {
            loadClinics()
            loadLastSavedClinic()
        }

        override fun onCleared() {
            super.onCleared()
            mapController.destroy()
        }

        // ------------------------------------------------------------------
        // Public event handlers
        // ------------------------------------------------------------------

        fun onLocationPermissionGranted() {
            viewModelScope.launch {
                observerLocationUseCase().collect { location ->
                    _mapScreenUiState.update { current ->
                        current.copy(
                            location = location,
                            showMap = true,
                            permissionGranted = true,
                            isLoadingPermission = false,
                            clinicsNear =
                                current.clinics.getNearestClinics(
                                    userLat = location.latitude,
                                    userLng = location.longitude,
                                    count = NEARBY_DISPLAY_COUNT,
                                ),
                            clinics =
                                current.clinics.getNearestClinics(
                                    userLat = location.latitude,
                                    userLng = location.longitude,
                                    count = NEARBY_SORT_COUNT,
                                ),
                        )
                    }
                }
            }
        }

        fun onPermissionCheckComplete(granted: Boolean) {
            _mapScreenUiState.update {
                it.copy(permissionGranted = granted, isLoadingPermission = false)
            }
        }

        fun onClinicSelectedChange(newClinic: Clinic) {
            _mapScreenUiState.update {
                it.copy(selectedClinic = newClinic, routeDistance = null, routeTime = null)
            }
        }

        fun onSearchBarInputChange(newValue: String) {
            _mapScreenUiState.update { it.copy(searchBarText = newValue) }
        }

        fun onCallTriggered() {
            val phone =
                _mapScreenUiState.value.selectedClinic
                    ?.phone
                    ?.takeIf { it.isNotEmpty() } ?: return

            val intent =
                Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${Uri.encode(phone)}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
        }

        fun onRestoreLastRouteClick(hexColor: String) {
            val savedId = _mapScreenUiState.value.lastSavedClinicId ?: return
            val clinic = _mapScreenUiState.value.clinics.find { it.id == savedId } ?: return
            onClinicSelectedChange(clinic)
            onCreateRouteClick(hexColor)
        }

        fun onHideCardSheetAndRemoveRouteLayer() {
            _mapScreenUiState.update { currentState ->
                currentState.copy(showRoute = false)
            }
            val style = mapController.style ?: return
            style.removeLayerById(ROUTE_LAYER_ID)
            style.removeSourceById(ROUTE_SOURCE_ID)
        }

        fun onCreateRouteClick(hexColor: String) {
            val state = _mapScreenUiState.value
            val userLocation =
                state.location?.let {
                    LatLng(it.latitude, it.longitude)
                } ?: return

            // If we already have a route and the user hasn't moved much, don't request a new one
            if (state.showRoute && state.lastRouteRequestLocation != null) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    state.lastRouteRequestLocation.latitude,
                    state.lastRouteRequestLocation.longitude,
                    userLocation.latitude,
                    userLocation.longitude,
                    results,
                )
                if (results[0] < MIN_DISTANCE_FOR_NEW_ROUTE) return
            }

            _mapScreenUiState.update { currentState ->
                currentState.copy(showRoute = true, routeError = null)
            }
            viewModelScope.launch {
                try {
                    val clinic = state.selectedClinic ?: return@launch

                    saveLastDestinationClinicIdUseCase(clinic.id)

                    val destination = LatLng(clinic.lat, clinic.lng)
                    val response: RouteResponse =
                        getRouteUseCase(
                            origin = userLocation,
                            destination = destination,
                        )

                    val routeDistance = response.paths.firstOrNull()?.distance
                    val path = response.paths.firstOrNull()
                    val timeInMills = path?.time ?: 0L
                    val fTime = formatTravelTime(timeInMills)

                    _mapScreenUiState.update { currentState ->
                        currentState.copy(
                            routeDistance = routeDistance,
                            routeTime = fTime,
                            lastRouteRequestLocation = userLocation,
                        )
                    }

                    val points = response.paths[0].points
                    val feature =
                        JsonObject().apply {
                            addProperty("type", "Feature")
                            add("properties", JsonObject())
                            add(
                                "geometry",
                                JsonObject().apply {
                                    addProperty("type", "LineString")
                                    add(
                                        "coordinates",
                                        JsonArray().apply {
                                            points.coordinates.forEach { coordenadas ->
                                                add(
                                                    JsonArray().apply {
                                                        add(coordenadas[0])
                                                        add(coordenadas[1])
                                                    },
                                                )
                                            }
                                        },
                                    )
                                },
                            )
                        }
                    val featureCollection =
                        JsonObject().apply {
                            addProperty("type", "FeatureCollection")
                            add("features", JsonArray().apply { add(feature) })
                        }
                    val lineGeoJson = featureCollection.toString()

                    mapController.style?.let { style ->
                        style.removeLayerById(ROUTE_LAYER_ID)
                        style.removeSourceById(ROUTE_SOURCE_ID)

                        val helper: MTPolylineLayerHelper = style.polylineHelper()
                        val opts =
                            MTPolylineLayerOptions(
                                data = lineGeoJson,
                                layerId = ROUTE_LAYER_ID,
                                sourceId = ROUTE_SOURCE_ID,
                                lineColor = MTStringOrZoomStringValues.StringValue(hexColor),
                                lineWidth = MTNumberOrZoomNumberValues.Number(4.0),
                                lineOpacity = MTNumberOrZoomNumberValues.Number(0.9),
                                lineDashArray = MTDashArrayOption.Numbers(listOf(2.0, 1.0)),
                            )
                        helper.addPolyline(opts)
                    }
                } catch (e: Exception) {
                    _mapScreenUiState.update { currentState ->
                        currentState.copy(
                            showRoute = false,
                            routeError = "Could not load route. Please try again later.",
                        )
                    }
                }
            }
        }

        fun setupClusters() {
            viewModelScope.launch {
                val state = _mapScreenUiState.value
                val location = state.location ?: return@launch
                val style = mapController.style ?: return@launch

                if (state.clinics.isEmpty()) {
                    _mapScreenUiState.update { it.copy(clinicsLoadError = "Failed to load clinics") }
                    return@launch
                }

                val nearbyClinics =
                    state.clinics.getNearestClinics(
                        userLat = location.latitude,
                        userLng = location.longitude,
                        count = CLUSTER_COUNT,
                    )

                val featureCollection = buildFeatureCollection(nearbyClinics)
                style.addSource(
                    MTGeoJSONSource(
                        identifier = "clinics",
                        jsonString = featureCollection.toString(),
                    ),
                )
            }
        }

        fun centerCameraOn(
            target: LngLat,
            zoom: Double = DEFAULT_MAP_ZOOM,
        ) {
            mapController.easeTo(
                cameraOptions =
                    MTCameraOptions(
                        zoom = zoom,
                        center = LngLat(lng = target.lng, lat = target.lat),
                    ),
            )
        }

        // ------------------------------------------------------------------
        // Private helpers
        // ------------------------------------------------------------------

        private fun loadClinics() {
            viewModelScope.launch {
                getClinicsStoredUseCase()
                    .catch { e ->
                        _mapScreenUiState.update {
                            it.copy(
                                isLoadingClinics = false,
                                clinicsLoadSuccess = false,
                                clinicsLoadError = e.message ?: "Unknown error",
                            )
                        }
                    }.collect { allClinics ->
                        _mapScreenUiState.update {
                            it.copy(
                                clinics = allClinics,
                                isLoadingClinics = false,
                                clinicsLoadSuccess = true,
                            )
                        }
                    }
            }
        }

        private fun loadLastSavedClinic() {
            viewModelScope.launch {
                getLastDestinationClinicIdUseCase().collect { lastClinicId ->
                    _mapScreenUiState.update { it.copy(lastSavedClinicId = lastClinicId) }
                }
            }
        }

        private fun buildClinicFeature(clinic: Clinic): JsonObject =
            JsonObject().apply {
                addProperty("type", "Feature")
                addProperty("id", clinic.id)
                add(
                    "properties",
                    JsonObject().apply {
                        addProperty("name", clinic.name)
                        addProperty("address", clinic.address)
                        addProperty("phone", clinic.phone)
                        addProperty("website", clinic.website)
                    },
                )
                add(
                    "geometry",
                    JsonObject().apply {
                        addProperty("type", "Point")
                        add(
                            "coordinates",
                            JsonArray().apply {
                                add(clinic.lng)
                                add(clinic.lat)
                            },
                        )
                    },
                )
            }

        private fun buildFeatureCollection(clinics: List<Clinic>): JsonObject =
            JsonObject().apply {
                addProperty("type", "FeatureCollection")
                add(
                    "features",
                    JsonArray().apply {
                        clinics.forEach { add(buildClinicFeature(it)) }
                    },
                )
            }

        private fun formatTravelTime(timeInMillis: Long): String {
            val minutes = (timeInMillis / 60000).toInt()
            return if (minutes < 60) "$minutes min" else "${minutes / 60}h ${minutes % 60}min"
        }
    }
