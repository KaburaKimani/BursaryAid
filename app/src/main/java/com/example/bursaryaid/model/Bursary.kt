package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bursaries")
data class Bursary(
    @PrimaryKey(autoGenerate = true)
    val bursaryId: Long = 0L,
    val title: String,
    val description: String,
    val totalBudget: Double,
    val remainingBudget: Double,
    val maxAmountPerStudent: Double,
    val applicationDeadlineMillis: Long,
    val isOpen: Boolean
)