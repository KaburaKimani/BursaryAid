package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val notificationId: Long = 0L,
    val userId: Long,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAtMillis: Long
)