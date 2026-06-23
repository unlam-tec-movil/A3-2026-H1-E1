package ar.edu.unlam.mobile.scaffolding.data.datasources.network.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class Points(
    val type: String,
    val coordinates: List<List<Double>>,
) {
    val latLngs: List<LatLng>
        get() =
            coordinates.map { coordinates ->
                LatLng(coordinates[1], coordinates[0])
            }
}

data class Path(
    val points: Points,
    val distance: Double,
    val time: Long,
)

data class RouteResponse(
    val paths: List<Path>,
)

data class RouteRequest(
    val points: List<List<Double>>,
    val profile: String = "foot",
    @SerializedName("points_encoded")
    val pointsEncoded: Boolean = false,
    val locale: String = "es",
)
