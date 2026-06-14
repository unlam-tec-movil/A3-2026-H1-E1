package ar.edu.unlam.mobile.scaffolding.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.ui.components.PulseRingUserLocationMarker
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.Dev3PlayGroundViewModel
import com.maptiler.maptilersdk.annotations.MTCustomAnnotationView
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
import com.maptiler.maptilersdk.map.types.MTMapCorner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dev3PlayGround(vm: Dev3PlayGroundViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var searchBarState by remember { mutableStateOf<Boolean>(false) }
    var showBottomSheetCard by remember { mutableStateOf<Boolean>(false) }
    var showFabReposition by remember { mutableStateOf<Boolean>(true) }

    val uiState by vm.mapScreenUiState.collectAsStateWithLifecycle()

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

    Scaffold(floatingActionButton = {
        if (showFabReposition) {
            FloatingActionButton(
                onClick = {
                    uiState.location?.let { location ->
                        controller.easeTo(
                            cameraOptions =
                                MTCameraOptions(
                                    center =
                                        LngLat(
                                            lng = location.longitude,
                                            lat = location.latitude,
                                        ),
                                ),
                        )
                    }
                },
                modifier = Modifier.padding(bottom = 75.dp),
                shape = RoundedCornerShape(30.dp),
                content = {
                    Icon(
                        imageVector = Icons.Filled.CenterFocusStrong,
                        modifier = Modifier,
                        contentDescription = "",
                    )
                },
            )
        }
    }, floatingActionButtonPosition = FabPosition.End) { paddingValues ->
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
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    ) {
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
                                ),
                            controller = controller,
                            modifier = Modifier.fillMaxSize(),
                            styleVariant = MTMapStyleVariant.DARK,
                        )
                        DockedSearchBar(
                            shape = RoundedCornerShape(8.dp),
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp),
                            onExpandedChange = { searchBarState = it },
                            expanded = searchBarState,
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = uiState.searchBarText,
                                    onQueryChange = { newValue -> vm.onSearchBarInputChange(newValue = newValue) },
                                    onSearch = { searchBarState = !searchBarState },
                                    expanded = searchBarState,
                                    placeholder = { Text("Search clinics...") },
                                    onExpandedChange = { searchBarState = !searchBarState },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
                                    trailingIcon = {
                                        if (uiState.searchBarText.isNotEmpty()) {
                                            IconButton(onClick = { vm.onSearchBarInputChange("") }) {
                                                Icon(Icons.Default.Close, contentDescription = null)
                                            }
                                        }
                                    },
                                )
                            },
                        ) {
                            if (uiState.filteredClinics.isNotEmpty()) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                ) {
                                    uiState.filteredClinics.forEach { clinic ->
                                        ListItem(
                                            headlineContent = { Text(clinic.name) },
                                            supportingContent = { Text(clinic.phone) },
                                            leadingContent = { Icon(Icons.Default.Healing, null) },
                                            modifier =
                                                Modifier.clickable {
                                                    controller.easeTo(
                                                        cameraOptions =
                                                            MTCameraOptions(
                                                                center = LngLat(lng = clinic.lng, lat = clinic.lat),
                                                                zoom = 14.0,
                                                            ),
                                                    )
                                                    searchBarState = !searchBarState
                                                },
                                        )
                                    }
                                }
                            }
                        }

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
                                onRingClicked = {},
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
                                PulseRingUserLocationMarker(onRingClicked = {
                                    showBottomSheetCard = true
                                    showFabReposition = false
                                    vm.onClinicSelectedChange(clinic, controller.style)
                                })
                            }
                        }
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AnimatedVisibility(
                                visible = showBottomSheetCard,
                                enter = scaleIn() + expandVertically(),
                                exit = scaleOut() + shrinkVertically(),
                            ) {
                                uiState.selectedClinic?.let { clinicSelected ->
                                    BottomSheetCard(
                                        modifier = Modifier,
                                        clinic = clinicSelected,
                                        onCloseClick = {
                                            showBottomSheetCard = false
                                            showFabReposition = true
                                        },
                                    )
                                }
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

@Composable
fun BottomSheetCard(
    clinic: Clinic,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
) {
    Card(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 90.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = clinic.name, style = MaterialTheme.typography.titleLarge)
            Text(text = clinic.address, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { onCloseClick() }) { Text(text = "Hide card") }
        }
    }
}
