package ar.edu.unlam.mobile.scaffolding.application.service.routing

import ar.edu.unlam.mobile.scaffolding.application.port.out.routing.RoutingRepository
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetRouteInteractorTest {
    private lateinit var interactor: GetRouteInteractor
    private val repository: RoutingRepository = mockk()

    @Before
    fun setUp() {
        interactor = GetRouteInteractor(repository)
    }

    @Test
    fun `interactor must call the repo with the correct coordinates and return a proper response`() =
        runTest {
            val origin = LatLng(-34.6, -58.5)
            val destination = LatLng(-34.6, -58.5)
            val expectedResponse = RouteResponse(paths = emptyList())

            coEvery {
                repository.getRoute(origin = origin, destination = destination)
            } returns expectedResponse
            val result = interactor.invoke(origin = origin, destination = destination)

            assertEquals(expectedResponse, result)
            coVerify(exactly = 1) { repository.getRoute(origin, destination) }
        }

    @Test(expected = Exception::class)
    fun `invoke should propagate exceptions from repository`() =
        runTest {
            val origin = LatLng(0.0, 0.0)
            val destination = LatLng(1.0, 1.0)

            coEvery {
                repository.getRoute(any(), any())
            } throws Exception("Network Error")

            interactor.invoke(origin, destination)
        }
}
