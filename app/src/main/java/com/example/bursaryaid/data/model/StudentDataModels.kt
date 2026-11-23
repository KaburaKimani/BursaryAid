package com.example.bursaryaid.data.model

import com.example.bursaryaid.model.AppConstants
import com.example.bursaryaid.model.ApplicationStatus
import kotlinx.coroutines.flow.Flow

typealias ApplicationStatus = ApplicationStatus

object AppConstants {
    const val DATABASE_NAME = AppConstants.DATABASE_NAME
    const val CURRENCY_CODE = AppConstants.CURRENCY_CODE
    const val MIN_YEAR_OF_STUDY = AppConstants.MIN_YEAR_OF_STUDY
    const val MAX_YEAR_OF_STUDY = AppConstants.MAX_YEAR_OF_STUDY
    const val MAX_APPLICATIONS_PER_BURSARY = AppConstants.MAX_APPLICATIONS_PER_BURSARY
}

data class StudentProfile(
    val studentId: Long = 0L,
    val userId: Long,
    val registrationNumber: String,
    val institutionName: String,
    val courseName: String,
    val yearOfStudy: Int,
    val householdSize: Int,
    val monthlyHouseholdIncomeKes: Long,
    val guardianName: String = "",
    val guardianPhoneNumber: String = "",
    val profileUpdatedAtMillis: Long
)

data class BursaryApplication(
    val applicationId: Long = 0L,
    val studentId: Long,
    val bursaryId: Long,
    val academicYear: String,
    val requestedAmountKes: Long,
    val applicationStatus: ApplicationStatus,
    val applicationReason: String,
    val supportingDocuments: List<String> = emptyList(),
    val submittedAtMillis: Long? = null,
    val updatedAtMillis: Long
)

data class BursaryOpportunity(
    val bursaryId: Long,
    val bursaryName: String,
    val fundingOrganization: String,
    val maximumAwardKes: Long,
    val applicationDeadlineMillis: Long,
    val isBursaryOpen: Boolean
)

interface StudentProfileRepository {
    suspend fun getStudentProfile(userId: Long): StudentProfile?
    suspend fun insertStudentProfile(profile: StudentProfile): Long
    suspend fun updateStudentProfile(profile: StudentProfile)
}

interface ApplicationRepository {
    suspend fun getApplicationById(applicationId: Long): BursaryApplication?
    fun getApplicationsByStudent(studentId: Long): Flow<List<BursaryApplication>>
    suspend fun insertApplication(application: BursaryApplication): Long
    suspend fun updateApplication(application: BursaryApplication)
    suspend fun deleteApplication(applicationId: Long)
}

interface BursaryRepository {
    fun getOpenBursaries(): Flow<List<BursaryOpportunity>>
}
