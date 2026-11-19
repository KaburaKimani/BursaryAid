package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import java.util.Locale

/** Everything an award needs to be checked, as plain whole-shilling values. */
data class AllocationInput(
    val applicationStatus: ApplicationStatus,
    val isEligible: Boolean,
    val awardedAmountKes: Long,
    val requestedAmountKes: Long,
    val maximumAwardKes: Long,
    val remainingBudgetKes: Long,
    val hasApprovedAllocation: Boolean
)

data class AllocationValidation(
    val isValid: Boolean,
    val errors: List<String>
)

object BudgetCalculator {
    /**
     * The budget left is always calculated from the approved awards and never stored,
     * so it cannot drift out of step with the real allocations.
     * It never goes below 0, even if the data is over-allocated.
     */
    fun remainingBudgetKes(totalBudgetKes: Long, approvedAwardsKes: List<Long>): Long =
        (totalBudgetKes - approvedAwardsKes.sum()).coerceAtLeast(0L)
}

object AllocationRules {

    /** Every rule must pass; every failed rule is reported. */
    fun validate(input: AllocationInput): AllocationValidation {
        val errors = mutableListOf<String>()

        // A1
        if (input.applicationStatus != ApplicationStatus.UNDER_REVIEW) {
            errors += "Application must be under review before an award is made"
        }
        if (!input.isEligible) {
            errors += "Application does not meet the eligibility criteria"
        }
        // A6
        if (input.hasApprovedAllocation) {
            errors += "This application already has an approved allocation"
        }

        // A2: amount first, so the limit checks below only run on a real amount
        if (input.awardedAmountKes <= 0L) {
            errors += "Enter an award amount greater than 0"
        } else {
            // A3
            if (input.awardedAmountKes > input.maximumAwardKes) {
                errors += "Maximum award for this bursary is ${formatKes(input.maximumAwardKes)}"
            }
            // A4
            if (input.awardedAmountKes > input.remainingBudgetKes) {
                errors += "Exceeds remaining budget of ${formatKes(input.remainingBudgetKes)}"
            }
            // A5 (PROPOSED)
            if (input.awardedAmountKes > input.requestedAmountKes) {
                errors += "Cannot award more than the ${formatKes(input.requestedAmountKes)} requested"
            }
        }

        return AllocationValidation(isValid = errors.isEmpty(), errors = errors)
    }

    /** Returns an error message, or null when the reason is long enough. */
    fun reasonError(reason: String): String? =
        if (reason.trim().length < AdminFeatureConstants.MIN_DECISION_REASON_LENGTH) {
            "Enter a reason of at least ${AdminFeatureConstants.MIN_DECISION_REASON_LENGTH} characters"
        } else {
            null
        }

    // Locale.US keeps the thousands separator the same on every phone
    private fun formatKes(value: Long): String = String.format(Locale.US, "KSh %,d", value)
}