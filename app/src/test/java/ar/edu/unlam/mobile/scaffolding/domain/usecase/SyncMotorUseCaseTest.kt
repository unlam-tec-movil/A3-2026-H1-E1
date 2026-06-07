package ar.edu.unlam.mobile.scaffolding.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncMotorUseCaseTest {
    private val useCase = SyncMotorUseCase()

    @Test
    fun `when difference is 10 then precision is IDEAL`() {
        val result = useCase.execute(90f, 100f)
        assertEquals(JointPrecision.IDEAL, result)
    }

    @Test
    fun `when difference is 15 then precision is IDEAL`() {
        val result = useCase.execute(90f, 105f)
        assertEquals(JointPrecision.IDEAL, result)
    }

    @Test
    fun `when difference is 20 then precision is WARNING`() {
        val result = useCase.execute(90f, 110f)
        assertEquals(JointPrecision.WARNING, result)
    }

    @Test
    fun `when difference is 30 then precision is WARNING`() {
        val result = useCase.execute(90f, 120f)
        assertEquals(JointPrecision.WARNING, result)
    }

    @Test
    fun `when difference is 35 then precision is ERROR`() {
        val result = useCase.execute(90f, 125f)
        assertEquals(JointPrecision.ERROR, result)
    }
}
