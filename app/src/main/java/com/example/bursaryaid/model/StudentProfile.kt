package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey(autoGenerate = true)
    val studentId: Long = 0L,
    val userId: Long,
    val registrationNumber: String,
    val institutionName: String,
    val courseName: String,
    val yearOfStudy: Int,
    val householdSize: Int = 1,
    val monthlyHouseholdIncomeKes: Long = 0L,
    val guardianName: String = "",
    val guardianPhoneNumber: String = "",
    val profileUpdatedAtMillis: Long = System.currentTimeMillis()
)
