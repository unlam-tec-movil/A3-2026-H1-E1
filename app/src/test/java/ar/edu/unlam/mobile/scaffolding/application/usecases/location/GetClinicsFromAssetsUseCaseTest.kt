package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetClinicsFromAssetsUseCaseTest {
    private lateinit var useCase: GetClinicsFromAssetsUseCase
    private val mockRepository: DataBaseLocationRepositoryPort = mockk()

    @Before
    fun setUp() {
        useCase = GetClinicsFromAssetsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns clinics list from repository`() {
        // Arrange
        val expectedClinics = listOf(
            Clinic(
                id = 0,
                name = "Centro Integral de Kinesiología",
                address = "Avenida Gaona 1843",
                phone = "+5491121643873",
                website = "",
                lat = -34.63603901,
                lng = -58.55649233,
            ),
            Clinic(
                id = 1,
                name = "Kinesiología Deportiva Traumatológica e Integral",
                address = "Doctor Gabriel Ardoino 134",
                phone = "+541173651817",
                website = "https://www.instagram.com/kdt.rehabilitacion/",
                lat = -34.63983,
                lng = -58.56261,
            ),
        )

        every { mockRepository.getClinicsFromAssets() } returns expectedClinics

        // Act
        val result = useCase()

        // Assert
        assertEquals(2, result.size)
        assertEquals(expectedClinics[0].name, result[0].name)
        assertEquals(expectedClinics[1].name, result[1].name)
        verify { mockRepository.getClinicsFromAssets() }
    }

    @Test
    fun `invoke handles empty clinics list`() {
        // Arrange
        every { mockRepository.getClinicsFromAssets() } returns emptyList()

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isEmpty())
        verify { mockRepository.getClinicsFromAssets() }
    }

    @Test
    fun `invoke preserves clinic data integrity`() {
        // Arrange
        val clinic = Clinic(
            id = 42,
            name = "Test Clinic",
            address = "Test Address",
            phone = "+5491234567890",
            website = "https://test.com",
            lat = -34.6337,
            lng = -58.5601,
        )

        every { mockRepository.getClinicsFromAssets() } returns listOf(clinic)

        // Act
        val result = useCase()

        // Assert
        assertEquals(1, result.size)
        val retrievedClinic = result[0]
        assertEquals(clinic.id, retrievedClinic.id)
        assertEquals(clinic.name, retrievedClinic.name)
        assertEquals(clinic.address, retrievedClinic.address)
        assertEquals(clinic.phone, retrievedClinic.phone)
        assertEquals(clinic.website, retrievedClinic.website)
        assertEquals(clinic.lat, retrievedClinic.lat, 0.0001)
        assertEquals(clinic.lng, retrievedClinic.lng, 0.0001)
    }

    @Test
    fun `invoke is called and delegates to repository`() {
        // Arrange
        val clinics = listOf(
            Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
            Clinic(2, "Clinic 2", "Address 2", "+456", "https://site.com", -34.1, -58.1),
            Clinic(3, "Clinic 3", "Address 3", "+789", "", -34.2, -58.2),
        )

        every { mockRepository.getClinicsFromAssets() } returns clinics

        // Act
        val result = useCase()

        // Assert
        assertEquals(3, result.size)
        verify { mockRepository.getClinicsFromAssets() }
    }

    @Test
    fun `invoke returns clinics in correct order`() {
        // Arrange
        val clinic1 = Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0)
        val clinic2 = Clinic(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1)
        val clinic3 = Clinic(3, "Clinic 3", "Address 3", "+789", "", -34.2, -58.2)

        every { mockRepository.getClinicsFromAssets() } returns listOf(clinic1, clinic2, clinic3)

        // Act
        val result = useCase()

        // Assert
        assertEquals(clinic1.id, result[0].id)
        assertEquals(clinic2.id, result[1].id)
        assertEquals(clinic3.id, result[2].id)
    }

    @Test
    fun `invoke can be called multiple times and returns same data`() {
        // Arrange
        val clinics = listOf(
            Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
        )

        every { mockRepository.getClinicsFromAssets() } returns clinics

        // Act
        val result1 = useCase()
        val result2 = useCase()

        // Assert
        assertEquals(result1.size, result2.size)
        assertEquals(result1[0].name, result2[0].name)
        verify(exactly = 2) { mockRepository.getClinicsFromAssets() }
    }
}
