package ar.edu.unlam.mobile.scaffolding.domain.repository

import ar.edu.unlam.mobile.scaffolding.domain.model.Exercise
import ar.edu.unlam.mobile.scaffolding.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface RehabRepository {
    fun getSessions(userId: String): Flow<List<Session>>

    suspend fun saveSession(session: Session)

    fun getExercises(): Flow<List<Exercise>>

    suspend fun insertExercises(exercises: List<Exercise>)
}
