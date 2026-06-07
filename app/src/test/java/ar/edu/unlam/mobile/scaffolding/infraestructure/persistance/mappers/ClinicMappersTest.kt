package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ClinicEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ClinicMappersTest {
    @Test
    fun `clinic to entity mapper preserves all fields`() {
        // Arrange
        val clinic =
            Clinic(
                id = 42,
                name = "Centro Integral de Kinesiología",
                address = "Avenida Gaona 1843",
                phone = "+5491121643873",
                website = "https://example.com",
                lat = -34.63603901,
                lng = -58.55649233,
            )

        // Act
        val entity = clinic.toEntity()

        // Assert
        assertEquals(42, entity.id)
        assertEquals("Centro Integral de Kinesiología", entity.name)
        assertEquals("Avenida Gaona 1843", entity.address)
        assertEquals("+5491121643873", entity.phone)
        assertEquals("https://example.com", entity.website)
        assertEquals(-34.63603901, entity.lat, 0.0001)
        assertEquals(-58.55649233, entity.lng, 0.0001)
    }

    @Test
    fun `clinic to entity mapper handles empty website`() {
        // Arrange
        val clinic =
            Clinic(
                id = 1,
                name = "Clinic Name",
                address = "Address",
                phone = "+123",
                website = "",
                lat = 0.0,
                lng = 0.0,
            )

        // Act
        val entity = clinic.toEntity()

        // Assert
        assertEquals("", entity.website)
    }

    @Test
    fun `clinic to entity mapper handles special characters in name`() {
        // Arrange
        val clinic =
            Clinic(
                id = 1,
                name = "Kinesiología y Osteopatía Integral KIO",
                address = "Urquiza 503",
                phone = "+541172356257",
                website = "",
                lat = -34.64666797,
                lng = -58.5721729,
            )

        // Act
        val entity = clinic.toEntity()

        // Assert
        assertEquals("Kinesiología y Osteopatía Integral KIO", entity.name)
    }

    @Test
    fun `clinic to entity mapper preserves precise coordinates`() {
        // Arrange
        val clinic =
            Clinic(
                id = 1,
                name = "Clinic",
                address = "Address",
                phone = "+123",
                website = "",
                lat = -34.64168309,
                lng = -58.56860626,
            )

        // Act
        val entity = clinic.toEntity()

        // Assert
        assertEquals(-34.64168309, entity.lat, 0.00000001)
        assertEquals(-58.56860626, entity.lng, 0.00000001)
    }

    @Test
    fun `entity to clinic mapper preserves all fields`() {
        // Arrange
        val entity =
            ClinicEntity(
                id = 7,
                name = "Kinesiología y Estética",
                address = "25 de Mayo 27",
                phone = "+541146567454",
                website = "https://example.com",
                lat = -34.6501572,
                lng = -58.55523603,
            )

        // Act
        val clinic = entity.toDomain()

        // Assert
        assertEquals(7, clinic.id)
        assertEquals("Kinesiología y Estética", clinic.name)
        assertEquals("25 de Mayo 27", clinic.address)
        assertEquals("+541146567454", clinic.phone)
        assertEquals("https://example.com", clinic.website)
        assertEquals(-34.6501572, clinic.lat, 0.0001)
        assertEquals(-58.55523603, clinic.lng, 0.0001)
    }

    @Test
    fun `entity to clinic mapper handles empty website`() {
        // Arrange
        val entity =
            ClinicEntity(
                id = 1,
                name = "Clinic Name",
                address = "Address",
                phone = "+123",
                website = "",
                lat = 0.0,
                lng = 0.0,
            )

        // Act
        val clinic = entity.toDomain()

        // Assert
        assertEquals("", clinic.website)
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        // Arrange
        val originalClinic =
            Clinic(
                id = 99,
                name = "Test Clinic",
                address = "Test Address",
                phone = "+5491234567890",
                website = "https://test.com",
                lat = -34.6234,
                lng = -58.5678,
            )

        // Act
        val entity = originalClinic.toEntity()
        val roundTripClinic = entity.toDomain()

        // Assert
        assertEquals(originalClinic.id, roundTripClinic.id)
        assertEquals(originalClinic.name, roundTripClinic.name)
        assertEquals(originalClinic.address, roundTripClinic.address)
        assertEquals(originalClinic.phone, roundTripClinic.phone)
        assertEquals(originalClinic.website, roundTripClinic.website)
        assertEquals(originalClinic.lat, roundTripClinic.lat, 0.0001)
        assertEquals(originalClinic.lng, roundTripClinic.lng, 0.0001)
    }

    @Test
    fun `entity to clinic mapper preserves precise coordinates`() {
        // Arrange
        val entity =
            ClinicEntity(
                id = 1,
                name = "Clinic",
                address = "Address",
                phone = "+123",
                website = "",
                lat = -34.65436959,
                lng = -58.55087725,
            )

        // Act
        val clinic = entity.toDomain()

        // Assert
        assertEquals(-34.65436959, clinic.lat, 0.00000001)
        assertEquals(-58.55087725, clinic.lng, 0.00000001)
    }

    @Test
    fun `clinic to entity mapper with zero coordinates`() {
        // Arrange
        val clinic =
            Clinic(
                id = 1,
                name = "Clinic",
                address = "Address",
                phone = "+123",
                website = "",
                lat = 0.0,
                lng = 0.0,
            )

        // Act
        val entity = clinic.toEntity()

        // Assert
        assertEquals(0.0, entity.lat, 0.0001)
        assertEquals(0.0, entity.lng, 0.0001)
    }

    @Test
    fun `clinic to entity mapper with negative coordinates`() {
        // Arrange
        val clinic =
            Clinic(
                id = 1,
                name = "Clinic",
                address = "Address",
                phone = "+123",
                website = "",
                lat = -90.0,
                lng = -180.0,
            )

        // Act
        val entity = clinic.toEntity()

        // Assert
        assertEquals(-90.0, entity.lat, 0.0001)
        assertEquals(-180.0, entity.lng, 0.0001)
    }
}
