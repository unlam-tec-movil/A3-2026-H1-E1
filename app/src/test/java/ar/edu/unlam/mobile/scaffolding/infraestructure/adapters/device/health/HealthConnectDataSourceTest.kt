package ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.device.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Percentage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class HealthConnectDataSourceTest {
    private val context = mockk<Context>(relaxed = true)
    private val healthConnectClient = mockk<HealthConnectClient>(relaxed = true)
    private val permissionController = mockk<PermissionController>(relaxed = true)
    private lateinit var dataSource: HealthConnectDataSource

    @Before
    fun setUp() {
        mockkObject(HealthConnectClient.Companion)
        mockkStatic(HealthConnectClient::class)
        every { HealthConnectClient.getSdkStatus(any()) } returns HealthConnectClient.SDK_AVAILABLE
        every { HealthConnectClient.getOrCreate(any()) } returns healthConnectClient
        every { healthConnectClient.permissionController } returns permissionController
        
        dataSource = HealthConnectDataSource(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isHealthConnectAvailable returns true when SDK_AVAILABLE`() {
        every { HealthConnectClient.getSdkStatus(any()) } returns HealthConnectClient.SDK_AVAILABLE
        assertTrue(dataSource.isHealthConnectAvailable())
    }

    @Test
    fun `isHealthConnectAvailable returns false when SDK_UNAVAILABLE`() {
        every { HealthConnectClient.getSdkStatus(any()) } returns HealthConnectClient.SDK_UNAVAILABLE
        assertFalse(dataSource.isHealthConnectAvailable())
    }

    @Test
    fun `hasAllPermissions returns true when all permissions are granted`() = runTest {
        val permissions = dataSource.permissions
        coEvery { permissionController.getGrantedPermissions() } returns permissions

        val result = dataSource.hasAllPermissions()
        assertTrue(result)
    }

    @Test
    fun `hasAllPermissions returns false when some permissions are missing`() = runTest {
        coEvery { permissionController.getGrantedPermissions() } returns setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )

        val result = dataSource.hasAllPermissions()
        assertFalse(result)
    }

    @Test
    fun `readSessionHealthData returns correct values when records are present`() = runTest {
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()

        // Mock HeartRateRecord response
        val heartRateRecords = listOf(
            HeartRateRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                samples = listOf(
                    HeartRateRecord.Sample(time = startTime, beatsPerMinute = 80),
                    HeartRateRecord.Sample(time = startTime.plusSeconds(30), beatsPerMinute = 90)
                ),
                metadata = Metadata()
            )
        )
        val hrResponse = mockk<ReadRecordsResponse<HeartRateRecord>>()
        every { hrResponse.records } returns heartRateRecords

        // Mock ActiveCaloriesBurnedRecord response
        val activeCaloriesRecords = listOf(
            ActiveCaloriesBurnedRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                energy = Energy.kilocalories(150.0),
                metadata = Metadata()
            ),
            ActiveCaloriesBurnedRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                energy = Energy.kilocalories(50.0),
                metadata = Metadata()
            )
        )
        val caloriesResponse = mockk<ReadRecordsResponse<ActiveCaloriesBurnedRecord>>()
        every { caloriesResponse.records } returns activeCaloriesRecords

        // Mock OxygenSaturationRecord response
        val oxygenRecords = listOf(
            OxygenSaturationRecord(
                time = startTime,
                zoneOffset = null,
                percentage = Percentage(98.0),
                metadata = Metadata()
            ),
            OxygenSaturationRecord(
                time = startTime.plusSeconds(60),
                zoneOffset = null,
                percentage = Percentage(96.0),
                metadata = Metadata()
            )
        )
        val oxygenResponse = mockk<ReadRecordsResponse<OxygenSaturationRecord>>()
        every { oxygenResponse.records } returns oxygenRecords

        coEvery {
            healthConnectClient.readRecords(match<ReadRecordsRequest<HeartRateRecord>> {
                getRecordType(it) == HeartRateRecord::class
            })
        } returns hrResponse

        coEvery {
            healthConnectClient.readRecords(match<ReadRecordsRequest<ActiveCaloriesBurnedRecord>> {
                getRecordType(it) == ActiveCaloriesBurnedRecord::class
            })
        } returns caloriesResponse

        coEvery {
            healthConnectClient.readRecords(match<ReadRecordsRequest<OxygenSaturationRecord>> {
                getRecordType(it) == OxygenSaturationRecord::class
            })
        } returns oxygenResponse

        val data = dataSource.readSessionHealthData(startTime, endTime)

        assertNotNull(data)
        assertEquals(85.0, data.averageHeartRate!!, 0.01)
        assertEquals(200.0, data.totalCaloriesBurned!!, 0.01)
        assertEquals(97.0, data.averageOxygenSaturation!!, 0.01)
    }

    @Test
    fun `readSessionHealthData returns null values when client is unavailable`() = runTest {
        every { HealthConnectClient.getSdkStatus(any()) } returns HealthConnectClient.SDK_UNAVAILABLE
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()

        val data = dataSource.readSessionHealthData(startTime, endTime)

        assertNotNull(data)
        assertNull(data.averageHeartRate)
        assertNull(data.totalCaloriesBurned)
        assertNull(data.averageOxygenSaturation)
    }

    private fun getRecordType(request: ReadRecordsRequest<*>): kotlin.reflect.KClass<*> {
        val field = ReadRecordsRequest::class.java.getDeclaredField("recordType")
        field.isAccessible = true
        return field.get(request) as kotlin.reflect.KClass<*>
    }
}
