package ar.edu.unlam.mobile.scaffolding.domain.usecase

import javax.inject.Inject
import kotlin.math.abs

enum class JointPrecision {
    IDEAL,
    WARNING,
    ERROR,
}

class SyncMotorUseCase
    @Inject
    constructor() {
        fun execute(
            currentAngle: Float,
            targetAngle: Float,
        ): JointPrecision {
            val diff = abs(currentAngle - targetAngle)
            return when {
                diff <= 15f -> JointPrecision.IDEAL
                diff <= 30f -> JointPrecision.WARNING
                else -> JointPrecision.ERROR
            }
        }
    }
