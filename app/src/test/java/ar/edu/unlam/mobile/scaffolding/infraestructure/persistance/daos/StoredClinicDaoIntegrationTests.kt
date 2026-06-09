package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos

import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.db.BaseIntegrationTest
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.ClinicEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StoredClinicsDaoIntegrationTest : BaseIntegrationTest() {
    private val dao get() = database.getStoredClinicsDao()

    @Test
    fun `insert and retrieve clinic`() =
        runTest {
            // Arrange
            val clinic =
                ClinicEntity(
                    id = 1,
                    name = "Test Clinic",
                    address = "123 Street",
                    phone = "555-0123",
                    website = "www.test.com",
                    lat = -34.6,
                    lng = -58.5,
                )

            // Act
            dao.insertClinic(clinic)
            val result = dao.getStoredClinics().first()

            // Assert
            Assert.assertEquals(1, result.size)
            Assert.assertEquals("Test Clinic", result[0].name)
        }

    @Test
    fun `insertAll and count clinics`() =
        runTest {
            // Arrange
            val clinics =
                listOf(
                    ClinicEntity(1, "Clinic A", "", "", "", 0.0, 0.0),
                    ClinicEntity(2, "Clinic B", "", "", "", 0.0, 0.0),
                )

            // Act
            dao.insertAll(clinics)
            val count = dao.getClinicCount()

            // Assert
            Assert.assertEquals(2, count)
        }
}
