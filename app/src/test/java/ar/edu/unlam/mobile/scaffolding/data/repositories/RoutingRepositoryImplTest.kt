package ar.edu.unlam.mobile.scaffolding.data.repositories

import ar.edu.unlam.mobile.scaffolding.application.port.out.routing.RoutingApiKeyProvider
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.apiRouting.RoutingApi
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteRequest
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RoutingRepositoryImplTest {
    private lateinit var repository: RoutingRepositoryImpl
    private val api: RoutingApi = mockk()
    private val apiKeyProvider: RoutingApiKeyProvider = mockk()

    @Before
    fun setUp() {
        repository =
            RoutingRepositoryImpl(
                api = api,
                routingApiKeyProvider = apiKeyProvider,
            )
    }

    @Test
    fun `getRoute must swap coordinates to match GraphHopper LngLat request parameters`() =
        runTest {
            val origin = LatLng(-34.636, -58.556)
            val destination = LatLng(-34.640, -58.562)

            val fakeApiKey = "test_api_key"
            val expectedResponse = RouteResponse(paths = emptyList())

            val requestSlot = slot<RouteRequest>()

            every {
                apiKeyProvider.getRoutingApiKey()
            } returns fakeApiKey
            coEvery {
                api.getRoute(
                    apiKey = fakeApiKey,
                    request = capture(requestSlot),
                )
            } returns expectedResponse

            val result = repository.getRoute(origin = origin, destination = destination)

            val capturedRequest = requestSlot.captured

            assertEquals(-58.556, capturedRequest.points[0][0], 0.0)
            assertEquals(-34.636, capturedRequest.points[0][1], 0.0)

            assertEquals(-58.562, capturedRequest.points[1][0], 0.0)
            assertEquals(-34.640, capturedRequest.points[1][1], 0.0)

            assertEquals(expectedResponse, result)

            coVerify(exactly = 1) { api.getRoute(fakeApiKey, any()) }
            verify(exactly = 1) { apiKeyProvider.getRoutingApiKey() }
        }

    @Test(expected = Exception::class)
    fun `getRoute should propagate network exceptions`() =
        runTest {
            every { apiKeyProvider.getRoutingApiKey() } returns "key"
            coEvery { api.getRoute(any(), any()) } throws Exception("Network timeout")

            repository.getRoute(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        }
}
