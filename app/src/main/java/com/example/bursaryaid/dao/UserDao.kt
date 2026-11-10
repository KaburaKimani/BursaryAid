package com.example.bursaryaid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bursaryaid.model.UserAccount
import com.example.bursaryaid.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("SELECT * FROM user_accounts WHERE userId = :userId")
    suspend fun getUserById(userId: Long): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE userRole = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserAccount>>
}