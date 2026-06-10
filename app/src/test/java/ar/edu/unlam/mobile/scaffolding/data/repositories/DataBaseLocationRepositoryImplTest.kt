package ar.edu.unlam.mobile.scaffolding.data.repositories

import android.content.Context
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.StoredClinicsDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.ClinicEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("CheckResult", "UnusedFlow")
class DataBaseLocationRepositoryImplTest {
    private lateinit var repository: DataBaseLocationRepositoryImpl
    private val mockClinicDao: StoredClinicsDao = mockk()
    private val mockContext: Context = mockk()

    @Before
    fun setUp() {
        repository = DataBaseLocationRepositoryImpl(mockClinicDao, mockContext)
    }

    @Test
    fun `getStoredClinics returns flow of clinics from DAO`() =
        runTest {
            // Arrange
            val clinicEntities =
                listOf(
                    ClinicEntity(
                        id = 1,
                        name = "Clinic A",
                        address = "Address A",
                        phone = "+123",
                        website = "site.com",
                        lat = -34.6037,
                        lng = -58.5609,
                    ),
                    ClinicEntity(
                        id = 2,
                        name = "Clinic B",
                        address = "Address B",
                        phone = "+456",
                        website = "site2.com",
                        lat = -34.6040,
                        lng = -58.5610,
                    ),
                )

            every { mockClinicDao.getStoredClinics() } returns flowOf(clinicEntities)

            // Act
            val result = repository.getStoredClinics()
            var clinics: List<Clinic>? = null
            result.collect { clinics = it }

            // Assert
            assertEquals(2, clinics?.size)
            assertEquals("Clinic A", clinics?.get(0)?.name)
            assertEquals("Clinic B", clinics?.get(1)?.name)
            verify { mockClinicDao.getStoredClinics() }
        }

    @Test
    fun `getStoredClinics returns empty flow when DAO returns no clinics`() =
        runTest {
            // Arrange
            every { mockClinicDao.getStoredClinics() } returns flowOf(emptyList())

            // Act
            val result = repository.getStoredClinics()
            var clinics: List<Clinic>? = null
            result.collect { clinics = it }

            // Assert
            assertEquals(0, clinics?.size)
        }

    @Test
    fun `saveAllClinics converts clinics to entities and inserts them`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    Clinic(
                        id = 1,
                        name = "Clinic A",
                        address = "Address A",
                        phone = "+123",
                        website = "site.com",
                        lat = -34.6037,
                        lng = -58.5609,
                    ),
                    Clinic(
                        id = 2,
                        name = "Clinic B",
                        address = "Address B",
                        phone = "+456",
                        website = "site2.com",
                        lat = -34.6040,
                        lng = -58.5610,
                    ),
                )

            coEvery { mockClinicDao.insertAll(any()) } returns Unit

            // Act
            repository.saveAllClinics(clinics)

            // Assert
            coVerify { mockClinicDao.insertAll(any()) }
        }

    @Test
    fun `saveAllClinics handles empty clinic list`() =
        runTest {
            // Arrange
            val clinics = emptyList<Clinic>()
            coEvery { mockClinicDao.insertAll(any()) } returns Unit

            // Act
            repository.saveAllClinics(clinics)

            // Assert
            coVerify { mockClinicDao.insertAll(emptyList()) }
        }

    @Test
    fun `hasStoredClinics returns true when count is greater than zero`() =
        runTest {
            // Arrange
            coEvery { mockClinicDao.getClinicCount() } returns 5

            // Act
            val result = repository.hasStoredClinics()

            // Assert
            assertTrue(result)
            coVerify { mockClinicDao.getClinicCount() }
        }

    @Test
    fun `hasStoredClinics returns false when count is zero`() =
        runTest {
            // Arrange
            coEvery { mockClinicDao.getClinicCount() } returns 0

            // Act
            val result = repository.hasStoredClinics()

            // Assert
            assertFalse(result)
        }

    @Test
    fun `hasStoredClinics returns false when count is negative`() =
        runTest {
            // Arrange
            coEvery { mockClinicDao.getClinicCount() } returns -1

            // Act
            val result = repository.hasStoredClinics()

            // Assert
            assertFalse(result)
        }

    @Test
    fun `getClinicsFromAssets parses JSON and returns clinic list`() {
        // Arrange
        val jsonContent =
            """
            {
              "meta": {
                "description": "Clínicas de Kinesiología",
                "source": "Test",
                "generated_at": "2026-06-04T00:51:36.589629+00:00",
                "reference_point": "Ciudadela, Buenos Aires (-34.6337, -58.5601)",
                "total": 2
              },
              "clinics": [
                {
                  "id": 0,
                  "name": "Centro Integral",
                  "address": "Avenida Gaona 1843",
                  "phone": "+5491121643873",
                  "website": "",
                  "lat": -34.63603901,
                  "lng": -58.55649233
                },
                {
                  "id": 1,
                  "name": "Kinesiología Deportiva",
                  "address": "Doctor Gabriel Ardoino 134",
                  "phone": "+541173651817",
                  "website": "https://www.instagram.com/kdt.rehabilitacion/",
                  "lat": -34.63983,
                  "lng": -58.56261
                }
              ]
            }
            """.trimIndent()

        val mockAsset = jsonContent.byteInputStream()
        every { mockContext.assets.open("clinicas_ciudadela_ba.json") } returns mockAsset

        // Act
        val clinics = repository.getClinicsFromAssets()

        // Assert
        assertEquals(2, clinics.size)
        assertEquals("Centro Integral", clinics[0].name)
        assertEquals("Kinesiología Deportiva", clinics[1].name)
        assertEquals(-34.63603901, clinics[0].lat, 0.0001)
        assertEquals(-58.55649233, clinics[0].lng, 0.0001)
        verify { mockContext.assets.open("clinicas_ciudadela_ba.json") }
    }

    @Test
    fun `getClinicsFromAssets maps all fields correctly`() {
        // Arrange
        val jsonContent =
            """
            {
              "meta": {"description": "Test"},
              "clinics": [
                {
                  "id": 5,
                  "name": "Test Clinic",
                  "address": "Test Address 123",
                  "phone": "+5491234567890",
                  "website": "https://test.com",
                  "lat": -34.6345,
                  "lng": -58.5610
                }
              ]
            }
            """.trimIndent()

        val mockAsset = jsonContent.byteInputStream()
        every { mockContext.assets.open("clinicas_ciudadela_ba.json") } returns mockAsset

        // Act
        val clinics = repository.getClinicsFromAssets()

        // Assert
        assertEquals(1, clinics.size)
        val clinic = clinics[0]
        assertEquals(5, clinic.id)
        assertEquals("Test Clinic", clinic.name)
        assertEquals("Test Address 123", clinic.address)
        assertEquals("+5491234567890", clinic.phone)
        assertEquals("https://test.com", clinic.website)
        assertEquals(-34.6345, clinic.lat, 0.0001)
        assertEquals(-58.5610, clinic.lng, 0.0001)
    }

    @Test
    fun `getClinicsFromAssets handles empty clinics array`() {
        // Arrange
        val jsonContent =
            """
            {
              "meta": {"description": "Test"},
              "clinics": []
            }
            """.trimIndent()

        val mockAsset = jsonContent.byteInputStream()
        every { mockContext.assets.open("clinicas_ciudadela_ba.json") } returns mockAsset

        // Act
        val clinics = repository.getClinicsFromAssets()

        // Assert
        assertEquals(0, clinics.size)
    }
}
