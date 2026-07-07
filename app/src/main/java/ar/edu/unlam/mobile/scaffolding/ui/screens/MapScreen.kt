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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.R
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.toHex
import ar.edu.unlam.mobile.scaffolding.ui.components.BottomSheetCard
import ar.edu.unlam.mobile.scaffolding.ui.components.FABShortCut
import ar.edu.unlam.mobile.scaffolding.ui.components.LottieAnimation
import ar.edu.unlam.mobile.scaffolding.ui.components.PulseRingUserLocationMarker
import ar.edu.unlam.mobile.scaffolding.ui.components.dashboard.LocationDeniedCard
import ar.edu.unlam.mobile.scaffolding.ui.theme.CoralDanger
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.MapScreenViewModel
import com.maptiler.maptilersdk.annotations.MTCustomAnnotationView
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.options.MTFitBoundsOptions
import com.maptiler.maptilersdk.map.options.MTPaddingOptions
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.style.MTMapStyleVariant
import com.maptiler.maptilersdk.map.types.MTBounds
import com.maptiler.maptilersdk.map.types.MTMapCorner
import kotlin.math.roundToInt

enum class HorizontalDragState { LEFT, RIGHT, CENTER }

enum class VerticalDragState { COLLAPSED, EXPANDED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    vm: MapScreenViewModel = hiltViewModel(),
    onLoadedStateChange: (Boolean) -> Unit,
) {
    // region State & setup
    val context = LocalContext.current
    val uiState by vm.mapScreenUiState.collectAsStateWithLifecycle()
    var searchBarState by remember { mutableStateOf<Boolean>(false) }
    var showBottomSheetCard by remember { mutableStateOf<Boolean>(false) }
    var showFabReposition by remember { mutableStateOf<Boolean>(false) }
    var allowGoToConfigFeature by remember { mutableStateOf(true) }

    val controller = vm.mapController
    val routeColor = MaterialTheme.colorScheme.primary.toHex()

    // Location permission
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { permissionGranted: Boolean ->
            if (permissionGranted) {
                vm.onLocationPermissionGranted()
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    // Drag animation anchors for the bottom sheet card
    val density = LocalDensity.current
    val verticalAnchorDistance = with(density) { 75.dp.toPx() }
    val horizontalAnchorDistance = with(density) { 75.dp.toPx() }
    val horizontalDragState =
        remember {
            AnchoredDraggableState(
                initialValue = HorizontalDragState.CENTER,
                anchors =
                    DraggableAnchors {
                        HorizontalDragState.LEFT at -horizontalAnchorDistance
                        HorizontalDragState.CENTER at 0f
                        HorizontalDragState.RIGHT at horizontalAnchorDistance
                    },
            )
        }
    val verticalDragState =
        remember {
            AnchoredDraggableState(
                initialValue = VerticalDragState.EXPANDED,
                anchors =
                    DraggableAnchors {
                        VerticalDragState.EXPANDED at 10f
                        VerticalDragState.COLLAPSED at verticalAnchorDistance
                    },
            )
        }

    val isLoading =
        uiState.isLoadingClinics ||
            uiState.isLoadingPermission ||
            (uiState.permissionGranted && !uiState.mapInitialized)

    LaunchedEffect(isLoading) {
        onLoadedStateChange(isLoading)
        showFabReposition = !isLoading
    }

    var clinicToDisplay by remember { mutableStateOf<Clinic?>(null) }
    LaunchedEffect(uiState.selectedClinic) {
        if (uiState.selectedClinic != null) {
            clinicToDisplay = uiState.selectedClinic
        }
    }

    LaunchedEffect(horizontalDragState.settledValue) {
        when (horizontalDragState.settledValue) {
            HorizontalDragState.LEFT -> {
                horizontalDragState.animateTo(HorizontalDragState.CENTER)
                uiState.location?.let { location ->
                    vm.centerCameraOn(LngLat(lng = location.longitude, lat = location.latitude))
                }
                showBottomSheetCard = false
                showFabReposition = true
                vm.onHideCardSheetAndRemoveRouteLayer()
            }

            HorizontalDragState.RIGHT -> {
                vm.onCallTriggered()
                horizontalDragState.animateTo(HorizontalDragState.CENTER)
            }

            HorizontalDragState.CENTER -> {}
        }
    }
    LaunchedEffect(verticalDragState.settledValue) {
        if (verticalDragState.settledValue == VerticalDragState.COLLAPSED) {
            verticalDragState.animateTo(VerticalDragState.EXPANDED)
            if (allowGoToConfigFeature) {
                vm.onGoToConfigClick()
                allowGoToConfigFeature = false
            }
        }
    }
    LaunchedEffect(uiState.location) {
        if (uiState.showRoute && uiState.location != null && uiState.selectedClinic != null) {
            vm.onCreateRouteClick(routeColor)
        }
    }

    LaunchedEffect(uiState.mapInitialized, uiState.clinicsNear) {
        if (uiState.mapInitialized && uiState.clinicsNear.isNotEmpty()) {
            if (uiState.selectedClinic == null) {
                vm.onClinicSelectedChange(uiState.clinicsNear.first())
            }

            uiState.location?.let { loc ->
                vm.centerCameraOn(LngLat(lng = loc.longitude, lat = loc.latitude))
            }
        }
    }
    LaunchedEffect(uiState.routeError) {
        uiState.routeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    // Check permission each time the app came back from for example settings

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
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
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
    // end region

    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(
                visible =
                    (uiState.lastSavedClinicId != null || uiState.selectedClinic != null) && !showBottomSheetCard,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 90.dp),
                ) {
                    if (showFabReposition) {
                        FABShortCut(
                            onClick = {
                                showBottomSheetCard = true
                                showFabReposition = false
                                vm.onRestoreLastRouteClick(routeColor)
                            },
                            icon = Icons.Default.NearMe,
                        )
                        FABShortCut(
                            modifier = Modifier,
                            onClick = {
                                uiState.location?.let { location ->
                                    vm.centerCameraOn(LngLat(lng = location.longitude, lat = location.latitude))
                                }
                            },
                            icon = Icons.Default.MyLocation,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->

        // region Map content
        if (uiState.permissionGranted && uiState.showMap && uiState.location != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                MTMapView(
                    referenceStyle = MTMapReferenceStyle.BASIC,
                    options =
                        MTMapOptions(
                            zoom = 15.0,
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
                    styleVariant = MTMapStyleVariant.LIGHT,
                )

                DockedSearchBar(
                    shape = RoundedCornerShape(8.dp),
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 65.dp)
                            .border(
                                shape = RoundedCornerShape(8.dp),
                                border =
                                    BorderStroke(
                                        width = 1.dp,
                                        brush =
                                            Brush.linearGradient(
                                                colors =
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                    ),
                                            ),
                                    ),
                            ),
                    onExpandedChange = { searchBarState = it },
                    expanded = searchBarState,
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = uiState.searchBarText,
                            onQueryChange = { vm.onSearchBarInputChange(newValue = it) },
                            onSearch = { searchBarState = false },
                            expanded = searchBarState,
                            placeholder = { Text("Buscar clínicas...") },
                            onExpandedChange = { searchBarState = it },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (uiState.searchBarText.isNotEmpty()) {
                                    IconButton(onClick = { vm.onSearchBarInputChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                                    }
                                }
                            },
                        )
                    },
                ) {
                    when {
                        uiState.filteredClinics.isNotEmpty() -> {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = "Se encontraron ${uiState.filteredClinics.size} clínica(s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                                HorizontalDivider()
                                uiState.filteredClinics.forEach { clinic ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = clinic.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        },
                                        supportingContent = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Phone,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    text = clinic.phone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Outlined.LocalHospital,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier =
                                                    Modifier
                                                        .size(36.dp)
                                                        .background(
                                                            color = MaterialTheme.colorScheme.primaryContainer,
                                                            shape = CircleShape,
                                                        ).padding(8.dp),
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        },
                                        modifier =
                                            Modifier.clickable {
                                                vm.onClinicSelectedChange(clinic)
                                                showBottomSheetCard = true
                                                showFabReposition = false
                                                vm.centerCameraOn(LngLat(lng = clinic.lng, lat = clinic.lat))
                                                searchBarState = false
                                            },
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }

                        uiState.searchBarText.isNotEmpty() -> {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "No hay clínicas que coincidan con \"${uiState.searchBarText}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // User location marker
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
                        ringColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        onRingClicked = {},
                    )
                }

                // Clinic markers — include selected clinic even if outside the near list
                val markersToShow =

                    remember(uiState.clinicsNear, uiState.selectedClinic) {
                        val list = uiState.clinicsNear.toMutableList()
                        uiState.selectedClinic?.let { selected ->
                            if (list.none { it.id == selected.id }) list.add(selected)
                        }
                        list
                    }

                markersToShow.forEach { clinic ->
                    MTCustomAnnotationView(
                        controller = controller,
                        coordinates = LngLat(lng = clinic.lng, lat = clinic.lat),
                        modifier = Modifier,
                    ) {
                        PulseRingUserLocationMarker(
                            ringColor =
                                if (clinic.id == uiState.selectedClinic?.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                } else {
                                    CoralDanger.copy(alpha = 0.8f)
                                },
                            onRingClicked = {
                                showBottomSheetCard = true
                                showFabReposition = false
                                vm.onClinicSelectedChange(clinic)
                            },
                        )
                    }
                }

                // Bottom sheet card with swipe-to-dismiss / swipe-to-call
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = showBottomSheetCard,
                        enter = scaleIn() + expandVertically(),
                        exit = scaleOut() + shrinkVertically(),
                    ) {
                        clinicToDisplay?.let { clinicSelected ->
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier =
                                    Modifier.background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = MaterialTheme.shapes.large,
                                    ),
                            ) {
                                // Hint icons shown behind the draggable card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "Llamar clínica",
                                        modifier = Modifier.padding(start = 15.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = "Crear ruta",
                                        modifier = Modifier.padding(end = 18.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                                BottomSheetCard(
                                    modifier =
                                        Modifier
                                            .anchoredDraggable(
                                                state = horizontalDragState,
                                                orientation = Orientation.Horizontal,
                                            ).offset {
                                                IntOffset(
                                                    x = horizontalDragState.requireOffset().roundToInt(),
                                                    y = 0,
                                                )
                                            },
                                    clinic = clinicSelected,
                                    distance = uiState.routeDistance,
                                    estimatedArrival = uiState.routeTime,
                                    onCloseClick = {
                                        showBottomSheetCard = false
                                        showFabReposition = true
                                    },
                                    onGenerateRouteClick = {
                                        val userLat = uiState.location!!.latitude
                                        val userLng = uiState.location!!.longitude
                                        val clinicLat = clinicSelected.lat
                                        val clinicLng = clinicSelected.lng
                                        val bounds =
                                            MTBounds(
                                                west = minOf(userLng, clinicLng),
                                                south = minOf(userLat, clinicLat),
                                                east = maxOf(userLng, clinicLng),
                                                north = maxOf(userLat, clinicLat),
                                            )
                                        controller.fitBounds(
                                            bounds = bounds,
                                            options =
                                                MTFitBoundsOptions(
                                                    padding =
                                                        MTPaddingOptions(
                                                            top = 100.0,
                                                            bottom = 300.0,
                                                            left = 100.0,
                                                            right = 100.0,
                                                        ),
                                                    duration = 1000.0,
                                                ),
                                        )
                                        vm.onCreateRouteClick(hexColor = routeColor)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier =
                        Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = MaterialTheme.shapes.large,
                            ),
                ) {
                    // Hint icons shown behind the draggable card
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "ir a ajustes",
                            modifier = Modifier.padding(top = 25.dp),
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .anchoredDraggable(
                                    state = verticalDragState,
                                    orientation = Orientation.Vertical,
                                ).offset {
                                    IntOffset(
                                        y = verticalDragState.requireOffset().roundToInt(),
                                        x = 0,
                                    )
                                },
                    ) {
                        LocationDeniedCard()
                    }
                }
            }
        }

// endregion

// region Overlays (loading / error / permission)
        if (uiState.isLoadingClinics ||
            uiState.isLoadingPermission ||
            (uiState.permissionGranted && !uiState.mapInitialized)
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(Modifier.fillMaxSize(), resId = R.raw.hospital)
            }
        } else if (!uiState.clinicsLoadSuccess && uiState.clinicsLoadError != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error al cargar clínicas: ${uiState.clinicsLoadError}")
                }
            }
        } else if (!uiState.permissionGranted) {
            val activity = context as? Activity
            if (activity != null) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) -> {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    else -> {}
                }
            }
        }
// endregion
    }
}
