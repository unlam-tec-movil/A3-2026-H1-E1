package ar.edu.unlam.mobile.scaffolding.domain.usecase

import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PoseMathTest {
    private lateinit var angleUseCase: CalculateJointAngleUseCase
    private lateinit var syncMotorUseCase: SyncMotorUseCase

    private val mockExercise =
        Exercise(
            id = "1",
            name = "Bicep Curl",
            description = "Test",
            targetJoints = listOf("RIGHT_SHOULDER", "RIGHT_ELBOW", "RIGHT_WRIST"),
            startAngle = 180f,
            endAngle = 30f,
            toleranceIdeal = 10f,
            toleranceWarning = 25f,
            repetitions = 5,
            sets = 1,
            bodyPart = "ARM",
        )

    @Before
    fun setUp() {
        angleUseCase = CalculateJointAngleUseCase()
        syncMotorUseCase = SyncMotorUseCase()
    }

    @Test
    fun `angle calculation for 90 degrees`() {
        // Points forming a perfect L
        val angle =
            angleUseCase.execute(
                0f,
                100f, // Point 1 (Top)
                0f,
                0f, // Mid Point (Corner)
                100f,
                0f, // Point 2 (Right)
            )
        assertEquals(90f, angle, 0.1f)
    }

    @Test
    fun `angle calculation for 180 degrees`() {
        // Straight line
        val angle =
            angleUseCase.execute(
                -100f,
                0f,
                0f,
                0f,
                100f,
                0f,
            )
        assertEquals(180f, angle, 0.1f)
    }

    @Test
    fun `precision feedback ranges`() {
        // IDEAL (within 10 deg of target 30)
        assertEquals(JointPrecision.IDEAL, syncMotorUseCase.execute(35f, mockExercise))

        // WARNING (within 25 deg but more than 10)
        assertEquals(JointPrecision.WARNING, syncMotorUseCase.execute(50f, mockExercise))

        // ERROR (more than 25 deg diff)
        assertEquals(JointPrecision.ERROR, syncMotorUseCase.execute(70f, mockExercise))
    }

    @Test
    fun `repetition cycle detection`() {
        var wasAtTarget = false

        // 1. User starts at 180 (Start position)
        var result = syncMotorUseCase.checkRepetition(180f, 180f, 30f, wasAtTarget)
        assertEquals(false, result.first) // Not completed
        wasAtTarget = result.second // still false

        // 2. User moves to 30 (Target position)
        result = syncMotorUseCase.checkRepetition(30f, 180f, 30f, wasAtTarget)
        assertEquals(false, result.first)
        wasAtTarget = result.second // now true

        // 3. User moves back to 180 (Full rep)
        result = syncMotorUseCase.checkRepetition(180f, 180f, 30f, wasAtTarget)
        assertEquals(true, result.first) // Rep Counted!
        assertEquals(false, result.second) // Reset target flag
    }
}
