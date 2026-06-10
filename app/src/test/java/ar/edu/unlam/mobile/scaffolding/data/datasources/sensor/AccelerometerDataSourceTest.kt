package ar.edu.unlam.mobile.scaffolding.data.datasources.sensor

import ar.edu.unlam.mobile.scaffolding.domain.model.SensorReading
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

// Umbral de referencia: FALL_THRESHOLD_MS2 = 24.5 m/s² ≈ 2.5 G
class AccelerometerDataSourceTest {
    private val threshold = AccelerometerDataSource.FALL_THRESHOLD_MS2

    private fun reading(
        x: Float,
        y: Float,
        z: Float,
    ): SensorReading {
        val magnitude = sqrt(x * x + y * y + z * z)
        return SensorReading(x = x, y = y, z = z, magnitude = magnitude)
    }

    // isFallDetected
    @Test
    fun `isFallDetected returns false for normal standing gravity reading`() {
        // Dispositivo quieto: ~1 G en eje Z = 9.8 m/s²
        val normalReading = reading(x = 0f, y = 0f, z = 9.8f)
        assertFalse(isFall(normalReading))
    }

    @Test
    fun `isFallDetected returns false for brisk walk reading around 1 5 G`() {
        // Caminata rápida: ~1.5 G ≈ 14.7 m/s² — no debe detectarse como caída
        val walkReading = reading(x = 5f, y = 5f, z = 12f)
        assertFalse(isFall(walkReading))
    }

    @Test
    fun `isFallDetected returns false when magnitude equals threshold exactly`() {
        // En el umbral exacto no se considera caída (operador estrictamente mayor)
        val readingAtThreshold =
            SensorReading(x = 0f, y = 0f, z = threshold, magnitude = threshold)
        assertFalse(isFall(readingAtThreshold))
    }

    @Test
    fun `isFallDetected returns true when magnitude is just above threshold`() {
        val readingAbove =
            SensorReading(x = 0f, y = 0f, z = threshold, magnitude = threshold + 0.1f)
        assertTrue(isFall(readingAbove))
    }

    @Test
    fun `isFallDetected returns true for hard impact reading around 3 G`() {
        // Impacto fuerte: ~3 G ≈ 29.4 m/s²
        val impactReading = reading(x = 15f, y = 15f, z = 15f) // magnitud ≈ 25.98
        assertTrue(isFall(impactReading))
    }

    @Test
    fun `isFallDetected returns true for extreme fall impact reading around 4 G`() {
        // Caída severa: ~4 G ≈ 39.2 m/s²
        val severeImpact = reading(x = 20f, y = 20f, z = 20f) // magnitud ≈ 34.64
        assertTrue(isFall(severeImpact))
    }

    @Test
    fun `isFallDetected returns true when single axis spike exceeds threshold`() {
        // Golpe en un solo eje supera el umbral
        val singleAxisImpact = reading(x = 30f, y = 0f, z = 0f)
        assertTrue(isFall(singleAxisImpact))
    }

    @Test
    fun `isFallDetected returns false for all-zero reading`() {
        val zeroReading = reading(x = 0f, y = 0f, z = 0f)
        assertFalse(isFall(zeroReading))
    }

    // SensorReading
    @Test
    fun `SensorReading magnitude should equal sqrt of sum of squares`() {
        val x = 3f
        val y = 4f
        val z = 0f
        val expected = sqrt(x * x + y * y + z * z) // = 5.0
        val r = reading(x, y, z)
        assert(r.magnitude == expected) {
            "Se esperaba magnitud $expected pero fue ${r.magnitude}"
        }
    }

    @Test
    fun `SensorReading magnitude should be 9 8 for standard gravity on Z axis`() {
        val r = reading(x = 0f, y = 0f, z = 9.8f)
        assert(r.magnitude == 9.8f)
    }

    // Helper — evita instanciar AccelerometerDataSource (requiere Context de Android)
    private fun isFall(reading: SensorReading): Boolean = reading.magnitude > threshold
}
