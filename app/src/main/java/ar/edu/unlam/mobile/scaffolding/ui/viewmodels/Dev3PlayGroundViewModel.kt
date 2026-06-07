package ar.edu.unlam.mobile.scaffolding.ui.viewmodels

import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.ObserverLocationUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.maptiler.maptilersdk.map.style.MTStyle
import com.maptiler.maptilersdk.map.style.layer.circle.MTCircleLayer
import com.maptiler.maptilersdk.map.style.layer.circle.colorConst
import com.maptiler.maptilersdk.map.style.layer.circle.radiusConst
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
        private val _locationUiState = MutableStateFlow(LocationUiSate())
        val locationUiState = _locationUiState.asStateFlow()

        init {
            viewModelScope.launch {
                try {
                    // Only populate if database is empty
                    getClinicsStoredUseCase().collect { clinics ->
                        if (clinics.isEmpty()) {
                            val clinicsFromAssets = getClinicsFromAssetsUseCase()
                            populateClinicsDbUseCase(clinicsFromAssets)
                        }
                        _locationUiState.update { it.copy(isLoadingClinics = false, clinicsLoadSuccess = true) }
                    }
                } catch (e: Exception) {
                    _locationUiState.update {
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
                    _locationUiState.update { currentState ->
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
            _locationUiState.update {
                it.copy(
                    permissionGranted = granted,
                    isLoadingPermission = false,
                )
            }
        }

        fun setupClusters(style: MTStyle) {
            viewModelScope.launch {
                getClinicsStoredUseCase().collect { clinics ->
                    if (clinics.isNotEmpty()) {
                        val features = clinics.map { clinic ->
                            JsonObject().apply {
                                addProperty("type", "Feature")
                                addProperty("id", clinic.id)
                                
                                val properties = JsonObject().apply {
                                    addProperty("name", clinic.name)
                                    addProperty("address", clinic.address)
                                    addProperty("phone", clinic.phone)
                                    addProperty("website", clinic.website)
                                }
                                add("properties", properties)
                                
                                val geometry = JsonObject().apply {
                                    addProperty("type", "Point")
                                    val coordinates = JsonArray().apply {
                                        add(clinic.lng)
                                        add(clinic.lat)
                                    }
                                    add("coordinates", coordinates)
                                }
                                add("geometry", geometry)
                            }
                        }
                        
                        val featureCollection = JsonObject().apply {
                            addProperty("type", "FeatureCollection")
                            val featuresArray = JsonArray()
                            features.forEach { featuresArray.add(it) }
                            add("features", featuresArray)
                        }
                        
                        val src = MTGeoJSONSource(
                            identifier = "clinics",
                            jsonString = featureCollection.toString()
                        )
                        style.addSource(src)
                        
                        val unclustered = MTCircleLayer(
                            identifier = "clinicPoints", 
                            sourceIdentifier = "clinics"
                        ).apply {
                            colorConst(android.graphics.Color.BLUE)
                            radiusConst(8.0)
                        }
                        style.addLayer(unclustered)
                    }
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
        )
    }
