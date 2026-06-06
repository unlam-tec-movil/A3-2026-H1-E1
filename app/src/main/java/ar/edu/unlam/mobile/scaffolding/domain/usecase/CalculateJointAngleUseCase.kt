package ar.edu.unlam.mobile.scaffolding.domain.usecase

import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2

class CalculateJointAngleUseCase
    @Inject
    constructor() {
        /**
         * Calcula el ángulo de una articulación central a partir de tres puntos.
         * Retorna el ángulo normalizado entre 0 y 180 grados.
         * Si alguno de los puntos es nulo, retorna 0f.
         */
        fun execute(
            firstPointX: Float?,
            firstPointY: Float?,
            midPointX: Float?,
            midPointY: Float?,
            lastPointX: Float?,
            lastPointY: Float?,
        ): Float {
            if (firstPointX == null ||
                firstPointY == null ||
                midPointX == null ||
                midPointY == null ||
                lastPointX == null ||
                lastPointY == null
            ) {
                return 0f
            }

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
