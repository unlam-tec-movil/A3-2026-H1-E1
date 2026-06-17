package ar.edu.unlam.mobile.scaffolding.application.port.inn.routing

import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng

interface GetRouteUseCase {
    suspend operator fun invoke(
        origin: LatLng,
        destination: LatLng,
    ): RouteResponse
}
