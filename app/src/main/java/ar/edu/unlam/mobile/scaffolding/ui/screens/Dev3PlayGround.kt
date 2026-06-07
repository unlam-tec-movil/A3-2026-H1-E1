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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.Dev3PlayGroundViewModel
import com.maptiler.maptilersdk.events.MTEvent
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.MTMapViewDelegate
import com.maptiler.maptilersdk.map.options.MTCameraOptions
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.style.MTMapStyleVariant
import com.maptiler.maptilersdk.map.types.MTData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dev3PlayGround(vm: Dev3PlayGroundViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val uiState by vm.locationUiState.collectAsStateWithLifecycle()

    var isMapInitialized by remember { mutableStateOf(false) }

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
                    isMapInitialized = true
                    vm.setupClusters(controller.style!!)
                }

                override fun onEventTriggered(
                    event: MTEvent,
                    data: MTData?,
                ) {
                    // no-op
                }
            }
    }

    LaunchedEffect(uiState.location, isMapInitialized) {
        if (isMapInitialized) {
            uiState.location?.let { loc ->
                val target = LngLat(lng = loc.longitude, lat = loc.latitude)
                controller.easeTo(cameraOptions = MTCameraOptions(target, zoom = 12.0))
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

            // Show loading while clinics are being loaded
            if (uiState.isLoadingClinics) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (!uiState.clinicsLoadSuccess && uiState.clinicsLoadError != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Error loading clinics: ${uiState.clinicsLoadError}")
                }
            } else if (uiState.isLoadingPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.permissionGranted && uiState.showMap && uiState.location != null) {
                Box(Modifier.weight(1f)) {
                    MTMapView(
                        referenceStyle = MTMapReferenceStyle.OPENSTREETMAP,
                        options =
                            MTMapOptions(
                                zoom = 12.0,
                                center = LngLat(lng = uiState.location!!.longitude, lat = uiState.location!!.latitude),
                            ),
                        controller = controller,
                        modifier = Modifier.fillMaxSize(),
                        styleVariant = MTMapStyleVariant.DARK,
                    )
                }
            } else {
                Button(onClick = {
                    when {
                        // ask for permission if before/from settings was denied
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

                        // ask for the first time
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
