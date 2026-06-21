package ar.edu.unlam.mobile.scaffolding.data.datasources.network.routing

import ar.edu.unlam.mobile.scaffolding.BuildConfig
import ar.edu.unlam.mobile.scaffolding.application.port.out.remote.routing.RoutingApiKeyProvider

class BuildConfigRoutingApiKeyProviderImpl : RoutingApiKeyProvider {
    override fun getRoutingApiKey(): String = BuildConfig.GRAPH_HOPPER_API_KEY
}
