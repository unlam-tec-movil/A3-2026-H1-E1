package ar.edu.unlam.mobile.scaffolding.domain.usecase

import kotlin.math.abs
import kotlin.math.atan2

class CalculateJointAngleUseCase {
    fun execute(
        firstPointX: Float,
        firstPointY: Float,
        midPointX: Float,
        midPointY: Float,
        lastPointX: Float,
        lastPointY: Float,
    ): Float {
        val radians =
            atan2(lastPointY - midPointY, lastPointX - midPointX) -
                atan2(firstPointY - midPointY, firstPointX - midPointX)
        var angle = abs(radians * 180.0 / Math.PI).toFloat()
        if (angle > 180f) {
            angle = 360f - angle
        }
        return angle
    }
}
