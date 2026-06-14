package ar.edu.unlam.mobile.scaffolding.application.service.routing

import ar.edu.unlam.mobile.scaffolding.application.port.inn.routing.GetRouteUseCase
import ar.edu.unlam.mobile.scaffolding.application.port.out.routing.RoutingRepository
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetRouteInteractor
    @Inject
    constructor(
        private val routRepo: RoutingRepository,
    ) : GetRouteUseCase {
        override suspend fun invoke(
            origin: LatLng,
            destination: LatLng,
        ): RouteResponse =
            routRepo.getRoute(
                origin = origin,
                destination = destination,
            )
    }
