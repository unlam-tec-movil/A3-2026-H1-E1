package ar.edu.unlam.mobile.scaffolding.ui.components

import android.R.id.progress
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ar.edu.unlam.mobile.scaffolding.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnimation(
    modifier: Modifier,
    resId: Int = R.raw.map_screen_loader,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId = resId))

    val progress by animateLottieCompositionAsState(
        isPlaying = true,
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.78f,
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )
    }
}
