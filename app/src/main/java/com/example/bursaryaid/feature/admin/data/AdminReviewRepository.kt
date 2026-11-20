package com.example.bursaryaid.feature.admin.data

import com.example.bursaryaid.feature.admin.logic.AdminApplicationDetail
import com.example.bursaryaid.feature.admin.logic.AdminApplicationItem
import com.example.bursaryaid.feature.admin.logic.DecisionResult
import kotlinx.coroutines.flow.Flow

/**
 * What the admin ViewModels need from the data layer. It is an interface of our own,
 * so the ViewModels do not depend on how the shared schema ends up being named.
 * One adapter will implement it over the real repositories.
 */
interface AdminReviewRepository {

    /** Every application as a plain row. Emits again whenever the database changes. */
    fun observeApplications(): Flow<List<AdminApplicationItem>>

    /** One application with its student and bursary facts, or null if it does not exist. */
    suspend fun getApplicationDetail(applicationId: Long): AdminApplicationDetail?

    /** SUBMITTED -> UNDER_REVIEW. */
    suspend fun startReview(applicationId: Long): DecisionResult

    /** UNDER_REVIEW -> NEEDS_INFORMATION, with a message for the student. */
    suspend fun requestInformation(
        applicationId: Long,
        message: String,
        reviewedByUserId: Long
    ): DecisionResult

    /**
     * UNDER_REVIEW -> APPROVED. The implementation must, in ONE database transaction:
     * re-check the remaining budget against the approved awards, save the AllocationRecord
     * and change the application status. Otherwise two administrators could spend the
     * same money at the same time.
     */
    suspend fun approveApplication(
        applicationId: Long,
        awardedAmountKes: Long,
        remarks: String,
        reviewedByUserId: Long
    ): DecisionResult

    /** UNDER_REVIEW -> REJECTED, recorded with an amount of 0. */
    suspend fun rejectApplication(
        applicationId: Long,
        reason: String,
        reviewedByUserId: Long
    ): DecisionResult
}