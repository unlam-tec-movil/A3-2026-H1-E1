package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.edu.unlam.mobile.scaffolding.ui.components.BottomBar
import ar.edu.unlam.mobile.scaffolding.ui.components.SnackbarVisualsWithError
import ar.edu.unlam.mobile.scaffolding.ui.navigation.Screen

// Rutas donde el bottom bar y el floatingActionButton no deben aparecer
private val routesWithoutChrome =
    setOf(
        Screen.Splash.route,
        Screen.Onboarding.route,
    )

@Composable
fun MainScreen() {
    val controller = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val currentBackStack by controller.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showChrome = currentRoute !in routesWithoutChrome

    Scaffold(
        bottomBar = { if (showChrome) BottomBar(controller = controller) },
        floatingActionButton = {
            if (showChrome) {
                IconButton(onClick = { controller.navigate("home") }) {
                    Icon(Icons.Filled.Home, contentDescription = "Home")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                val isError = (data.visuals as? SnackbarVisualsWithError)?.isError ?: false
                val buttonColor =
                    if (isError) {
                        ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.inversePrimary,
                        )
                    }

                Snackbar(
                    modifier =
                        Modifier.border(2.dp, MaterialTheme.colorScheme.secondary).padding(12.dp),
                    action = {
                        TextButton(
                            onClick = { if (isError) data.dismiss() else data.performAction() },
                            colors = buttonColor,
                        ) {
                            Text(data.visuals.actionLabel ?: "")
                        }
                    },
                ) {
                    Text(data.visuals.message)
                }
            }
        },
    ) { paddingValue ->
        NavHost(navController = controller, startDestination = Screen.Splash.route) {
            // Splash
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        controller.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        controller.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }
            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToLogin = {
                        controller.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
            // Login todo
            composable(Screen.Login.route) {
                HomeScreen(modifier = Modifier.padding(paddingValue))
            }
            // Register todo
            composable(Screen.Register.route) {
                HomeScreen(modifier = Modifier.padding(paddingValue))
            }

            composable("home") {
                HomeScreen(modifier = Modifier.padding(paddingValue))
            }
            composable("form") {
                FormScreen(
                    modifier = Modifier.padding(paddingValue),
                    snackbarHostState = snackBarHostState,
                )
            }
            composable("Dev3PlayGround") {
                Dev3PlayGround()
            }
            composable(
                route = "user/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getString("id") ?: "1"
                UserScreen(userId = id, modifier = Modifier.padding(paddingValue))
            }
        }
    }
}
