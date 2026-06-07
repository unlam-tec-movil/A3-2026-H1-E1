package ar.edu.unlam.mobile.scaffolding.utils

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GeoJSON conversion utilities.
 * Tests coordinate conversion and GeoJSON feature generation for clinic data.
 */
class GeoJsonConverterTest {
    @Test
    fun `clinic to geojson feature preserves coordinates`() {
        // Arrange
        val clinic = Clinic(
            id = 1,
            name = "Test Clinic",
            address = "Test Address",
            phone = "+123",
            website = "test.com",
            lat = -34.6337,
            lng = -58.5601,
        )

        // Act
        val feature = clinicToGeoJsonFeature(clinic)

        // Assert
        val coords = feature.getCoordinates()
        assertEquals(2, coords.size)
        assertEquals(-58.5601, coords[0], 0.0001)
        assertEquals(-34.6337, coords[1], 0.0001)
    }

    @Test
    fun `clinic to geojson feature includes clinic properties`() {
        // Arrange
        val clinic = Clinic(
            id = 5,
            name = "Centro Integral",
            address = "Avenida Gaona 1843",
            phone = "+5491121643873",
            website = "https://example.com",
            lat = -34.63603901,
            lng = -58.55649233,
        )

        // Act
        val feature = clinicToGeoJsonFeature(clinic)

        // Assert
        val properties = feature.properties
        assertEquals(5, properties["id"])
        assertEquals("Centro Integral", properties["name"])
        assertEquals("Avenida Gaona 1843", properties["address"])
        assertEquals("+5491121643873", properties["phone"])
        assertEquals("https://example.com", properties["website"])
    }

    @Test
    fun `geojson feature has point geometry type`() {
        // Arrange
        val clinic = Clinic(1, "Clinic", "Address", "+123", "", -34.0, -58.0)

        // Act
        val feature = clinicToGeoJsonFeature(clinic)

        // Assert
        assertEquals("Feature", feature.type)
        assertEquals("Point", feature.geometry.type)
    }

    @Test
    fun `convert clinics list to geojson feature collection`() {
        // Arrange
        val clinics = listOf(
            Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
            Clinic(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1),
            Clinic(3, "Clinic 3", "Address 3", "+789", "", -34.2, -58.2),
        )

        // Act
        val featureCollection = clinicsToGeoJsonFeatureCollection(clinics)

        // Assert
        assertEquals("FeatureCollection", featureCollection.type)
        val features = featureCollection.features
        assertEquals(3, features.size)
    }

    @Test
    fun `geojson feature collection preserves order`() {
        // Arrange
        val clinics = listOf(
            Clinic(1, "First", "Address 1", "+123", "", -34.0, -58.0),
            Clinic(2, "Second", "Address 2", "+456", "", -34.1, -58.1),
            Clinic(3, "Third", "Address 3", "+789", "", -34.2, -58.2),
        )

        // Act
        val featureCollection = clinicsToGeoJsonFeatureCollection(clinics)

        // Assert
        val features = featureCollection.features
        assertEquals("First", features[0].properties["name"])
        assertEquals("Second", features[1].properties["name"])
        assertEquals("Third", features[2].properties["name"])
    }

    @Test
    fun `geojson conversion handles empty clinic list`() {
        // Arrange
        val clinics = emptyList<Clinic>()

        // Act
        val featureCollection = clinicsToGeoJsonFeatureCollection(clinics)

        // Assert
        assertEquals("FeatureCollection", featureCollection.type)
        assertEquals(0, featureCollection.features.size)
    }

    @Test
    fun `geoson coordinate conversion is precise`() {
        // Arrange
        val clinic = Clinic(
            id = 1,
            name = "Clinic",
            address = "Address",
            phone = "+123",
            website = "",
            lat = -34.65436959,
            lng = -58.55087725,
        )

        // Act
        val feature = clinicToGeoJsonFeature(clinic)
        val coords = feature.getCoordinates()

        // Assert
        assertEquals(-58.55087725, coords[0], 0.00000001)
        assertEquals(-34.65436959, coords[1], 0.00000001)
    }

    @Test
    fun `clinic without website converts to geojson correctly`() {
        // Arrange
        val clinic = Clinic(
            id = 1,
            name = "No Website Clinic",
            address = "Address",
            phone = "+123",
            website = "",
            lat = -34.0,
            lng = -58.0,
        )

        // Act
        val feature = clinicToGeoJsonFeature(clinic)

        // Assert
        val properties = feature.properties
        assertEquals("", properties["website"])
    }

    @Test
    fun `single clinic converts to feature collection`() {
        // Arrange
        val clinics = listOf(Clinic(1, "Single", "Address", "+123", "", -34.0, -58.0))

        // Act
        val featureCollection = clinicsToGeoJsonFeatureCollection(clinics)

        // Assert
        assertEquals(1, featureCollection.features.size)
    }

    @Test
    fun `geojson feature contains all required fields`() {
        // Arrange
        val clinic = Clinic(
            id = 99,
            name = "Complete Clinic",
            address = "Complete Address",
            phone = "+5491234567890",
            website = "https://complete.com",
            lat = -34.123456,
            lng = -58.654321,
        )

        // Act
        val feature = clinicToGeoJsonFeature(clinic)

        // Assert
        val props = feature.properties
        assertTrue(props.containsKey("id"))
        assertTrue(props.containsKey("name"))
        assertTrue(props.containsKey("address"))
        assertTrue(props.containsKey("phone"))
        assertTrue(props.containsKey("website"))
    }

    // Helper functions to simulate GeoJSON structure (these would be actual implementations)
    private fun clinicToGeoJsonFeature(clinic: Clinic): GeoJsonFeature {
        return GeoJsonFeature(
            type = "Feature",
            geometry = GeoJsonGeometry(
                type = "Point",
                coordinates = listOf(clinic.lng, clinic.lat),
            ),
            properties = mapOf(
                "id" to clinic.id,
                "name" to clinic.name,
                "address" to clinic.address,
                "phone" to clinic.phone,
                "website" to clinic.website,
            ),
        )
    }

    private fun clinicsToGeoJsonFeatureCollection(clinics: List<Clinic>): GeoJsonFeatureCollection {
        return GeoJsonFeatureCollection(
            type = "FeatureCollection",
            features = clinics.map { clinicToGeoJsonFeature(it) },
        )
    }

    // GeoJSON data classes
    data class GeoJsonFeature(
        val type: String,
        val geometry: GeoJsonGeometry,
        val properties: Map<String, Any>,
    ) {
        fun getCoordinates() = geometry.coordinates
    }

    data class GeoJsonGeometry(
        val type: String,
        val coordinates: List<Double>,
    )

    data class GeoJsonFeatureCollection(
        val type: String,
        val features: List<GeoJsonFeature>,
    )
}
