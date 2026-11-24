package com.example.bursaryaid.repository

import com.example.bursaryaid.dao.UserDao
import com.example.bursaryaid.model.UserAccount
import com.example.bursaryaid.model.UserRole
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {
    suspend fun insertUser(user: UserAccount): Long =
        userDao.insertUser(user)

    suspend fun updateUser(user: UserAccount): Int =
        userDao.updateUser(user)

    suspend fun getUserById(userId: Long): UserAccount? =
        userDao.getUserById(userId)

    suspend fun getUserByEmail(email: String): UserAccount? =
        userDao.getUserByEmail(email)

    suspend fun emailExists(email: String): Boolean =
        userDao.emailExists(email)

    fun getUsersByRole(role: UserRole): Flow<List<UserAccount>> =
        userDao.getUsersByRole(role)

    fun getAllUsers(): Flow<List<UserAccount>> =
        userDao.getAllUsers()
}
