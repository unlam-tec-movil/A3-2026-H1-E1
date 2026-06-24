package ar.edu.unlam.mobile.scaffolding.application.port.out.local.sensor

abstract class MeasurableSensorPort(
    protected val sensorType: Int,
) {
    abstract val sensorExists: Boolean
    protected var onSensorValueChanged: ((List<Float>) -> Unit)? = null

    abstract fun startListening()

    abstract fun stopListening()

    public fun setOnSensorValueChangedListener(listenerEvent: (List<Float>) -> Unit) {
        onSensorValueChanged = listenerEvent
    }
}
