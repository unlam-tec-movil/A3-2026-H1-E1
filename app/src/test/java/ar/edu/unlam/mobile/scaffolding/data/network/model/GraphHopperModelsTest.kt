package ar.edu.unlam.mobile.scaffolding.data.network.model

import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Path
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.Points
import ar.edu.unlam.mobile.scaffolding.data.datasources.network.model.RouteResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class GraphHopperModelsTest {
    // geoJson uses lngLat format while the maps sdk's uses LatLng
    @Test
    fun `Points latLngs should properly swap longitude and latitude`() {
        val coordinates = listOf(listOf(-58.4, -34.6), listOf(-58.5, -34.7))

        val points = Points(type = "LineString", coordinates = coordinates)

        val result = points.latLngs

        assertEquals("should have 2 points", 2, result.size)
        assertEquals("First point latitude mismatch", -34.6, result[0].latitude, 0.0)
        assertEquals("First point longitude mismatch", -58.4, result[0].longitude, 0.0)

        assertEquals("Second point point latitude mismatch", -34.7, result[1].latitude, 0.0)
        assertEquals("Second point longitude mismatch", -58.5, result[1].longitude, 0.0)
    }

    @Test
    fun `Points latLngs should return empty when coordinates are empty`() {
        val points =
            Points(
                type = "LineString",
                coordinates = emptyList(),
            )
        assertEquals("list of coordinates empty", 0, points.latLngs.size)
    }

    @Test
    fun `Path and RouteResponse should maintain data integrity`() {
        val points = Points(type = "LineString", coordinates = listOf(listOf(0.0, 0.0)))
        val path = Path(points = points, distance = 1234.56)
        val response = RouteResponse(paths = listOf(path))

        assertEquals(1, response.paths.size)
        assertEquals(1234.56, response.paths[0].distance, 0.0)
        assertEquals("LineString", response.paths[0].points.type)
    }
}
