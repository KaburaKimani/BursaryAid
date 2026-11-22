package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bursaries")
data class Bursary(
    @PrimaryKey(autoGenerate = true)
    val bursaryId: Long = 0L,
    val bursaryName: String,
    val bursaryDescription: String,
    val fundingOrganization: String = "",
    val totalBudgetKes: Long = 0L,
    val minimumIncomeKes: Long = 0L,
    val maximumAwardKes: Long = 0L,
    val applicationDeadlineMillis: Long,
    val isBursaryOpen: Boolean = true
)
