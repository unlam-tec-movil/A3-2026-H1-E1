package ar.edu.unlam.mobile.scaffolding.infraestructure.adapters.device.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ar.edu.unlam.mobile.scaffolding.domain.model.SensorReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class AccelerometerDataSource(
    context: Context,
) {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    companion object {
        // Umbral de magnitud (m/s²) a partir del cual se clasifica como caída / impacto brusco.
        const val FALL_THRESHOLD_MS2: Float = 24.5f // ≈ 2.5 G
    }

    fun getReadingsFlow(): Flow<SensorReading> =
        callbackFlow {
            if (accelerometerSensor == null) {
                close(UnsupportedOperationException("Este dispositivo no cuenta con acelerómetro"))
                return@callbackFlow
            }

            val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event ?: return
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val magnitude = sqrt(x * x + y * y + z * z)
                        trySend(SensorReading(x = x, y = y, z = z, magnitude = magnitude))
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int,
                    ) {}
                }

            sensorManager.registerListener(
                listener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME,
            )

            awaitClose { sensorManager.unregisterListener(listener) }
        }

    fun isFallDetected(reading: SensorReading): Boolean = reading.magnitude > FALL_THRESHOLD_MS2
}
