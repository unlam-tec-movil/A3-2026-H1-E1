package ar.edu.unlam.mobile.scaffolding.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateJointAngleUseCaseTest {
    private lateinit var useCase: CalculateJointAngleUseCase

    @Before
    fun setUp() {
        useCase = CalculateJointAngleUseCase()
    }

    @Test
    fun `execute returns 90 degrees for a right angle`() {
        // Hombro (0, 1), Codo (0, 0), Muñeca (1, 0) -> 90 grados
        val angle = useCase.execute(0f, 1f, 0f, 0f, 1f, 0f)
        assertEquals(90f, angle, 0.01f)
    }

    @Test
    fun `execute returns 180 degrees for a straight line`() {
        // Hombro (-1, 0), Codo (0, 0), Muñeca (1, 0) -> 180 grados
        val angle = useCase.execute(-1f, 0f, 0f, 0f, 1f, 0f)
        assertEquals(180f, angle, 0.01f)
    }

    @Test
    fun `execute returns 0 degrees for overlapping points`() {
        // Hombro (1, 0), Codo (0, 0), Muñeca (1, 0) -> 0 grados
        val angle = useCase.execute(1f, 0f, 0f, 0f, 1f, 0f)
        assertEquals(0f, angle, 0.01f)
    }

    @Test
    fun `execute returns 0 if any point is null`() {
        val angle = useCase.execute(null, 1f, 0f, 0f, 1f, 0f)
        assertEquals(0f, angle, 0.01f)
    }

    @Test
    fun `execute returns normalized value between 0 and 180`() {
        // Un ángulo que daría > 180 sin normalizar
        // Hombro (1, 1), Codo (0, 0), Muñeca (1, -1) -> ángulo obtuso por fuera, agudo por dentro
        val angle = useCase.execute(1f, 1f, 0f, 0f, 1f, -1f)
        assertEquals(90f, angle, 0.01f)
    }
}
