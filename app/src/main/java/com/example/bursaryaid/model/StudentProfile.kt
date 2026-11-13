package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey(autoGenerate = true)
    val studentId: Long = 0L,
    val userId: Long,
    val admissionNumber: String,
    val institutionName: String,
    val courseOfStudy: String,
    val yearOfStudy: Int,
    val countyOfOrigin: String,
    val familyIncomeLevel: String,
    val isOrphaned: Boolean,
    val isDisabled: Boolean
)