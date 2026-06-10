package ar.edu.unlam.mobile.scaffolding.domain.model

/**
 * Lectura raw del acelerómetro en los tres ejes (m/s²).
 * @param magnitude magnitud total del vector de aceleración √(x²+y²+z²)
 */
data class SensorReading(
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
)
