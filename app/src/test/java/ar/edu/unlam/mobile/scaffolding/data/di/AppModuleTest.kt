package ar.edu.unlam.mobile.scaffolding.data.di

import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.map.ApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApi
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingRepository
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Constants
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppModuleTest {
    @Test
    fun `providesRetrofit should return Retrofit instance with the correct Url `() {
        val retrofit = AppModule.providesRetrofit()

        assertNotNull(retrofit)
        assertEquals(Constants.GRAPH_HOPPER_BASE_URL, retrofit.baseUrl().toString())
    }

    @Test
    fun `providesRoutingApi should return RoutingApi instance using retrofit`() {
        val retrofitTest = AppModule.providesRetrofit()
        val routingApiTest = AppModule.providesRoutingApi(retrofitTest)

        assertNotNull(routingApiTest)
        assertTrue(routingApiTest is RoutingApi)
    }

    @Test
    fun `providesRoutingRepository should return RoutingRepository instance`() {
        val api = mockk<RoutingApi>()
        val provider = mockk<RoutingApiKeyProvider>()

        val routingRepoTest = AppModule.providesRoutingRepository(api, provider)
        assertNotNull(routingRepoTest)
        assertTrue(routingRepoTest is RoutingRepository)
    }

    @Test
    fun `providesGetRouteUsecase should return a GetRouteInteractor instance`() {
        val repo = mockk<RoutingRepository>()
        AppModule.providesGetRouteUsecase(repo)
    }

    @Test
    fun `providesRoutingApikey should return a RoutingApiKeyProvider instance`() {
        val provider = AppModule.providesRoutingApiKeyProvider()
        assertNotNull(provider)
        assertTrue(provider is RoutingApiKeyProvider)
    }

    @Test
    fun `providesApikey should return a ApiKeyProvider instance`() {
        val provider = AppModule.providesMapApiKeyProvider()
        assertNotNull(provider)
        assertTrue(provider is ApiKeyProvider)
    }
}
