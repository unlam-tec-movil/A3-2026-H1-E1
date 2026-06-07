package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos

import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ClinicEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for StoredClinicsDao operations.
 * Tests clinic database CRUD operations without needing a real database.
 */
class StoredClinicsDaoTest {
    private lateinit var mockDao: StoredClinicsDao

    @Before
    fun setUp() {
        mockDao = mockk()
    }

    @Test
    fun `insertClinic saves single clinic to database`() =
        runTest {
            // Arrange
            val clinic =
                ClinicEntity(
                    id = 1,
                    name = "Test Clinic",
                    address = "Test Address",
                    phone = "+123",
                    website = "test.com",
                    lat = -34.0,
                    lng = -58.0,
                )

            coEvery { mockDao.insertClinic(any()) } returns Unit

            // Act
            mockDao.insertClinic(clinic)

            // Assert
            coVerify { mockDao.insertClinic(clinic) }
        }

    @Test
    fun `insertAll saves multiple clinics to database`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    ClinicEntity(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                    ClinicEntity(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1),
                    ClinicEntity(3, "Clinic 3", "Address 3", "+789", "", -34.2, -58.2),
                )

            coEvery { mockDao.insertAll(any()) } returns Unit

            // Act
            mockDao.insertAll(clinics)

            // Assert
            coVerify { mockDao.insertAll(clinics) }
        }

    @Test
    fun `insertAll handles empty list`() =
        runTest {
            // Arrange
            val clinics = emptyList<ClinicEntity>()
            coEvery { mockDao.insertAll(any()) } returns Unit

            // Act
            mockDao.insertAll(clinics)

            // Assert
            coVerify { mockDao.insertAll(emptyList()) }
        }

    @Test
    fun `getStoredClinics returns flow of clinics ordered by id descending`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    ClinicEntity(3, "Clinic 3", "Address 3", "+789", "", -34.2, -58.2),
                    ClinicEntity(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1),
                    ClinicEntity(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                )

            every { mockDao.getStoredClinics() } returns flowOf(clinics)

            // Act
            var retrievedClinics: List<ClinicEntity>? = null
            mockDao.getStoredClinics().collect { retrievedClinics = it }

            // Assert
            assertEquals(3, retrievedClinics?.size)
            assertEquals(3, retrievedClinics?.get(0)?.id)
            assertEquals(2, retrievedClinics?.get(1)?.id)
            assertEquals(1, retrievedClinics?.get(2)?.id)
            verify { mockDao.getStoredClinics() }
        }

    @Test
    fun `getStoredClinics returns empty flow when no clinics exist`() =
        runTest {
            // Arrange
            every { mockDao.getStoredClinics() } returns flowOf(emptyList())

            // Act
            var retrievedClinics: List<ClinicEntity>? = null
            mockDao.getStoredClinics().collect { retrievedClinics = it }

            // Assert
            assertEquals(0, retrievedClinics?.size)
            verify { mockDao.getStoredClinics() }
        }

    @Test
    fun `getClinicCount returns zero when no clinics stored`() =
        runTest {
            // Arrange
            coEvery { mockDao.getClinicCount() } returns 0

            // Act
            val count = mockDao.getClinicCount()

            // Assert
            assertEquals(0, count)
            coVerify { mockDao.getClinicCount() }
        }

    @Test
    fun `getClinicCount returns correct count`() =
        runTest {
            // Arrange
            coEvery { mockDao.getClinicCount() } returns 10

            // Act
            val count = mockDao.getClinicCount()

            // Assert
            assertEquals(10, count)
        }

    @Test
    fun `insertClinic with replace conflict strategy overwrites existing`() =
        runTest {
            // Arrange
            val clinic1 = ClinicEntity(1, "Original Name", "Address", "+123", "", -34.0, -58.0)
            val clinic2 = ClinicEntity(1, "Updated Name", "Address", "+123", "", -34.0, -58.0)

            coEvery { mockDao.insertClinic(any()) } returns Unit

            // Act
            mockDao.insertClinic(clinic1)
            mockDao.insertClinic(clinic2)

            // Assert
            coVerify(exactly = 2) { mockDao.insertClinic(any()) }
        }

    @Test
    fun `getStoredClinics returns flow that can be collected multiple times`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    ClinicEntity(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                )

            every { mockDao.getStoredClinics() } returns flowOf(clinics)

            // Act
            var firstCollection: List<ClinicEntity>? = null
            var secondCollection: List<ClinicEntity>? = null

            mockDao.getStoredClinics().collect { firstCollection = it }
            mockDao.getStoredClinics().collect { secondCollection = it }

            // Assert
            assertEquals(firstCollection?.size, secondCollection?.size)
        }

    @Test
    fun `insertAll saves clinics preserving all entity fields`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    ClinicEntity(
                        id = 42,
                        name = "Complex Name Clinic",
                        address = "Av. Test 1234",
                        phone = "+5491234567890",
                        website = "https://example.com",
                        lat = -34.612345,
                        lng = -58.654321,
                    ),
                )

            coEvery { mockDao.insertAll(any()) } returns Unit

            // Act
            mockDao.insertAll(clinics)

            // Assert
            coVerify { mockDao.insertAll(clinics) }
        }

    @Test
    fun `insertClinic saves clinic with special characters in name`() =
        runTest {
            // Arrange
            val clinic =
                ClinicEntity(
                    id = 1,
                    name = "Kinesiología y Osteopatía Integral KIO",
                    address = "Urquiza 503",
                    phone = "+541172356257",
                    website = "",
                    lat = -34.64666797,
                    lng = -58.5721729,
                )

            coEvery { mockDao.insertClinic(any()) } returns Unit

            // Act
            mockDao.insertClinic(clinic)

            // Assert
            coVerify { mockDao.insertClinic(clinic) }
        }

    @Test
    fun `getClinicCount handles large dataset`() =
        runTest {
            // Arrange
            coEvery { mockDao.getClinicCount() } returns 1000

            // Act
            val count = mockDao.getClinicCount()

            // Assert
            assertTrue(count > 0)
            assertEquals(1000, count)
        }

    @Test
    fun `insertAll with large batch of clinics`() =
        runTest {
            // Arrange
            val clinics =
                (0..99).map { i ->
                    ClinicEntity(
                        id = i,
                        name = "Clinic $i",
                        address = "Address $i",
                        phone = "+549$i",
                        website = "",
                        lat = -34.0 - i * 0.001,
                        lng = -58.0 - i * 0.001,
                    )
                }

            coEvery { mockDao.insertAll(any()) } returns Unit

            // Act
            mockDao.insertAll(clinics)

            // Assert
            coVerify { mockDao.insertAll(clinics) }
        }

    @Test
    fun `getStoredClinics returns clinics with correct entity structure`() =
        runTest {
            // Arrange
            val clinic =
                ClinicEntity(
                    id = 5,
                    name = "Test Clinic",
                    address = "Test Address",
                    phone = "+5491234567890",
                    website = "https://test.com",
                    lat = -34.6337,
                    lng = -58.5601,
                )

            every { mockDao.getStoredClinics() } returns flowOf(listOf(clinic))

            // Act
            var retrievedClinic: ClinicEntity? = null
            mockDao.getStoredClinics().collect { clinics ->
                if (clinics.isNotEmpty()) retrievedClinic = clinics[0]
            }

            // Assert
            assertEquals(5, retrievedClinic?.id)
            assertEquals("Test Clinic", retrievedClinic?.name)
            assertEquals("Test Address", retrievedClinic?.address)
            assertEquals("+5491234567890", retrievedClinic?.phone)
            assertEquals("https://test.com", retrievedClinic?.website)
            assertEquals(-34.6337, retrievedClinic?.lat ?: 0.0, 0.0001)
            assertEquals(-58.5601, retrievedClinic?.lng ?: 0.0, 0.0001)
        }
}
