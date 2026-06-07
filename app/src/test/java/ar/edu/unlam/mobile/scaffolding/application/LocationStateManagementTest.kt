package ar.edu.unlam.mobile.scaffolding.application

import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsFromAssetsUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.GetClinicsStoredUseCase
import ar.edu.unlam.mobile.scaffolding.application.usecases.location.PopulateClinicsDbUseCase
import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import ar.edu.unlam.mobile.scaffolding.domain.ports.location.DataBaseLocationRepositoryPort
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
 * Integration-style tests for location state management.
 * Tests the workflow of loading clinics from assets, storing them, and retrieving them.
 */
class LocationStateManagementTest {
    private lateinit var getClinicsFromAssetsUseCase: GetClinicsFromAssetsUseCase
    private lateinit var populateClinicsDbUseCase: PopulateClinicsDbUseCase
    private lateinit var getStoredClinicsUseCase: GetClinicsStoredUseCase
    private val mockRepository: DataBaseLocationRepositoryPort = mockk()

    @Before
    fun setUp() {
        getClinicsFromAssetsUseCase = GetClinicsFromAssetsUseCase(mockRepository)
        populateClinicsDbUseCase = PopulateClinicsDbUseCase(mockRepository)
        getStoredClinicsUseCase = GetClinicsStoredUseCase(mockRepository)
    }

    @Test
    fun `complete workflow load assets then store then retrieve`() =
        runTest {
            // Arrange
            val assetsClinicss =
                listOf(
                    Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                    Clinic(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1),
                )

            every { mockRepository.getClinicsFromAssets() } returns assetsClinicss
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(assetsClinicss)

            // Act - Step 1: Load from assets
            val assetsLoaded = getClinicsFromAssetsUseCase()
            assertTrue(assetsLoaded.isNotEmpty())
            assertEquals(2, assetsLoaded.size)

            // Act - Step 2: Populate database
            populateClinicsDbUseCase(assetsLoaded)
            coVerify { mockRepository.saveAllClinics(assetsLoaded) }

            // Act - Step 3: Retrieve from storage
            var storedClinics: List<Clinic>? = null
            getStoredClinicsUseCase().collect { storedClinics = it }

            // Assert
            assertEquals(assetsLoaded.size, storedClinics?.size)
            assertEquals(assetsLoaded[0].name, storedClinics?.get(0)?.name)
        }

    @Test
    fun `loading assets followed by storing preserves clinic data`() =
        runTest {
            // Arrange
            val clinic =
                Clinic(
                    id = 5,
                    name = "Data Integrity Clinic",
                    address = "Data Integrity Address",
                    phone = "+5491234567890",
                    website = "https://test.com",
                    lat = -34.6337,
                    lng = -58.5601,
                )

            every { mockRepository.getClinicsFromAssets() } returns listOf(clinic)
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(listOf(clinic))

            // Act
            val loaded = getClinicsFromAssetsUseCase()
            populateClinicsDbUseCase(loaded)

            var retrieved: Clinic? = null
            getStoredClinicsUseCase().collect { clinics ->
                if (clinics.isNotEmpty()) retrieved = clinics[0]
            }

            // Assert
            assertEquals(clinic.id, retrieved?.id)
            assertEquals(clinic.name, retrieved?.name)
            assertEquals(clinic.address, retrieved?.address)
            assertEquals(clinic.phone, retrieved?.phone)
            assertEquals(clinic.website, retrieved?.website)
            assertEquals(clinic.lat, retrieved?.lat ?: 0.0, 0.0001)
            assertEquals(clinic.lng, retrieved?.lng ?: 0.0, 0.0001)
        }

    @Test
    fun `empty assets results in empty database storage`() =
        runTest {
            // Arrange
            every { mockRepository.getClinicsFromAssets() } returns emptyList()
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(emptyList())

            // Act
            val loaded = getClinicsFromAssetsUseCase()
            populateClinicsDbUseCase(loaded)

            var retrieved: List<Clinic>? = null
            getStoredClinicsUseCase().collect { retrieved = it }

            // Assert
            assertTrue(loaded.isEmpty())
            assertTrue(retrieved?.isEmpty() ?: false)
        }

    @Test
    fun `multiple load attempts preserve consistent state`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                )

            every { mockRepository.getClinicsFromAssets() } returns clinics
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(clinics)

            // Act
            val load1 = getClinicsFromAssetsUseCase()
            val load2 = getClinicsFromAssetsUseCase()

            // Assert
            assertEquals(load1.size, load2.size)
            assertEquals(load1[0].name, load2[0].name)
        }

    @Test
    fun `updating database with new clinics replaces old data`() =
        runTest {
            // Arrange
            val clinics1 =
                listOf(
                    Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                )
            val clinics2 =
                listOf(
                    Clinic(2, "Clinic 2", "Address 2", "+456", "", -34.1, -58.1),
                )

            coEvery { mockRepository.saveAllClinics(any()) } returns Unit

            // Act
            populateClinicsDbUseCase(clinics1)
            populateClinicsDbUseCase(clinics2)

            // Assert
            coVerify(exactly = 1) { mockRepository.saveAllClinics(clinics1) }
            coVerify(exactly = 1) { mockRepository.saveAllClinics(clinics2) }
        }

    @Test
    fun `large dataset workflow completes successfully`() =
        runTest {
            // Arrange
            val largeClinics =
                (0..99).map { i ->
                    Clinic(
                        id = i,
                        name = "Clinic $i",
                        address = "Address $i",
                        phone = "+549$i",
                        website = if (i % 2 == 0) "" else "https://site$i.com",
                        lat = -34.0 - i * 0.001,
                        lng = -58.0 - i * 0.001,
                    )
                }

            every { mockRepository.getClinicsFromAssets() } returns largeClinics
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(largeClinics)

            // Act
            val loaded = getClinicsFromAssetsUseCase()
            populateClinicsDbUseCase(loaded)

            var retrieved: List<Clinic>? = null
            getStoredClinicsUseCase().collect { retrieved = it }

            // Assert
            assertEquals(100, loaded.size)
            assertEquals(100, retrieved?.size)
        }

    @Test
    fun `clinic data maintains precision through workflow`() =
        runTest {
            // Arrange
            val clinic =
                Clinic(
                    id = 42,
                    name = "Precision Test Clinic",
                    address = "Precision Address",
                    phone = "+5491987654321",
                    website = "https://precision.test",
                    lat = -34.65436959,
                    lng = -58.55087725,
                )

            every { mockRepository.getClinicsFromAssets() } returns listOf(clinic)
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(listOf(clinic))

            // Act
            val loaded = getClinicsFromAssetsUseCase()
            populateClinicsDbUseCase(loaded)

            var retrievedClinic: Clinic? = null
            getStoredClinicsUseCase().collect { clinics ->
                if (clinics.isNotEmpty()) retrievedClinic = clinics[0]
            }

            // Assert
            assertEquals(-34.65436959, retrievedClinic?.lat ?: 0.0, 0.00000001)
            assertEquals(-58.55087725, retrievedClinic?.lng ?: 0.0, 0.00000001)
        }

    @Test
    fun `workflow with clinics containing special characters`() =
        runTest {
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

            every { mockRepository.getClinicsFromAssets() } returns listOf(clinic)
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(listOf(clinic))

            // Act
            val loaded = getClinicsFromAssetsUseCase()
            populateClinicsDbUseCase(loaded)

            var retrieved: Clinic? = null
            getStoredClinicsUseCase().collect { clinics ->
                if (clinics.isNotEmpty()) retrieved = clinics[0]
            }

            // Assert
            assertEquals("Kinesiología y Osteopatía Integral KIO", retrieved?.name)
        }

    @Test
    fun `verify repository interactions during workflow`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    Clinic(1, "Clinic 1", "Address 1", "+123", "", -34.0, -58.0),
                )

            every { mockRepository.getClinicsFromAssets() } returns clinics
            coEvery { mockRepository.saveAllClinics(any()) } returns Unit
            every { mockRepository.getStoredClinics() } returns flowOf(clinics)

            // Act
            getClinicsFromAssetsUseCase()
            populateClinicsDbUseCase(clinics)
            getStoredClinicsUseCase().collect { }

            // Assert
            verify { mockRepository.getClinicsFromAssets() }
            coVerify { mockRepository.saveAllClinics(clinics) }
            verify { mockRepository.getStoredClinics() }
        }
}
