package ar.edu.unlam.mobile.scaffolding.data.datasources.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ar.edu.unlam.mobile.scaffolding.application.port.out.local.sensor.MeasurableSensorPort

abstract class MeasurableSensorImpl(
    private val context: Context,
    private val sensorFeature: String,
    sensorType: Int,
) : MeasurableSensorPort(sensorType),
    SensorEventListener {
    override val sensorExists: Boolean
        get() = context.packageManager.hasSystemFeature(sensorFeature)

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    override fun startListening() {
        if (!sensorExists) {
            return
        }
        if (!::sensorManager.isInitialized) {
            sensorManager = context.getSystemService(SensorManager::class.java) as SensorManager
            sensor = sensorManager.getDefaultSensor(sensorType)
        }
        sensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun stopListening() {
        if (!sensorExists || !::sensorManager.isInitialized) {
            return
        }
        sensor?.let {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!sensorExists) {
            return
        }

        if (event?.sensor?.type == sensorType) {
            onSensorValueChanged?.invoke(event.values.toList())
        }
    }

    override fun onAccuracyChanged(
        event: Sensor?,
        p1: Int,
    ) = Unit
}
