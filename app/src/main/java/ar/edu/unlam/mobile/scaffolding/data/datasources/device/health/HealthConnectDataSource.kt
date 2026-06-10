package ar.edu.unlam.mobile.scaffolding.data.datasources.device.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

data class HealthSessionData(
    val averageHeartRate: Double?,
    val totalCaloriesBurned: Double?,
    val averageOxygenSaturation: Double?,
)

class HealthConnectDataSource(
    private val context: Context,
) {
    val permissions =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        )

    fun isHealthConnectAvailable(): Boolean =
        try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }

    private fun getClient(): HealthConnectClient? =
        if (isHealthConnectAvailable()) {
            try {
                HealthConnectClient.getOrCreate(context)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

    suspend fun hasAllPermissions(): Boolean {
        val client = getClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }

    fun getPermissionRequestContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun readSessionHealthData(
        startTime: Instant,
        endTime: Instant,
    ): HealthSessionData {
        val client = getClient()
        if (client == null) {
            return HealthSessionData(
                averageHeartRate = null,
                totalCaloriesBurned = null,
                averageOxygenSaturation = null,
            )
        }

        val averageHeartRate =
            try {
                val response =
                    client.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        ),
                    )
                val samples = response.records.flatMap { it.samples }
                if (samples.isNotEmpty()) {
                    samples.map { it.beatsPerMinute.toDouble() }.average()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

        val totalCaloriesBurned =
            try {
                val response =
                    client.readRecords(
                        ReadRecordsRequest(
                            recordType = ActiveCaloriesBurnedRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        ),
                    )
                if (response.records.isNotEmpty()) {
                    response.records.sumOf { it.energy.inKilocalories }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

        val averageOxygenSaturation =
            try {
                val response =
                    client.readRecords(
                        ReadRecordsRequest(
                            recordType = OxygenSaturationRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                        ),
                    )
                if (response.records.isNotEmpty()) {
                    response.records.map { it.percentage.value }.average()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

        return HealthSessionData(
            averageHeartRate = averageHeartRate,
            totalCaloriesBurned = totalCaloriesBurned,
            averageOxygenSaturation = averageOxygenSaturation,
        )
    }
}
