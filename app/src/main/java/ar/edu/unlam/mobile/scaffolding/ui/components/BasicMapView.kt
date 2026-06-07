package ar.edu.unlam.mobile.scaffolding.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle

@Composable
fun BasicMapView(
    baseContext: Context,
    controller: MTMapViewController,
) {
    val controller = remember { MTMapViewController(baseContext) }

    MTMapView(
        referenceStyle = MTMapReferenceStyle.OPENSTREETMAP,
        options = MTMapOptions(zoom = 10.0),
        controller = controller,
        modifier = Modifier.fillMaxSize(),
    )
}
