package ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.repositories

import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import ar.edu.unlam.mobile.scaffolding.domain.repository.RehabRepository
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.daos.SessionDao
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.infraestructure.persistance.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RehabRepositoryImpl
    @Inject
    constructor(
        private val sessionDao: SessionDao,
    ) : RehabRepository {
        override fun getSessions(userId: String): Flow<List<Session>> =
            sessionDao.getSessionsByUser(userId).map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun saveSession(session: Session) {
            sessionDao.insertSession(session.toEntity())
        }
    }
