package com.example.bursaryaid.feature.student.logic

import com.example.bursaryaid.data.model.AppConstants
import com.example.bursaryaid.data.model.ApplicationStatus
import com.example.bursaryaid.data.model.BursaryApplication

/**
 * Student-side lifecycle rules, kept in one place so UI, ViewModels and tests agree.
 * Eligibility and allocation rules are NOT here - Member 4 owns those.
 */
object ApplicationRules {

    /** Statuses in which the student may still change the application. */
    private val EDITABLE_STATUSES = setOf(ApplicationStatus.DRAFT, ApplicationStatus.NEEDS_INFORMATION)

    fun canEdit(applicationStatus: ApplicationStatus): Boolean = applicationStatus in EDITABLE_STATUSES

    /** Only drafts may be deleted; submitted applications are part of the review record. */
    fun canDelete(applicationStatus: ApplicationStatus): Boolean = applicationStatus == ApplicationStatus.DRAFT

    fun canSubmit(applicationStatus: ApplicationStatus): Boolean = canEdit(applicationStatus)

    /**
     * True when the student already has MAX_APPLICATIONS_PER_BURSARY (or more) applications
     * for [bursaryId], ignoring [excludeApplicationId] (the one currently being edited).
     */
    fun isDuplicate(
        existingApplications: List<BursaryApplication>,
        bursaryId: Long,
        excludeApplicationId: Long?
    ): Boolean = existingApplications.count {
        it.bursaryId == bursaryId && it.applicationId != excludeApplicationId
    } >= AppConstants.MAX_APPLICATIONS_PER_BURSARY

    /** Newest activity first; null filter means "show all". */
    fun filterByStatus(
        applications: List<BursaryApplication>,
        selectedFilter: ApplicationStatus?
    ): List<BursaryApplication> = applications
        .filter { selectedFilter == null || it.applicationStatus == selectedFilter }
        .sortedByDescending { it.updatedAtMillis }
}
