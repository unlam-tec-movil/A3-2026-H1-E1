package ar.edu.unlam.mobile.scaffolding.data.datasources.network.apiRouting

import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteRequest
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RoutingApi {
    @POST("route")
    suspend fun getRoute(
        @Query("key") apiKey: String,
        @Body request: RouteRequest,
    ): RouteResponse
}
