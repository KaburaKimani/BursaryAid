package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allocation_records")
data class AllocationRecord(
    @PrimaryKey(autoGenerate = true)
    val allocationId: Long = 0L,
    val applicationId: Long,
    val bursaryId: Long,
    val studentId: Long,
    val allocatedAmount: Double,
    val decisionStatus: DecisionStatus,
    val remarks: String,
    val allocatedAtMillis: Long
)