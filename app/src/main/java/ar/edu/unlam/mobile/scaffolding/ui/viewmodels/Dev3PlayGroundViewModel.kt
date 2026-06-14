package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.port.inn.routing.GetRouteUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.maptiler.maptilersdk.helpers.MTDashArrayOption
import com.maptiler.maptilersdk.helpers.MTNumberOrZoomNumberValues
import com.maptiler.maptilersdk.helpers.MTPolylineLayerHelper
import com.maptiler.maptilersdk.helpers.MTPolylineLayerOptions
import com.maptiler.maptilersdk.helpers.MTStringOrZoomStringValues
import com.maptiler.maptilersdk.map.style.MTStyle
import com.maptiler.maptilersdk.map.style.source.MTGeoJSONSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Dev3PlayGroundViewModel
    @Inject
    constructor(
        private val observerLocationUseCase: ObserverLocationUseCase,
        private val getClinicsFromAssetsUseCase: GetClinicsFromAssetsUseCase,
        private val populateClinicsDbUseCase: PopulateClinicsDbUseCase,
        private val getClinicsStoredUseCase: GetClinicsStoredUseCase,
        private val getRouteUseCase: GetRouteUseCase,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _mapScreenUiState = MutableStateFlow(LocationUiSate())
        val mapScreenUiState = _mapScreenUiState.asStateFlow()

        init {
            viewModelScope.launch {
                try {
                    getClinicsStoredUseCase().collect { clinics ->
                        if (clinics.isEmpty()) {
                            val clinicsFromAssets = getClinicsFromAssetsUseCase()
                            populateClinicsDbUseCase(clinicsFromAssets)
                        }
                        _mapScreenUiState.update {
                            it.copy(
                                isLoadingClinics = false,
                                clinicsLoadSuccess = true,
                                clinics = clinics,
                            )
                        }
                    }
                } catch (e: Exception) {
                    _mapScreenUiState.update {
                        it.copy(
                            isLoadingClinics = false,
                            clinicsLoadSuccess = false,
                            clinicsLoadError = e.message,
                        )
                    }
                }
            }
        }

        fun onLocationPermissionGranted() {
            viewModelScope.launch {
                observerLocationUseCase().collect { location ->
                    _mapScreenUiState.update { currentState ->
                        currentState.copy(
                            location = location,
                            showMap = true,
                            permissionGranted = true,
                            isLoadingPermission = false,
                        )
                    }
                }
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

        fun onCreateRouteClick(
            style: MTStyle,
            clinicSelected: Clinic,
        ) {
            viewModelScope.launch {
                val response: RouteResponse =
                    getRouteUseCase.invoke(
                        origin =
                            LatLng(
                                _mapScreenUiState.value.location!!.latitude,
                                _mapScreenUiState.value.location!!.longitude,
                            ),
                        destination =
                            LatLng(
                                clinicSelected.lat,
                                clinicSelected.lng,
                            ),
                    )
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

                style.removeLayerById("basic-polyline")
                style.removeSourceById("basic-polyline-source")

                val helper: MTPolylineLayerHelper = style.polylineHelper()
                val opts =
                    MTPolylineLayerOptions(
                        data = lineGeoJson,
                        layerId = "basic-polyline",
                        sourceId = "basic-polyline-source",
                        lineColor = MTStringOrZoomStringValues.StringValue("#E63946"),
                        lineWidth = MTNumberOrZoomNumberValues.Number(3.0),
                        lineOpacity = MTNumberOrZoomNumberValues.Number(0.9),
                        lineDashArray = MTDashArrayOption.Numbers(listOf(2.0, 1.0)),
                    )
                helper.addPolyline(opts)
            }
        }

        fun setupClusters(style: MTStyle) {
            viewModelScope.launch {
                getClinicsStoredUseCase().collect { clinics ->
                    if (clinics.isNotEmpty()) {
                        val features =
                            clinics.map { clinic ->
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

                        style.addSource(
                            MTGeoJSONSource(
                                identifier = "clinics",
                                jsonString = featureCollection.toString(),
                            ),
                        )
                    }
                }
            }
        }

        fun onSearchBarInputChange(newValue: String) {
            _mapScreenUiState.update { currentState ->
                currentState.copy(searchBarText = newValue)
            }
        }

        fun onClinicSelectedChange(
            newClinic: Clinic,
            style: MTStyle?,
        ) {
            _mapScreenUiState.update { currentState ->
                currentState.copy(selectedClinic = newClinic)
            }
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
            val selectedClinic: Clinic? = null,
            var searchBarText: String = "",
        ) {
            val filteredClinics: List<Clinic>
                get() =
                    clinics.filter {
                        it.name.contains(searchBarText, ignoreCase = true) &&
                            searchBarText.isNotEmpty()
                    }
        }
    }
