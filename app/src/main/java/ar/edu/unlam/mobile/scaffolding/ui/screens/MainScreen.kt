package ar.edu.unlam.mobile.scaffolding.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
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
import ar.edu.unlam.mobile.scaffolding.ui.screens.dashboard.DashboardScreen
import ar.edu.unlam.mobile.scaffolding.ui.screens.rehab.RehabSessionScreen
import ar.edu.unlam.mobile.scaffolding.ui.screens.rehab.RoutineListScreen

private val routesWithoutChrome =
    setOf(
        Screen.Splash.route,
        Screen.Onboarding.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.EnvironmentCheck.route,
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
                        Modifier
                            .border(2.dp, MaterialTheme.colorScheme.secondary)
                            .padding(12.dp),
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
        NavHost(
            navController = controller,
            startDestination = Screen.Splash.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(400))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(400))
            },
        ) {
            // Splash
            composable(
                route = Screen.Splash.route,
                exitTransition = { fadeOut(animationSpec = tween(400)) },
            ) {
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
            composable(
                route = Screen.Onboarding.route,
                enterTransition = { fadeIn(animationSpec = tween(400)) },
                exitTransition = { fadeOut(animationSpec = tween(400)) },
            ) {
                OnboardingScreen(
                    onNavigateToLogin = {
                        controller.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }

            // Login
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToDashboard = {
                        controller.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        controller.navigate(Screen.Register.route)
                    },
                )
            }

            // Register
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        controller.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                )
            }

            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToRoutineList = { controller.navigate(Screen.RoutineList.route) },
                    modifier = Modifier.padding(paddingValue),
                )
            }

            // Routine List
            composable(Screen.RoutineList.route) {
                RoutineListScreen(
                    controller = controller,
                    modifier = Modifier.padding(paddingValue),
                )
            }

            // Environment Check
            composable(Screen.EnvironmentCheck.route) {
                EnvironmentCheckScreen(
                    onNavigateToRehab = {
                        controller.navigate(Screen.RehabSession.route) {
                            popUpTo(Screen.EnvironmentCheck.route) { inclusive = true }
                        }
                    },
                )
            }

            // Rehab Session
            composable(Screen.RehabSession.route) {
                RehabSessionScreen(modifier = Modifier.padding(paddingValue))
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = {
                        controller.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(paddingValue),
                )
            }

            // Form
            composable(Screen.Form.route) {
                FormScreen(
                    modifier = Modifier.padding(paddingValue),
                    snackbarHostState = snackBarHostState,
                )
            }

            // Playground
            composable(Screen.Dev3PlayGround.route) {
                Dev3PlayGround()
            }

            // User
            composable(
                route = Screen.User.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getString("id") ?: "1"
                UserScreen(userId = id, modifier = Modifier.padding(paddingValue))
            }
        }
    }
}
