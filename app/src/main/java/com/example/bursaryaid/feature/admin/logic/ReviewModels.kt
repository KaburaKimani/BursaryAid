package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus

/**
 * Everything the review screen needs about one application, as plain values.
 * An adapter builds this from the database. Amounts are whole shillings.
 */
data class AdminApplicationDetail(
    val applicationId: Long,
    val applicationStatus: ApplicationStatus,
    val requestedAmountKes: Long,
    val justification: String,
    val submittedAtMillis: Long,
    // The student
    val studentId: Long,
    val studentName: String,
    val admissionNumber: String,
    val institutionName: String,
    val courseName: String,
    val yearOfStudy: Int,
    val householdIncomeKes: Long?,
    // The bursary
    val bursaryId: Long,
    val bursaryName: String,
    val applicationDeadlineMillis: Long,
    val incomeLimitKes: Long?,
    val maximumAwardKes: Long,
    val totalBudgetKes: Long,
    /** The amounts of every APPROVED allocation for this bursary. */
    val approvedAwardsKes: List<Long>,
    val hasApprovedAllocation: Boolean
) {
    /** Always calculated from the approved awards and never stored. */
    val remainingBudgetKes: Long
        get() = BudgetCalculator.remainingBudgetKes(totalBudgetKes, approvedAwardsKes)
}

/** Converts a detail row into what the eligibility rules need. */
fun AdminApplicationDetail.toEligibilityInput() = EligibilityInput(
    applicationStatus = applicationStatus,
    submittedAtMillis = submittedAtMillis,
    applicationDeadlineMillis = applicationDeadlineMillis,
    yearOfStudy = yearOfStudy,
    householdIncomeKes = householdIncomeKes,
    incomeLimitKes = incomeLimitKes,
    requestedAmountKes = requestedAmountKes,
    hasApprovedAllocation = hasApprovedAllocation
)

/** The outcome of an action that writes to the database. */
data class DecisionResult(
    val isSuccess: Boolean,
    val message: String? = null
) {
    companion object {
        fun success() = DecisionResult(isSuccess = true)
        fun failure(message: String) = DecisionResult(isSuccess = false, message = message)
    }
}

/**
 * Sent after a decision is saved, so other modules (for example notifications) can react
 * without the admin code depending on them.
 */
data class DecisionEvent(
    val applicationId: Long,
    val studentId: Long,
    val newStatus: ApplicationStatus,
    val awardedAmountKes: Long,
    val reason: String
)