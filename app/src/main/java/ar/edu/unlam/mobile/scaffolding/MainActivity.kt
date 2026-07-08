package ar.edu.unlam.mobile.scaffolding

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ar.edu.unlam.mobile.scaffolding.application.service.local.ThemeSensorManager
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences.SessionPreferences
import ar.edu.unlam.mobile.scaffolding.ui.navigation.MainScreen
import ar.edu.unlam.mobile.scaffolding.ui.theme.GambAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionPreferences: SessionPreferences

    @Inject
    lateinit var themeSensorManager: ThemeSensorManager

    override fun onStart() {
        super.onStart()
        // Theme sensor manager will be started conditionally in setContent when isDynamicThemeActive is true
    }

    override fun onStop() {
        super.onStop()
        themeSensorManager.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val isDarkMode by sessionPreferences.isDarkMode.collectAsState(initial = false)
            val isDynamicThemeActive by sessionPreferences.isDynamicThemeActive.collectAsState(initial = true)

            LaunchedEffect(isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle =
                        SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                            detectDarkMode = { isDarkMode },
                        ),
                    navigationBarStyle =
                        SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                            detectDarkMode = { isDarkMode },
                        ),
                )

                val windowInsetsController =
                    androidx.core.view.WindowCompat.getInsetsController(
                        window,
                        window.decorView,
                    )
                windowInsetsController.systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(
                    androidx.core.view.WindowInsetsCompat.Type
                        .systemBars(),
                )
            }

            LaunchedEffect(isDynamicThemeActive) {
                if (isDynamicThemeActive) {
                    themeSensorManager.startListening()
                } else {
                    themeSensorManager.stopListening()
                }
            }

            GambAppTheme(darkTheme = isDarkMode, dynamicColor = isDynamicThemeActive) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen()
                }
            }
        }
    }
}
