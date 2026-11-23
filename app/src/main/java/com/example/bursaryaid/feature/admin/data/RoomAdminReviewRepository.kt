
package com.example.bursaryaid.feature.admin.data

import androidx.room.withTransaction

import com.example.bursaryaid.database.AppDatabase

import com.example.bursaryaid.feature.admin.logic.AdminApplicationDetail
import com.example.bursaryaid.feature.admin.logic.AdminApplicationItem
import com.example.bursaryaid.feature.admin.logic.AllocationInput
import com.example.bursaryaid.feature.admin.logic.AllocationRules
import com.example.bursaryaid.feature.admin.logic.DecisionResult
import com.example.bursaryaid.feature.admin.logic.EligibilityRules
import com.example.bursaryaid.feature.admin.logic.ReviewWorkflow
import com.example.bursaryaid.feature.admin.logic.toEligibilityInput

import com.example.bursaryaid.model.AllocationRecord
import com.example.bursaryaid.model.AppNotification
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.DecisionStatus
import com.example.bursaryaid.model.NotificationType

import com.example.bursaryaid.repository.AllocationRecordRepository
import com.example.bursaryaid.repository.AppNotificationRepository
import com.example.bursaryaid.repository.BursaryApplicationRepository
import com.example.bursaryaid.repository.BursaryRepository
import com.example.bursaryaid.repository.StudentProfileRepository
import com.example.bursaryaid.repository.UserRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private data class StudentInfo(
    val name: String,
    val admissionNumber: String,
    val institutionName: String,
    val courseName: String,
    val yearOfStudy: Int,
    val monthlyHouseholdIncomeKes: Long
)

class RoomAdminReviewRepository(
    private val database: AppDatabase,
    private val applicationRepository: BursaryApplicationRepository,
    private val profileRepository: StudentProfileRepository,
    private val userRepository: UserRepository,
    private val bursaryRepository: BursaryRepository,
    private val allocationRepository: AllocationRecordRepository,
    private val notificationRepository: AppNotificationRepository? = null,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : AdminReviewRepository {

    override fun observeApplications(): Flow<List<AdminApplicationItem>> =
        applicationRepository.getAllApplications().map { applications ->

            val students = mutableMapOf<Long, StudentInfo>()
            val bursaryNames = mutableMapOf<Long, String>()

            applications
                .filter {
                    it.applicationStatus != ApplicationStatus.DRAFT
                }
                .map { application ->

                    val student = students[application.studentId]
                        ?: loadStudent(application.studentId).also {
                            students[application.studentId] = it
                        }

                    val bursaryName = bursaryNames[application.bursaryId]
                        ?: (
                                bursaryRepository.getBursaryById(
                                    application.bursaryId
                                )?.bursaryName ?: "Unknown bursary"
                                ).also {
                                bursaryNames[application.bursaryId] = it
                            }

                    AdminApplicationItem(
                        applicationId = application.applicationId,
                        studentId = application.studentId,
                        studentName = student.name,
                        admissionNumber = student.admissionNumber,
                        bursaryId = application.bursaryId,
                        bursaryName = bursaryName,
                        requestedAmountKes = application.requestedAmountKes,
                        applicationStatus = application.applicationStatus,
                        submittedAtMillis = application.submittedAtMillis ?: 0L
                    )
                }
        }

    override suspend fun getApplicationDetail(
        applicationId: Long
    ): AdminApplicationDetail? = loadDetail(applicationId)

    override suspend fun startReview(
        applicationId: Long
    ): DecisionResult = database.withTransaction {

        val application =
            applicationRepository.getApplicationById(applicationId)
                ?: return@withTransaction DecisionResult.failure(
                    "Application not found."
                )

        if (!ReviewWorkflow.canStartReview(application.applicationStatus)) {
            return@withTransaction DecisionResult.failure(
                "Only submitted applications can be reviewed."
            )
        }

        applicationRepository.updateApplication(
            application.copy(
                applicationStatus = ApplicationStatus.UNDER_REVIEW,
                updatedAtMillis = clock()
            )
        )

        DecisionResult.success()
    }

    override suspend fun requestInformation(
        applicationId: Long,
        message: String,
        reviewedByUserId: Long
    ): DecisionResult = database.withTransaction {

        val application =
            applicationRepository.getApplicationById(applicationId)
                ?: return@withTransaction DecisionResult.failure(
                    "Application not found."
                )

        if (!ReviewWorkflow.canRequestInformation(application.applicationStatus)) {
            return@withTransaction DecisionResult.failure(
                "Information can only be requested during review."
            )
        }

        val cleanMessage = message.trim()

        if (cleanMessage.isBlank()) {
            return@withTransaction DecisionResult.failure(
                "Please provide a reason for requesting information."
            )
        }

        applicationRepository.updateApplication(
            application.copy(
                applicationStatus = ApplicationStatus.NEEDS_INFORMATION,
                updatedAtMillis = clock()
            )
        )

        notifyStudent(
            application.studentId,
            NotificationType.INFORMATION_REQUESTED,
            "More information needed",
            cleanMessage
        )

        DecisionResult.success()
    }

    override suspend fun approveApplication(
        applicationId: Long,
        awardedAmountKes: Long,
        remarks: String,
        reviewedByUserId: Long
    ): DecisionResult = database.withTransaction {

        val detail = loadDetail(applicationId)
            ?: return@withTransaction DecisionResult.failure(
                "Application not found."
            )

        val eligibility =
            EligibilityRules.check(detail.toEligibilityInput())

        val validation = AllocationRules.validate(
            AllocationInput(
                applicationStatus = detail.applicationStatus,
                isEligible = eligibility.isEligible,
                awardedAmountKes = awardedAmountKes,
                requestedAmountKes = detail.requestedAmountKes,
                maximumAwardKes = detail.maximumAwardKes,
                remainingBudgetKes = detail.remainingBudgetKes,
                hasApprovedAllocation = detail.hasApprovedAllocation
            )
        )

        if (!validation.isValid) {
            return@withTransaction DecisionResult.failure(
                validation.errors.joinToString("\n")
            )
        }

        val application =
            applicationRepository.getApplicationById(applicationId)
                ?: return@withTransaction DecisionResult.failure(
                    "Application not found."
                )

        val now = clock()

        allocationRepository.insertAllocation(
            AllocationRecord(
                applicationId = applicationId,
                bursaryId = application.bursaryId,
                studentId = application.studentId,
                allocatedAmount = awardedAmountKes.toDouble(),
                decisionStatus = DecisionStatus.APPROVED,
                remarks = remarks.trim(),
                allocatedAtMillis = now,
                reviewedByUserId = reviewedByUserId
            )
        )

        applicationRepository.updateApplication(
            application.copy(
                applicationStatus = ApplicationStatus.APPROVED,
                updatedAtMillis = now
            )
        )

        notifyStudent(
            application.studentId,
            NotificationType.ALLOCATION_CONFIRMED,
            "Bursary awarded",
            "Your application was approved for KSh %,d."
                .format(awardedAmountKes)
        )

        DecisionResult.success()
    }

    override suspend fun rejectApplication(
        applicationId: Long,
        reason: String,
        reviewedByUserId: Long
    ): DecisionResult = database.withTransaction {

        val application =
            applicationRepository.getApplicationById(applicationId)
                ?: return@withTransaction DecisionResult.failure(
                    "Application not found."
                )

        if (!ReviewWorkflow.canDecide(application.applicationStatus)) {
            return@withTransaction DecisionResult.failure(
                "Only applications under review can be rejected."
            )
        }

        val cleanReason = reason.trim()

        if (cleanReason.isBlank()) {
            return@withTransaction DecisionResult.failure(
                "A rejection reason is required."
            )
        }

        val now = clock()

        allocationRepository.insertAllocation(
            AllocationRecord(
                applicationId = applicationId,
                bursaryId = application.bursaryId,
                studentId = application.studentId,
                allocatedAmount = 0.0,
                decisionStatus = DecisionStatus.REJECTED,
                remarks = cleanReason,
                allocatedAtMillis = now,
                reviewedByUserId = reviewedByUserId
            )
        )

        applicationRepository.updateApplication(
            application.copy(
                applicationStatus = ApplicationStatus.REJECTED,
                updatedAtMillis = now
            )
        )

        notifyStudent(
            application.studentId,
            NotificationType.APPLICATION_STATUS_CHANGED,
            "Application rejected",
            cleanReason
        )

        DecisionResult.success()
    }

    private suspend fun loadDetail(
        applicationId: Long
    ): AdminApplicationDetail? {

        val application =
            applicationRepository.getApplicationById(applicationId)
                ?: return null

        val bursary =
            bursaryRepository.getBursaryById(application.bursaryId)
                ?: error("Bursary ${application.bursaryId} was not found.")

        val student = loadStudent(application.studentId)

        val approved =
            allocationRepository
                .getAllocationsByBursary(bursary.bursaryId)
                .first()
                .filter {
                    it.decisionStatus == DecisionStatus.APPROVED
                }

        return AdminApplicationDetail(
            applicationId = application.applicationId,
            applicationStatus = application.applicationStatus,
            requestedAmountKes = application.requestedAmountKes,
            justification = application.applicationReason,
            submittedAtMillis = application.submittedAtMillis ?: 0L,

            studentId = application.studentId,
            studentName = student.name,
            admissionNumber = student.admissionNumber,
            institutionName = student.institutionName,
            courseName = student.courseName,
            yearOfStudy = student.yearOfStudy,

            householdIncomeKes = student.monthlyHouseholdIncomeKes,

            bursaryId = bursary.bursaryId,
            bursaryName = bursary.bursaryName,
            applicationDeadlineMillis = bursary.applicationDeadlineMillis,

            // minimumIncomeKes is the maximum income threshold
            // according to the existing AllocationValidator.
            incomeLimitKes = bursary.minimumIncomeKes.takeIf {
                it > 0L
            },

            maximumAwardKes = bursary.maximumAwardKes,
            totalBudgetKes = bursary.totalBudgetKes,

            approvedAwardsKes = approved.map {
                it.allocatedAmount.toLong()
            },

            hasApprovedAllocation = approved.any {
                it.applicationId == applicationId
            }
        )
    }

    private suspend fun loadStudent(
        studentId: Long
    ): StudentInfo {

        val profile =
            profileRepository.getProfileById(studentId)

        val user =
            profile?.let {
                userRepository.getUserById(it.userId)
            }

        return StudentInfo(
            name = user?.fullName ?: "Unknown student",
            admissionNumber = profile?.registrationNumber ?: "-",
            institutionName = profile?.institutionName ?: "-",
            courseName = profile?.courseName ?: "-",
            yearOfStudy = profile?.yearOfStudy ?: 0,
            monthlyHouseholdIncomeKes =
                profile?.monthlyHouseholdIncomeKes ?: 0L
        )
    }

    private suspend fun notifyStudent(
        studentId: Long,
        type: NotificationType,
        title: String,
        message: String
    ) {

        val repository = notificationRepository ?: return

        val profile =
            profileRepository.getProfileById(studentId) ?: return

        repository.insertNotification(
            AppNotification(
                userId = profile.userId,
                title = title,
                message = message,
                type = type,
                createdAtMillis = clock()
            )
        )
    }
}
