package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.repositories

import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.SessionDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.entities.SessionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RehabRepositoryImplTest {
    private val sessionDao = mockk<SessionDao>(relaxed = true)
    private val repository = RehabRepositoryImpl(sessionDao)

    @Test
    fun `getSessions should call sessionDao getSessionsByUser and map to domain model`() =
        runTest {
            val userId = "user_imanol"
            val mockEntities =
                listOf(
                    SessionEntity(
                        id = 1L,
                        userId = userId,
                        dateTimestamp = 123456789L,
                        durationSeconds = 600,
                        averageRom = 85f,
                        successfulReps = 10,
                    ),
                )
            every { sessionDao.getSessionsByUser(userId) } returns flowOf(mockEntities)

            repository.getSessions(userId).collect { sessions ->
                assertEquals(1, sessions.size)
                val session = sessions[0]
                assertEquals(1L, session.id)
                assertEquals(userId, session.userId)
                assertEquals(85f, session.averageRom)
            }

            coVerify { sessionDao.getSessionsByUser(userId) }
        }

    @Test
    fun `saveSession should call sessionDao insertSession after mapping to entity`() =
        runTest {
            val session =
                Session(
                    id = 1L,
                    userId = "user_imanol",
                    dateTimestamp = 123456789L,
                    durationSeconds = 600,
                    averageRom = 85f,
                    successfulReps = 10,
                )
            coEvery { sessionDao.insertSession(any()) } returns Unit

            repository.saveSession(session)

            coVerify { sessionDao.insertSession(match { it.id == 1L && it.userId == "user_imanol" }) }
        }
}
