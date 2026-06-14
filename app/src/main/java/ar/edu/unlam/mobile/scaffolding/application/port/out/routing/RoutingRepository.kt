package ar.edu.unlam.mobile.scaffolding.application.port.out.routing

import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng

interface RoutingRepository {
    suspend fun getRoute(
        origin: LatLng,
        destination: LatLng,
    ): RouteResponse
}
