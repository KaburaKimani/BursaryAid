package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.model.BursaryApplication
import com.example.bursaryaid.model.StudentProfile

/**
 * Validator and rules engine for administrator eligibility evaluation and fund allocation.
 * Owned jointly by Member 4 (Allocation rules) and Member 6 (Validation & Integration tests).
 */
object AllocationValidator {

    /**
     * Validates an allocation attempt against application state, maximum award, and budget constraints.
     * Returns an error message if invalid, or null if valid.
     */
    fun validateAllocation(
        applicationId: Long,
        awardedAmountKes: Long,
        requestedAmountKes: Long,
        maximumAwardKes: Long,
        remainingBudgetKes: Long,
        applicationStatus: ApplicationStatus = ApplicationStatus.SUBMITTED
    ): String? {
        if (applicationId <= 0L) {
            return "Invalid application ID."
        }
        if (applicationStatus != ApplicationStatus.SUBMITTED && applicationStatus != ApplicationStatus.UNDER_REVIEW) {
            return "Only submitted or under-review applications can receive fund allocation."
        }
        if (awardedAmountKes <= 0L) {
            return "Awarded amount must be greater than zero."
        }
        if (awardedAmountKes > requestedAmountKes) {
            return "Awarded amount ($awardedAmountKes KES) cannot exceed requested amount ($requestedAmountKes KES)."
        }
        if (maximumAwardKes > 0L && awardedAmountKes > maximumAwardKes) {
            return "Awarded amount ($awardedAmountKes KES) exceeds maximum permitted award of $maximumAwardKes KES."
        }
        if (awardedAmountKes > remainingBudgetKes) {
            return "Awarded amount ($awardedAmountKes KES) exceeds remaining budget balance of $remainingBudgetKes KES."
        }
        return null
    }

    /**
     * Evaluates applicant eligibility based on household income thresholds and application status.
     */
    fun checkEligibility(
        profile: StudentProfile,
        bursary: Bursary,
        application: BursaryApplication
    ): EligibilityResult {
        if (application.applicationStatus != ApplicationStatus.SUBMITTED &&
            application.applicationStatus != ApplicationStatus.UNDER_REVIEW
        ) {
            return EligibilityResult(
                isEligible = false,
                reason = "Application is in '${application.applicationStatus.name}' status and not eligible for review."
            )
        }

        if (bursary.minimumIncomeKes > 0L && profile.monthlyHouseholdIncomeKes > bursary.minimumIncomeKes) {
            return EligibilityResult(
                isEligible = false,
                reason = "Household income (${profile.monthlyHouseholdIncomeKes} KES) exceeds maximum threshold (${bursary.minimumIncomeKes} KES)."
            )
        }

        if (application.requestedAmountKes <= 0L) {
            return EligibilityResult(
                isEligible = false,
                reason = "Requested amount must be greater than zero."
            )
        }

        return EligibilityResult(
            isEligible = true,
            reason = "Student meets all income and status eligibility criteria."
        )
    }
}
