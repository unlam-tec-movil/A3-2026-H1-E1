package ar.edu.unlam.mobile.scaffolding.data.repositories

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.ExerciseDao
import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.SessionDao
import ar.edu.unlam.mobile.scaffolding.data.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.data.mappers.toEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RehabRepositoryImpl
    @Inject
    constructor(
        private val sessionDao: SessionDao,
        private val exerciseDao: ExerciseDao,
    ) : RehabRepository {
        override fun getSessions(userId: String): Flow<List<Session>> =
            sessionDao.getSessionsByUser(userId).map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun saveSession(session: Session) {
            sessionDao.insertSession(session.toEntity())
        }

        override fun getExercises(): Flow<List<Exercise>> =
            exerciseDao.getAllExercises().map { entities ->
                entities.map { it.toDomain() }
            }

        override fun getExerciseById(id: String): Flow<Exercise?> =
            exerciseDao.getExerciseById(id).map { entity -> entity?.toDomain() }

        override suspend fun insertExercises(exercises: List<Exercise>) {
            exerciseDao.insertExercises(exercises.map { it.toEntity() })
        }
    }
