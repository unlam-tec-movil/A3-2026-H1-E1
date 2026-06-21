package ar.edu.unlam.mobile.scaffolding.data.network.apiRouting

import ar.edu.unlam.mobile.scaffolding.BuildConfig
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.routing.BuildConfigRoutingApiKeyProviderImpl
import junit.framework.TestCase.assertEquals
import org.junit.Test

class BuildConfigRoutingApiKeyProviderImplTest {
    private val provider = BuildConfigRoutingApiKeyProviderImpl()

    @Test
    fun `the provider must return the api key from wherever the api is stored`() {
        val result = provider.getRoutingApiKey()

        assertEquals(BuildConfig.GRAPH_HOPPER_API_KEY, result)
    }
}
