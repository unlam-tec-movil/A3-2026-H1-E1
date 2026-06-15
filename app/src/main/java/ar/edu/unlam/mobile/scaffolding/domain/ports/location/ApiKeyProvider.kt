package ar.edu.unlam.mobile.scaffolding.domain.ports.location

interface ApiKeyProvider {
    // for MapTiler(the map, the routing api key is RoutingApiKeyProvider)
    fun getApiKey(): String
}
