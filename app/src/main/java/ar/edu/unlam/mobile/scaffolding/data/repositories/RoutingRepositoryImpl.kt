package ar.edu.unlam.mobile.scaffolding.data.repositories

import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApi
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingRepository
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteRequest
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class RoutingRepositoryImpl
    @Inject
    constructor(
        private val api: RoutingApi,
        private val routingApiKeyProvider: RoutingApiKeyProvider,
    ) : RoutingRepository {
        override suspend fun getRoute(
            origin: LatLng,
            destination: LatLng,
        ): RouteResponse {
            val request =
                RouteRequest(
                    points =
                        listOf(
                            listOf(
                                origin.longitude,
                                origin.latitude,
                            ),
                            listOf(
                                destination.longitude,
                                destination.latitude,
                            ),
                        ),
                )
            return api.getRoute(routingApiKeyProvider.getRoutingApiKey(), request)
        }
    }
