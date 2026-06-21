package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.application.port.out.local.db.ClinicsRepositoryPort
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PopulateClinicsDbUseCaseTest {
    private lateinit var useCase: PopulateClinicsDbUseCase
    private val mockRepository: ClinicsRepositoryPort = mockk()

    @Before
    fun setUp() {
        useCase = PopulateClinicsDbUseCase(mockRepository)
    }

    @Test
    fun `invoke saves clinics to repository`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
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
                        name = "Kinesiología Deportiva",
                        address = "Doctor Gabriel Ardoino 134",
                        phone = "+541173651817",
                        website = "https://www.instagram.com/kdt.rehabilitacion/",
                        lat = -34.63983,
                        lng = -58.56261,
                    ),
                )

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(clinics)

            // Assert
            coVerify { mockRepository.saveAllClinics(clinics) }
        }

    @Test
    fun `invoke handles empty clinics list`() =
        runTest {
            // Arrange
            val clinics = emptyList<Clinic>()
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(clinics)

            // Assert
            coVerify { mockRepository.saveAllClinics(emptyList()) }
        }

    @Test
    fun `invoke saves single clinic`() =
        runTest {
            // Arrange
            val clinic =
                Clinic(
                    id = 5,
                    name = "Test Clinic",
                    address = "Test Address",
                    phone = "+5491234567890",
                    website = "https://test.com",
                    lat = -34.6337,
                    lng = -58.5601,
                )

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(listOf(clinic))

            // Assert
            coVerify { mockRepository.saveAllClinics(listOf(clinic)) }
        }

    @Test
    fun `invoke saves large clinics list`() =
        runTest {
            // Arrange
            val clinics =
                (0..99).map { i ->
                    Clinic(
                        id = i,
                        name = "Clinic $i",
                        address = "Address $i",
                        phone = "+549$i",
                        website = "https://site$i.com",
                        lat = -34.6 - i * 0.001,
                        lng = -58.5 - i * 0.001,
                    )
                }

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(clinics)

            // Assert
            coVerify { mockRepository.saveAllClinics(clinics) }
        }

    @Test
    fun `invoke handles clinics with special characters`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    Clinic(
                        id = 1,
                        name = "Kinesiología y Osteopatía Integral KIO",
                        address = "Urquiza 503",
                        phone = "+541172356257",
                        website = "",
                        lat = -34.64666797,
                        lng = -58.5721729,
                    ),
                )

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(clinics)

            // Assert
            coVerify { mockRepository.saveAllClinics(clinics) }
        }

    @Test
    fun `invoke can be called multiple times with different data`() =
        runTest {
            // Arrange
            val clinics1 = listOf(Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0))
            val clinics2 = listOf(Clinic(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1))

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(clinics1)
            useCase(clinics2)

            // Assert
            coVerify(exactly = 1) { mockRepository.saveAllClinics(clinics1) }
            coVerify(exactly = 1) { mockRepository.saveAllClinics(clinics2) }
        }

    @Test
    fun `invoke preserves data integrity during save`() =
        runTest {
            // Arrange
            val clinic =
                Clinic(
                    id = 42,
                    name = "Test Clinic With Complex Name",
                    address = "Av. Test 1234 - Buenos Aires",
                    phone = "+5491123456789",
                    website = "https://example.com",
                    lat = -34.634567,
                    lng = -58.560123,
                )

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            useCase(listOf(clinic))

            // Assert
            coVerify { mockRepository.saveAllClinics(listOf(clinic)) }
        }
}
