package ar.edu.unlam.mobile.scaffolding.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = ElectricIndigo,
        secondary = CyanWave,
        tertiary = MidnightDeep,
        background = DarkBg,
        surface = DarkSurface,
        outline = DarkBorder,
        outlineVariant = DarkBorder,
        onBackground = DarkTextPrimary,
        onSurface = DarkTextPrimary,
        onPrimary = LightSurface,
        onSecondary = MidnightDeep,
        onTertiary = DarkTextPrimary,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = ElectricIndigo,
        secondary = CyanWave,
        tertiary = MidnightDeep,
        background = LightBg,
        surface = LightSurface,
        outline = LightBorder,
        outlineVariant = LightBorder,
        onBackground = LightTextPrimary,
        onSurface = LightTextPrimary,
        onPrimary = LightSurface,
        onSecondary = LightSurface,
        onTertiary = LightSurface,
    )

@Composable
fun ScaffoldingV2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@Composable
fun GambAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    ScaffoldingV2Theme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content,
    )
}
