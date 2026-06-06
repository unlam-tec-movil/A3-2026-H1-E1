package ar.edu.unlam.mobile.scaffolding.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.mobile.scaffolding.ui.viewmodels.Dev3PlayGroundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dev3PlayGround(vm: Dev3PlayGroundViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val uiState by vm.locationUiState.collectAsStateWithLifecycle()

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

    Scaffold(topBar = { TopAppBar(title = { Text(text = "PlayGround dev3") }) }) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        Toast
                            .makeText(
                                context,
                                "permission already granted, hot mommies near by and approaching, you better run",
                                Toast.LENGTH_SHORT,
                            ).show()
                        vm.onLocationPermissionGranted()
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) -> {
                        Toast
                            .makeText(
                                context,
                                "really hot mommies want to know where you are :(",
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
            uiState.location?.let { location ->
                Text("Lat:${location.latitude}, Long:${location.longitude}\n ")
            } ?: Text("Waitting for location permission")
        }
    }
}
// "id": "a0571f4e-3773-4077-be8b-1d4032c81a02",
// "name": "BienKinesio",
// "category": "physical_therapy",
// "address": "Juan José Paso 289",
// "city": "Martínez",
// "region": "",
// "postcode": "B1640",
// "phone": "+541148989030",
// "website": "http://www.bienkinesio.com/",
// "lat": -34.48979723,
// "lng": -58.49666901,
// "confidence_score": 0.557,
// "email": "",
// "manager_name": "",
// "manager_title": "",
// "source": "overture",
// "imported_at": "2026-05-05T23:42:06.298423+00:00"
