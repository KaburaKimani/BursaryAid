package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bursary_applications")
data class BursaryApplication(
    @PrimaryKey(autoGenerate = true)
    val applicationId: Long = 0L,
    val bursaryId: Long,
    val studentId: Long,
    val requestedAmount: Double,
    val justificationReason: String,
    val applicationStatus: ApplicationStatus,
    val submittedAtMillis: Long,
    val lastUpdatedAtMillis: Long
)