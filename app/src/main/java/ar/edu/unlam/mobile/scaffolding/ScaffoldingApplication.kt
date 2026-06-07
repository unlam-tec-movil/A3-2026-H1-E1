package ar.edu.unlam.mobile.scaffolding

import android.app.Application
import android.util.Log
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.ApiKeyProvider
import com.maptiler.maptilersdk.MTConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ScaffoldingApplication : Application() {
    @Inject lateinit var apiKeyProvider: ApiKeyProvider

    override fun onCreate() {
        super.onCreate()
        val apiKey = apiKeyProvider.getApiKey()
        Log.d(
            "ScaffoldingApp",
            "API Key loaded: ${if (apiKey.isNotEmpty()) "✓ (${apiKey.length} chars)" else "✗ EMPTY"}",
        )
        Log.d("ScaffoldingApp", "API Key value: $apiKey")
        MTConfig.apiKey = apiKey
    }
}
