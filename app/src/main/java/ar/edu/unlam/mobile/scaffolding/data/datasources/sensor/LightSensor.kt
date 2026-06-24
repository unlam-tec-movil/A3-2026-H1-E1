package ar.edu.unlam.mobile.scaffolding.data.datasources.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor

class LightSensor(
    context: Context,
) : MeasurableSensorImpl(
        context = context,
        sensorFeature = PackageManager.FEATURE_SENSOR_LIGHT,
        sensorType = Sensor.TYPE_LIGHT,
    )
