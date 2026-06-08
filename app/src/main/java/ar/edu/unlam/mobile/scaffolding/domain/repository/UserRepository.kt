package ar.edu.unlam.mobile.scaffolding.domain.repository

import ar.edu.unlam.mobile.scaffolding.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<User?>

    suspend fun getUserByEmail(email: String): User?

    suspend fun saveUser(user: User)

    suspend fun clearUser()
}
