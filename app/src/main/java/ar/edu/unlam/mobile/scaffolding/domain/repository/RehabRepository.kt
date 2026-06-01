package ar.edu.unlam.mobile.scaffolding.domain.repository

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

interface RehabRepository {
    fun getSessions(userId: String): Flow<List<SessionEntity>>

    suspend fun saveSession(session: SessionEntity)
}
