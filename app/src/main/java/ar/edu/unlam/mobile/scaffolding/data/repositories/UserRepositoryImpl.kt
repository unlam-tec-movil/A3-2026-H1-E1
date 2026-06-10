package ar.edu.unlam.mobile.scaffolding.data.repositories

import ar.edu.unlam.mobile.scaffolding.data.datasources.local.dao.UserDao
import ar.edu.unlam.mobile.scaffolding.data.mappers.toDomain
import ar.edu.unlam.mobile.scaffolding.data.mappers.toEntity
import ar.edu.unlam.mobile.scaffolding.domain.model.User
import ar.edu.unlam.mobile.scaffolding.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl
    @Inject
    constructor(
        private val userDao: UserDao,
    ) : UserRepository {
        override fun getUser(): Flow<User?> =
            userDao.getUser().map { entity ->
                entity?.toDomain()
            }

        override suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)?.toDomain()

        override suspend fun saveUser(user: User) {
            userDao.insertUser(user.toEntity())
        }

        override suspend fun clearUser() {
            userDao.clearUser()
        }
    }
