package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
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

        fun onCreateRouteClick(style: MTStyle) {
            viewModelScope.launch {
                val lineGeoJson =
                    """
                                { "type":"FeatureCollection","features":[
                                  { "type":"Feature",
                                    "properties": { "name":"Sample Line" },
                                    "geometry":{
                                      "type":"LineString",
                    "coordinates": [
                      [
                        -58.546716,
                        -34.641723
                      ],
                      [
                        -58.547347,
                        -34.641253
                      ],
                      [
                        -58.551644,
                        -34.638118
                      ],
                      [
                        -58.551832,
                        -34.637974
                      ],
                      [
                        -58.551631,
                        -34.638206
                      ],
                      [
                        -58.55141,
                        -34.638367
                      ],
                      [
                        -58.551497,
                        -34.638388
                      ],
                      [
                        -58.551564,
                        -34.638393
                      ],
                      [
                        -58.553995,
                        -34.638332
                      ],
                      [
                        -58.554032,
                        -34.639461
                      ],
                      [
                        -58.553236,
                        -34.63948
                      ],
                      [
                        -58.553244,
                        -34.639907
                      ],
                      [
                        -58.55784,
                        -34.639843
                      ],
                      [
                        -58.55825,
                        -34.639872
                      ],
                      [
                        -58.558715,
                        -34.639893
                      ],
                      [
                        -58.55849,
                        -34.640892
                      ],
                      [
                        -58.558396,
                        -34.641016
                      ],
                      [
                        -58.557967,
                        -34.64166
                      ],
                      [
                        -58.55756,
                        -34.642218
                      ],
                      [
                        -58.557498,
                        -34.642293
                      ],
                      [
                        -58.557439,
                        -34.642248
                      ]
                    ]}
                                  }
                                ]}
                    """.trimIndent()

                // Clean up any previous run
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
                        // Use either numeric or string dash patterns; here numeric values
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
            viewModelScope.launch {
                style?.let {
                    onCreateRouteClick(style)
                }
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
