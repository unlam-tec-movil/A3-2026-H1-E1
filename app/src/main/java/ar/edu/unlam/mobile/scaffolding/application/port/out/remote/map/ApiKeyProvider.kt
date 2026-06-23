package ar.edu.unlam.mobile.scaffolding.application.port.out.remote.map

interface ApiKeyProvider {
    // for MapTiler(the map, the routing api key is RoutingApiKeyProvider)
    fun getApiKey(): String
}
