package ar.edu.unlam.mobile.scaffolding.data.datasources.device.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LightSensorDataSource(
    context: Context,
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    fun getLuxFlow(): Flow<Float> =
        callbackFlow {
            val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.values?.get(0)?.let { trySend(it) }
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int,
                    ) {}
                }
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
}
