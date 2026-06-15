package ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.database.BaseIntegrationTest
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.AppClinicEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StoredClinicsDaoIntegrationTest : BaseIntegrationTest() {
    private val dao get() = database.clinicDao()

    @Test
    fun `insert and retrieve clinic`() =
        runTest {
            // Arrange
            val clinic =
                AppClinicEntity(
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
            val result = dao.getClinics().first()

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
                    AppClinicEntity(1, "Clinic A", "", "", "", 0.0, 0.0),
                    AppClinicEntity(2, "Clinic B", "", "", "", 0.0, 0.0),
                )

            // Act
            dao.insertAll(clinics)
            val count = dao.getClinicCount()

            // Assert
            Assert.assertEquals(2, count)
        }

    @Test
    fun `insert clinic with Spanish characters and verify integrity`() =
        runTest {
            // Arrange
            val clinic =
                AppClinicEntity(
                    id = 10,
                    name = "Clínica de Rehabilitación e Investigación",
                    address = "Av. de Mayo 1370, 5° piso",
                    phone = "011 4381-6000",
                    website = "https://clinica.com.ar",
                    lat = -34.6091,
                    lng = -58.3831,
                )

            // Act
            dao.insertClinic(clinic)
            val result = dao.getClinics().first().find { it.id == 10 }

            // Assert
            Assert.assertNotNull(result)
            Assert.assertEquals("Clínica de Rehabilitación e Investigación", result?.name)
            Assert.assertEquals("Av. de Mayo 1370, 5° piso", result?.address)
        }

    @Test
    fun `coordinate precision is preserved`() =
        runTest {
            // Arrange
            val lat = -34.64666797
            val lng = -58.5721729
            val clinic =
                AppClinicEntity(
                    id = 20,
                    name = "Precision Test",
                    address = "",
                    phone = "",
                    website = "",
                    lat = lat,
                    lng = lng,
                )

            // Act
            dao.insertClinic(clinic)
            val result = dao.getClinics().first().first { it.id == 20 }

            // Assert
            Assert.assertEquals(lat, result.lat, 0.000000001)
            Assert.assertEquals(lng, result.lng, 0.000000001)
        }

    @Test
    fun `insertClinic with same id replaces existing entry`() =
        runTest {
            // Arrange
            val original = AppClinicEntity(1, "Original", "", "", "", 0.0, 0.0)
            val replacement = AppClinicEntity(1, "Replacement", "New Address", "", "", 1.1, 1.1)

            // Act
            dao.insertClinic(original)
            dao.insertClinic(replacement)
            val result = dao.getClinics().first()

            // Assert
            Assert.assertEquals(1, result.size)
            Assert.assertEquals("Replacement", result[0].name)
            Assert.assertEquals("New Address", result[0].address)
        }

    @Test
    fun `property-based test for random clinics`() =
        runTest {
            // We run 50 iterations to keep the test fast but thorough
            checkAll(
                iterations = 50,
                Arb.int(1..100000), // Random ID
                Arb.string(minSize = 1, maxSize = 100), // Random Name
                Arb.double(-90.0, 90.0), // Random Latitude
                Arb.double(-180.0, 180.0), // Random Longitude
            ) { id, name, lat, lng ->
                // Arrange
                val clinic =
                    AppClinicEntity(
                        id = id,
                        name = name,
                        address = "Random Address ${Arb.string(5).next()}",
                        phone = "123456",
                        website = "https://random.com",
                        lat = lat,
                        lng = lng,
                    )

                // Act
                dao.insertClinic(clinic)
                val result = dao.getClinics().first().find { it.id == id }

                // Assert
                Assert.assertNotNull("Clinic with id $id should be found", result)
                Assert.assertEquals(name, result?.name)
                Assert.assertEquals(lat, result?.lat ?: 0.0, 0.000001)
                Assert.assertEquals(lng, result?.lng ?: 0.0, 0.000001)
            }
        }
}
