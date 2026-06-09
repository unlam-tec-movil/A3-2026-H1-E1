package ar.edu.unlam.mobile.scaffolding.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.ui.components.PulseRingUserLocationMarker
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.Dev3PlayGroundViewModel
import com.maptiler.maptilersdk.annotations.MTCustomAnnotationView
import com.maptiler.maptilersdk.events.MTEvent
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.style.MTMapStyleVariant
import com.maptiler.maptilersdk.map.types.MTData
import com.maptiler.maptilersdk.map.types.MTMapCorner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dev3PlayGround(vm: Dev3PlayGroundViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val uiState by vm.locationUiState.collectAsStateWithLifecycle()

    val controller = remember { MTMapViewController(context = context) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { permissionGranted: Boolean ->
            if (permissionGranted) {
                vm.onLocationPermissionGranted()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            vm.onLocationPermissionGranted()
        } else {
            vm.onPermissionCheckComplete(granted = false)
        }
    }

    LaunchedEffect(controller) {
        controller.delegate =
            object : MTMapViewDelegate {
                override fun onMapViewInitialized() {
                    vm.setupClusters(controller.style!!)
                }

                override fun onEventTriggered(
                    event: MTEvent,
                    data: MTData?,
                ) {
                }
            }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(text = "PlayGround dev3") }) }) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DisposableEffect(controller) { onDispose { controller.delegate = null } }

            when {
                uiState.isLoadingClinics || uiState.isLoadingPermission -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                !uiState.clinicsLoadSuccess && uiState.clinicsLoadError != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error loading clinics: ${uiState.clinicsLoadError}")
                    }
                }

                uiState.permissionGranted && uiState.showMap && uiState.location != null -> {
                    Box(Modifier.weight(1f)) {
                        MTMapView(
                            referenceStyle = MTMapReferenceStyle.OPENSTREETMAP,
                            options =
                                MTMapOptions(
                                    zoom = 16.0,
                                    center =
                                        LngLat(
                                            lng = uiState.location!!.longitude,
                                            lat = uiState.location!!.latitude,
                                        ),
                                    highFrequencyEventThrottleMs = 16,
                                    logoPosition = MTMapCorner.BOTTOM_RIGHT,
                                    navigationControlIsVisible = true,
                                ),
                            controller = controller,
                            modifier = Modifier.fillMaxSize(),
                            styleVariant = MTMapStyleVariant.LIGHT,
                        )

                        MTCustomAnnotationView(
                            controller = controller,
                            coordinates =
                                LngLat(
                                    lng = uiState.location!!.longitude,
                                    lat = uiState.location!!.latitude,
                                ),
                            modifier = Modifier,
                        ) {
                            PulseRingUserLocationMarker(
                                iconRes = R.drawable.gambapp_logo_opt3_round,
                                ringColor = Color(0xFF2196F3).copy(alpha = 0.5f),
                            )
                        }

                        uiState.clinics.forEach { clinic ->

                            MTCustomAnnotationView(
                                controller = controller,
                                coordinates =
                                    LngLat(
                                        lng = clinic.lng,
                                        lat = clinic.lat,
                                    ),
                                modifier = Modifier,
                            ) {
                                PulseRingUserLocationMarker()
                            }
                        }
                    }
                }

                else -> {
                    Button(onClick = {
                        when {
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) -> {
                                Toast
                                    .makeText(
                                        context,
                                        "Location permission is required to see clinics on the map",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                            else -> {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    }) {
                        Text(text = "Ask for permission")
                    }
                }
            }
        }
    }
}
