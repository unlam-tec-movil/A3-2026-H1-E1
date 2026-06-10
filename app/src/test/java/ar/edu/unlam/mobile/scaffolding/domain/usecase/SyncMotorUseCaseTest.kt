package ar.edu.unlam.mobile.scaffolding.domain.usecase

import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncMotorUseCaseTest {
    private val useCase = SyncMotorUseCase()
    private val mockExercise =
        Exercise(
            id = "test",
            name = "Test",
            description = "Test",
            targetJoints = emptyList(),
            startAngle = 180f,
            endAngle = 90f,
            toleranceIdeal = 5f,
            toleranceWarning = 15f,
            repetitions = 10,
            sets = 3,
            bodyPart = "Test",
        )

    @Test
    fun `when difference is 2 then precision is IDEAL`() {
        val result = useCase.execute(92f, mockExercise)
        assertEquals(JointPrecision.IDEAL, result)
    }

    @Test
    fun `when difference is 10 then precision is WARNING`() {
        val result = useCase.execute(100f, mockExercise)
        assertEquals(JointPrecision.WARNING, result)
    }

    @Test
    fun `when difference is 20 then precision is ERROR`() {
        val result = useCase.execute(115f, mockExercise)
        assertEquals(JointPrecision.ERROR, result)
    }

    @Test
    fun `repetition logic - full cycle`() {
        var wasAtTarget = false

        // Start position
        var result = useCase.checkRepetition(180f, 180f, 90f, wasAtTarget)
        assertEquals(false, result.first) // Not completed
        wasAtTarget = result.second // false

        // Reaching target
        result = useCase.checkRepetition(90f, 180f, 90f, wasAtTarget)
        assertEquals(false, result.first)
        wasAtTarget = result.second // true

        // Moving back to start
        result = useCase.checkRepetition(180f, 180f, 90f, wasAtTarget)
        assertEquals(true, result.first) // Completed!
        wasAtTarget = result.second // false
    }
}
