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

@HiltViewModel
class MapScreenViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val observerLocationUseCase: ObserverLocationUseCase,
        private val getClinicsStoredUseCase: GetClinicsStoredUseCase,
        private val getRouteUseCase: GetRouteUseCase,
    ) : ViewModel() {
        val mapController: MTMapViewController by lazy {
            MTMapViewController(context).apply {
                delegate =
                    object : MTMapViewDelegate {
                        override fun onMapViewInitialized() {
                            _mapScreenUiState.update { currentState ->
                                currentState.copy(mapInitialized = true)
                            }
                            if (_mapScreenUiState.value.location != null) {
                                setupClusters()
                            }
                        }

                        override fun onEventTriggered(
                            event: MTEvent,
                            data: MTData?,
                        ) {
                        }
                    }
            }
        }

        @Suppress("ktlint:standard:backing-property-naming")
        private val _mapScreenUiState = MutableStateFlow(LocationUiSate())
        val mapScreenUiState = _mapScreenUiState.asStateFlow()

        init {

            viewModelScope.launch {
                getClinicsStoredUseCase()
                    .catch { e ->
                        // This handles the error and updates the state
                        _mapScreenUiState.update { currentState ->
                            currentState.copy(
                                isLoadingClinics = false,
                                clinicsLoadSuccess = false,
                                clinicsLoadError = e.message ?: "Unknown error",
                            )
                        }
                    }.collect { allClinicsInRoom ->
                        _mapScreenUiState.update { currentState ->
                            currentState.copy(
                                clinics = allClinicsInRoom,
                                isLoadingClinics = false,
                                clinicsLoadSuccess = true,
                            )
                        }
                    }
            }
        }

        override fun onCleared() {
            super.onCleared()
            mapController.destroy()
        }

        fun onLocationPermissionGranted() {
            viewModelScope.launch {
                observerLocationUseCase().collect { location ->
                    _mapScreenUiState.update { currentState ->
                        val nearby =
                            currentState.clinics.getNearestClinics(
                                userLat = location.latitude,
                                userLng = location.longitude,
                                count = 15,
                            )

                        currentState.copy(
                            location = location,
                            showMap = true,
                            permissionGranted = true,
                            isLoadingPermission = false,
                            clinicsNear = nearby,
                            clinics =
                                currentState.clinics.getNearestClinics(
                                    userLat = location.latitude,
                                    userLng = location.longitude,
                                    count = 60,
                                ),
                        )
                    }
                }
            }
        }

        fun onCallTriggered() {
            _mapScreenUiState.value.selectedClinic?.phone?.takeIf { it.isNotEmpty() }?.let { phoneNumber ->

                val intent =
                    Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:${Uri.encode(phoneNumber)}".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                context.startActivity(intent)
            }
        }

        fun onPermissionCheckComplete(granted: Boolean) {
            _mapScreenUiState.update {
                it.copy(
                    permissionGranted = granted,
                    isLoadingPermission = false,
                )
            }
        }

        fun onHideCardSheet() {
            val style = mapController.style ?: return
            style.removeLayerById("basic-polyline")
            style.removeSourceById("basic-polyline-source")
            _mapScreenUiState.update { currentState ->
                currentState.copy(selectedClinic = null)
            }
        }

        fun onCreateRouteClick(hexColor: String) {
            viewModelScope.launch {
                val userLocation =
                    LatLng(
                        _mapScreenUiState.value.location!!.latitude,
                        _mapScreenUiState.value.location!!.longitude,
                    )
                val destination =
                    LatLng(
                        _mapScreenUiState.value.selectedClinic!!.lat,
                        _mapScreenUiState.value.selectedClinic!!.lng,
                    )

                val response: RouteResponse =
                    getRouteUseCase.invoke(
                        origin = userLocation,
                        destination = destination,
                    )
                val routeDistance = response.paths.firstOrNull()?.distance
                val path = response.paths.firstOrNull()
                val timeInMills = path?.time ?: 0L
                val min = (timeInMills / 60000).toInt()
                val fTime = if (min < 60) "$min min" else "${min / 60}h ${min % 60}min"

                _mapScreenUiState.update { currentState ->
                    currentState.copy(
                        routeDistance = routeDistance,
                        routeTime = fTime,
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
                val style =
                    mapController.style?.let { style ->

                        style.removeLayerById("basic-polyline")
                        style.removeSourceById("basic-polyline-source")

                        val helper: MTPolylineLayerHelper = style.polylineHelper()
                        val opts =
                            MTPolylineLayerOptions(
                                data = lineGeoJson,
                                layerId = "basic-polyline",
                                sourceId = "basic-polyline-source",
                                lineColor = MTStringOrZoomStringValues.StringValue(hexColor),
                                lineWidth = MTNumberOrZoomNumberValues.Number(4.0),
                                lineOpacity = MTNumberOrZoomNumberValues.Number(0.9),
                                lineDashArray = MTDashArrayOption.Numbers(listOf(2.0, 1.0)),
                            )
                        helper.addPolyline(opts)
                    }
            }
        }

        fun setupClusters() {
            viewModelScope.launch {
                if (_mapScreenUiState.value.clinics.isNotEmpty()) {
                    val features =
                        _mapScreenUiState.value.clinics
                            .getNearestClinics(
                                userLat = _mapScreenUiState.value.location!!.latitude,
                                userLng = _mapScreenUiState.value.location!!.longitude,
                                count = 12,
                            ).map { clinic ->
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
                            }

                    val featureCollection =
                        JsonObject().apply {
                            addProperty("type", "FeatureCollection")
                            add("features", JsonArray().also { arr -> features.forEach { arr.add(it) } })
                        }
                    val style = mapController.style ?: return@launch
                    style.addSource(
                        MTGeoJSONSource(
                            identifier = "clinics",
                            jsonString = featureCollection.toString(),
                        ),
                    )
                } else {
                    _mapScreenUiState.update { currentState ->
                        currentState.copy(clinicsLoadError = "fail on clinics fetch ")
                    }
                }
            }
        }

        fun onSearchBarInputChange(newValue: String) {
            _mapScreenUiState.update { currentState ->
                currentState.copy(searchBarText = newValue)
            }
        }

        fun onClinicSelectedChange(newClinic: Clinic) {
            _mapScreenUiState.update { currentState ->
                currentState.copy(selectedClinic = newClinic, routeDistance = null, routeTime = null)
            }
        }

        fun centerCameraOn(
            target: LngLat,
            zoom: Double = 15.0,
        ) {
            mapController.easeTo(
                cameraOptions =
                    MTCameraOptions(
                        zoom = zoom,
                        center =
                            LngLat(
                                lng = target.lng,
                                lat = target.lat,
                            ),
                    ),
            )
        }

        data class LocationUiSate(
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
        ) {
            val filteredClinics: List<Clinic>
                get() =
                    clinics.filter {

                        it.name.contains(searchBarText, ignoreCase = true) &&
                            searchBarText.isNotEmpty()
                    }
        }
    }
