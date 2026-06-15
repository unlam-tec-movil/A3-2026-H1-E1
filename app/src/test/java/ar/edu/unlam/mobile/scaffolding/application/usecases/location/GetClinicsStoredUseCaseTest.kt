package ar.edu.unlam.mobile.scaffolding.application.usecases.location

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@Suppress("CheckResult", "UnusedFlow")
class GetClinicsStoredUseCaseTest {
    private lateinit var useCase: GetClinicsStoredUseCase
    private val mockRepository: DataBaseLocationRepositoryPort = mockk()

    @Before
    fun setUp() {
        useCase = GetClinicsStoredUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow of stored clinics`() =
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

            every { mockRepository.getClinics() } returns flowOf(clinics)

            // Act
            val flow = useCase()
            var collectedClinics: List<Clinic>? = null
            flow.collect { collectedClinics = it }

            // Assert
            assertEquals(2, collectedClinics?.size)
            assertEquals(clinics[0].name, collectedClinics?.get(0)?.name)
            assertEquals(clinics[1].name, collectedClinics?.get(1)?.name)
            verify { mockRepository.getClinics() }
        }

    @Test
    fun `invoke returns empty flow when no clinics stored`() =
        runTest {
            // Arrange
            every { mockRepository.getClinics() } returns flowOf(emptyList())

            // Act
            val flow = useCase()
            var collectedClinics: List<Clinic>? = null
            flow.collect { collectedClinics = it }

            // Assert
            assertEquals(0, collectedClinics?.size)
            verify { mockRepository.getClinics() }
        }

    @Test
    fun `invoke returns flow that emits all clinics`() =
        runTest {
            // Arrange
            val clinics =
                (0..9).map { i ->
                    Clinic(
                        id = i,
                        name = "Clinic $i",
                        address = "Address $i",
                        phone = "+549$i",
                        website = if (i % 2 == 0) "" else "https://site$i.com",
                        lat = -34.6 - i * 0.001,
                        lng = -58.5 - i * 0.001,
                    )
                }

            every { mockRepository.getClinics() } returns flowOf(clinics)

            // Act
            val flow = useCase()
            var collectedClinics: List<Clinic>? = null
            flow.collect { collectedClinics = it }

            // Assert
            assertEquals(10, collectedClinics?.size)
            verify { mockRepository.getClinics() }
        }

    @Test
    fun `invoke returns single clinic in flow`() =
        runTest {
            // Arrange
            val clinic =
                Clinic(
                    id = 5,
                    name = "Single Clinic",
                    address = "Single Address",
                    phone = "+5491234567890",
                    website = "https://test.com",
                    lat = -34.6337,
                    lng = -58.5601,
                )

            every { mockRepository.getClinics() } returns flowOf(listOf(clinic))

            // Act
            val flow = useCase()
            var collectedClinics: List<Clinic>? = null
            flow.collect { collectedClinics = it }

            // Assert
            assertEquals(1, collectedClinics?.size)
            assertEquals(clinic.name, collectedClinics?.get(0)?.name)
        }

    @Test
    fun `invoke delegates to repository getStoredClinics`() =
        runTest {
            // Arrange
            every { mockRepository.getClinics() } returns flowOf(emptyList())

            // Act
            useCase()

            // Assert
            verify { mockRepository.getClinics() }
        }

    @Test
    fun `invoke preserves clinic data integrity`() =
        runTest {
            // Arrange
            val clinic =
                Clinic(
                    id = 99,
                    name = "Integrity Test Clinic",
                    address = "Integrity Test Address",
                    phone = "+5491987654321",
                    website = "https://integrity-test.com",
                    lat = -34.612345,
                    lng = -58.567890,
                )

            every { mockRepository.getClinics() } returns flowOf(listOf(clinic))

            // Act
            val flow = useCase()
            var collectedClinic: Clinic? = null
            flow.collect { clinics ->
                if (clinics.isNotEmpty()) collectedClinic = clinics[0]
            }

            // Assert
            assertEquals(clinic.id, collectedClinic?.id)
            assertEquals(clinic.name, collectedClinic?.name)
            assertEquals(clinic.address, collectedClinic?.address)
            assertEquals(clinic.phone, collectedClinic?.phone)
            assertEquals(clinic.website, collectedClinic?.website)
            assertEquals(clinic.lat, collectedClinic?.lat ?: 0.0, 0.0001)
            assertEquals(clinic.lng, collectedClinic?.lng ?: 0.0, 0.0001)
        }

    @Test
    fun `invoke returns flow that can be collected multiple times`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                    Clinic(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1),
                )

            every { mockRepository.getClinics() } returns flowOf(clinics)

            // Act
            val flow = useCase()
            var firstCollection: List<Clinic>? = null
            var secondCollection: List<Clinic>? = null

            flow.collect { firstCollection = it }
            flow.collect { secondCollection = it }

            // Assert
            assertEquals(firstCollection?.size, secondCollection?.size)
            assertEquals(firstCollection?.get(0)?.name, secondCollection?.get(0)?.name)
        }
}
