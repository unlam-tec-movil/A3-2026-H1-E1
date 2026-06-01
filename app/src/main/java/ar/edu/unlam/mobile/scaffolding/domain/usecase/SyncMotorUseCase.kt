package ar.edu.unlam.mobile.scaffolding.domain.usecase

import kotlin.math.abs

class SyncMotorUseCase {
    enum class Feedback { IDEAL, WARNING, ERROR }

    fun execute(
        measuredAngle: Float,
        targetAngle: Float,
    ): Feedback {
        val difference = abs(measuredAngle - targetAngle)
        return when {
            difference <= 15f -> Feedback.IDEAL // Tolerancia óptima (Verde)
            difference <= 30f -> Feedback.WARNING // Tolerancia moderada (Amarillo)
            else -> Feedback.ERROR // Fuera de rango (Rojo)
        }
    }
}
