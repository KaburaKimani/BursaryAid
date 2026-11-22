package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bursary_applications")
data class BursaryApplication(
    @PrimaryKey(autoGenerate = true)
    val applicationId: Long = 0L,
    val bursaryId: Long,
    val studentId: Long,
    val academicYear: String = "",
    val requestedAmountKes: Long = 0L,
    val applicationStatus: ApplicationStatus = ApplicationStatus.DRAFT,
    val applicationReason: String = "",
    val supportingDocuments: List<String> = emptyList(),
    val submittedAtMillis: Long? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
