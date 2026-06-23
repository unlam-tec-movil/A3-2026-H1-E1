package ar.edu.unlam.mobile.scaffolding.application.service.local.remote.routing

import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng

interface GetRouteUseCase {
    suspend operator fun invoke(
        origin: LatLng,
        destination: LatLng,
    ): RouteResponse
}
