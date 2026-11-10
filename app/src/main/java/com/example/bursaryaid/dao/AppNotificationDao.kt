package com.example.bursaryaid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bursaryaid.model.AppNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Update
    suspend fun updateNotification(notification: AppNotification)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun getNotificationsByUserId(userId: Long): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCountByUserId(userId: Long): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Long)
}