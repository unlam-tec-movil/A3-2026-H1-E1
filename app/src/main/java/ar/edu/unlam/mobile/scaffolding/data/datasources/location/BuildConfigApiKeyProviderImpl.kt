package ar.edu.unlam.mobile.scaffolding.data.datasources.location

import ar.edu.unlam.mobile.scaffolding.BuildConfig
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.ApiKeyProvider
import javax.inject.Inject

class BuildConfigApiKeyProviderImpl
    @Inject
    constructor() : ApiKeyProvider {
        override fun getApiKey(): String = BuildConfig.API_KEY
    }
