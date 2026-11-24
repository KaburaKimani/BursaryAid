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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

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

    /**
     * Keep the admin list in sync with every table that contributes visible applicant data.
     * Previously only application rows were observed, so editing a student's profile or a
     * bursary name could leave stale labels on the admin screen until another application
     * happened to change.
     */
    override fun observeApplications(): Flow<List<AdminApplicationItem>> =
        combine(
            applicationRepository.getAllApplications(),
            profileRepository.getAllProfiles(),
            userRepository.getAllUsers(),
            bursaryRepository.getAllBursaries()
        ) { applications, profiles, users, bursaries ->
            val profilesById = profiles.associateBy { it.studentId }
            val usersById = users.associateBy { it.userId }
            val bursariesById = bursaries.associateBy { it.bursaryId }

            applications
                .asSequence()
                .filter { it.applicationStatus != ApplicationStatus.DRAFT }
                .map { application ->
                    val profile = profilesById[application.studentId]
                    val user = profile?.let { usersById[it.userId] }
                    AdminApplicationItem(
                        applicationId = application.applicationId,
                        studentId = application.studentId,
                        studentName = user?.fullName ?: "Unknown student",
                        admissionNumber = profile?.registrationNumber ?: "-",
                        bursaryId = application.bursaryId,
                        bursaryName = bursariesById[application.bursaryId]?.bursaryName ?: "Unknown bursary",
                        requestedAmountKes = application.requestedAmountKes,
                        applicationStatus = application.applicationStatus,
                        submittedAtMillis = application.submittedAtMillis ?: 0L
                    )
                }
                .toList()
        }

    override suspend fun getApplicationDetail(
        applicationId: Long
    ): AdminApplicationDetail? = loadDetail(applicationId)

    override suspend fun startReview(
        applicationId: Long
    ): DecisionResult = database.withTransaction {
        val application = applicationRepository.getApplicationById(applicationId)
            ?: return@withTransaction DecisionResult.failure("Application not found.")

        if (!ReviewWorkflow.canStartReview(application.applicationStatus)) {
            return@withTransaction DecisionResult.failure("Only submitted applications can be reviewed.")
        }

        val updatedRows = applicationRepository.updateApplication(
            application.copy(
                applicationStatus = ApplicationStatus.UNDER_REVIEW,
                updatedAtMillis = clock()
            )
        )
        if (updatedRows == 0) return@withTransaction DecisionResult.failure("The application could not be updated.")
        DecisionResult.success()
    }

    override suspend fun requestInformation(
        applicationId: Long,
        message: String,
        reviewedByUserId: Long
    ): DecisionResult = database.withTransaction {
        val application = applicationRepository.getApplicationById(applicationId)
            ?: return@withTransaction DecisionResult.failure("Application not found.")

        if (!ReviewWorkflow.canRequestInformation(application.applicationStatus)) {
            return@withTransaction DecisionResult.failure("Information can only be requested during review.")
        }

        val cleanMessage = message.trim()
        if (cleanMessage.isBlank()) {
            return@withTransaction DecisionResult.failure("Please provide a reason for requesting information.")
        }

        val updatedRows = applicationRepository.updateApplication(
            application.copy(
                applicationStatus = ApplicationStatus.NEEDS_INFORMATION,
                updatedAtMillis = clock()
            )
        )
        if (updatedRows == 0) return@withTransaction DecisionResult.failure("The application could not be updated.")

        notifyStudent(application.studentId, NotificationType.INFORMATION_REQUESTED, "More information needed", cleanMessage)
        DecisionResult.success()
    }

    override suspend fun approveApplication(
        applicationId: Long,
        awardedAmountKes: Long,
        remarks: String,
        reviewedByUserId: Long
    ): DecisionResult = database.withTransaction {
        val detail = loadDetail(applicationId)
            ?: return@withTransaction DecisionResult.failure("Application not found.")

        val eligibility = EligibilityRules.check(detail.toEligibilityInput())
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
            return@withTransaction DecisionResult.failure(validation.errors.joinToString("\n"))
        }

        val application = applicationRepository.getApplicationById(applicationId)
            ?: return@withTransaction DecisionResult.failure("Application not found.")

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

        val updatedRows = applicationRepository.updateApplication(
            application.copy(applicationStatus = ApplicationStatus.APPROVED, updatedAtMillis = now)
        )
        if (updatedRows == 0) return@withTransaction DecisionResult.failure("The application could not be updated.")

        notifyStudent(
            application.studentId,
            NotificationType.ALLOCATION_CONFIRMED,
            "Bursary awarded",
            "Your application was approved for KSh %,d.".format(awardedAmountKes)
        )
        DecisionResult.success()
    }

    override suspend fun rejectApplication(
        applicationId: Long,
        reason: String,
        reviewedByUserId: Long
    ): DecisionResult = database.withTransaction {
        val application = applicationRepository.getApplicationById(applicationId)
            ?: return@withTransaction DecisionResult.failure("Application not found.")

        if (!ReviewWorkflow.canDecide(application.applicationStatus)) {
            return@withTransaction DecisionResult.failure("Only applications under review can be rejected.")
        }

        val cleanReason = reason.trim()
        if (cleanReason.isBlank()) {
            return@withTransaction DecisionResult.failure("A rejection reason is required.")
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

        val updatedRows = applicationRepository.updateApplication(
            application.copy(applicationStatus = ApplicationStatus.REJECTED, updatedAtMillis = now)
        )
        if (updatedRows == 0) return@withTransaction DecisionResult.failure("The application could not be updated.")

        notifyStudent(application.studentId, NotificationType.APPLICATION_STATUS_CHANGED, "Application rejected", cleanReason)
        DecisionResult.success()
    }

    private suspend fun loadDetail(applicationId: Long): AdminApplicationDetail? {
        val application = applicationRepository.getApplicationById(applicationId) ?: return null
        val bursary = bursaryRepository.getBursaryById(application.bursaryId)
            ?: error("Bursary ${application.bursaryId} was not found.")
        val profile = profileRepository.getProfileById(application.studentId)
        val user = profile?.let { userRepository.getUserById(it.userId) }
        val approved = allocationRepository.getAllocationsByBursary(bursary.bursaryId)
            .first()
            .filter { it.decisionStatus == DecisionStatus.APPROVED }

        return AdminApplicationDetail(
            applicationId = application.applicationId,
            applicationStatus = application.applicationStatus,
            requestedAmountKes = application.requestedAmountKes,
            justification = application.applicationReason,
            submittedAtMillis = application.submittedAtMillis ?: 0L,
            studentId = application.studentId,
            studentName = user?.fullName ?: "Unknown student",
            admissionNumber = profile?.registrationNumber ?: "-",
            institutionName = profile?.institutionName ?: "-",
            courseName = profile?.courseName ?: "-",
            yearOfStudy = profile?.yearOfStudy ?: 0,
            householdIncomeKes = profile?.monthlyHouseholdIncomeKes ?: 0L,
            bursaryId = bursary.bursaryId,
            bursaryName = bursary.bursaryName,
            applicationDeadlineMillis = bursary.applicationDeadlineMillis,
            incomeLimitKes = bursary.minimumIncomeKes.takeIf { it > 0L },
            maximumAwardKes = bursary.maximumAwardKes,
            totalBudgetKes = bursary.totalBudgetKes,
            approvedAwardsKes = approved.map { it.allocatedAmount.toLong() },
            hasApprovedAllocation = approved.any { it.applicationId == applicationId }
        )
    }

    private suspend fun notifyStudent(
        studentId: Long,
        type: NotificationType,
        title: String,
        message: String
    ) {
        val repository = notificationRepository ?: return
        val profile = profileRepository.getProfileById(studentId) ?: return
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
