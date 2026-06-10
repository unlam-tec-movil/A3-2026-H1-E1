package ar.edu.unlam.mobile.scaffolding.domain.usecase

import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
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
            exercise: Exercise,
        ): JointPrecision {
            // We compare against the endAngle as the target during the movement
            val diff = abs(currentAngle - exercise.endAngle)
            return when {
                diff <= exercise.toleranceIdeal -> JointPrecision.IDEAL
                diff <= exercise.toleranceWarning -> JointPrecision.WARNING
                else -> JointPrecision.ERROR
            }
        }

        /**
         * Logic to detect a completed repetition.
         * A rep is counted when the user goes from startAngle to endAngle and back.
         */
        fun checkRepetition(
            currentAngle: Float,
            startAngle: Float,
            endAngle: Float,
            wasAtTarget: Boolean,
        ): Pair<Boolean, Boolean> {
            val atTarget = abs(currentAngle - endAngle) < 15f
            val atStart = abs(currentAngle - startAngle) < 15f

            var completed = false
            var newWasAtTarget = wasAtTarget

            if (atTarget) {
                newWasAtTarget = true
            } else if (atStart && wasAtTarget) {
                completed = true
                newWasAtTarget = false
            }

            return Pair(completed, newWasAtTarget)
        }
    }
