package com.example.bursaryaid.repository

import com.example.bursaryaid.dao.AppNotificationDao
import com.example.bursaryaid.model.AppNotification
import kotlinx.coroutines.flow.Flow

class AppNotificationRepository(private val appNotificationDao: AppNotificationDao) {
    suspend fun insertNotification(notification: AppNotification): Long =
        appNotificationDao.insertNotification(notification)

    suspend fun updateNotification(notification: AppNotification) =
        appNotificationDao.updateNotification(notification)

    fun getNotificationsByUserId(userId: Long): Flow<List<AppNotification>> =
        appNotificationDao.getNotificationsByUserId(userId)

    fun getUnreadCountByUserId(userId: Long): Flow<Int> =
        appNotificationDao.getUnreadCountByUserId(userId)

    suspend fun markAllAsRead(userId: Long) =
        appNotificationDao.markAllAsRead(userId)
}