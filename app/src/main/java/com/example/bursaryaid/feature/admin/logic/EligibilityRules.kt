package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.AppConstants
import com.example.bursaryaid.model.ApplicationStatus

/**
 * Everything the eligibility check needs, as plain values.
 * An adapter fills this in from the database, so these rules never touch Room.
 */
data class EligibilityInput(
    val applicationStatus: ApplicationStatus,
    val submittedAtMillis: Long,
    val applicationDeadlineMillis: Long,
    val yearOfStudy: Int,
    val householdIncomeKes: Long?,
    val incomeLimitKes: Long?,
    val requestedAmountKes: Long,
    val hasApprovedAllocation: Boolean
)

data class EligibilityResult(
    val isEligible: Boolean,
    val failedReasons: List<String> = emptyList(),
    val reason: String = if (isEligible) "Student meets all income and status eligibility criteria." else failedReasons.joinToString("; ")
) {
    constructor(isEligible: Boolean, reason: String) : this(
        isEligible = isEligible,
        failedReasons = if (isEligible || reason.isBlank()) emptyList() else listOf(reason),
        reason = reason
    )

    val eligibilityReason: String
        get() = if (isEligible) "Meets all criteria" else (failedReasons.ifEmpty { listOf(reason) }).joinToString("; ")
}

/**
 * Explicit eligibility criteria (no scoring). Every check must pass.
 * Rules marked PROPOSED are awaiting the group lead's confirmation.
 */
object EligibilityRules {

    fun check(input: EligibilityInput): EligibilityResult {
        val failures = mutableListOf<String>()

        // E1: only submitted or under-review applications can be assessed
        val reviewable = input.applicationStatus == ApplicationStatus.SUBMITTED ||
                input.applicationStatus == ApplicationStatus.UNDER_REVIEW
        if (!reviewable) {
            failures += "Application is not open for review"
        }

        // E3: the submission time is compared with the deadline, not the bursary's open flag
        if (input.submittedAtMillis > input.applicationDeadlineMillis) {
            failures += "Submitted after the deadline"
        }

        // E5
        if (input.yearOfStudy !in AppConstants.MIN_YEAR_OF_STUDY..AppConstants.MAX_YEAR_OF_STUDY) {
            failures += "Year of study is outside the allowed range"
        }

        // E6 (PROPOSED: the limit is a ceiling). Skipped when the bursary sets no limit.
        val limit = input.incomeLimitKes
        if (limit != null) {
            val income = input.householdIncomeKes
            if (income == null) {
                failures += "Household income has not been provided"
            } else if (income > limit) {
                val limitText = "%,d".format(limit)
                failures += "Household income exceeds the limit of KSh $limitText"
            }
        }

        // E7
        if (input.requestedAmountKes <= 0L) {
            failures += "Requested amount must be greater than 0"
        }

        // E8
        if (input.hasApprovedAllocation) {
            failures += "This application already has an approved allocation"
        }

        return EligibilityResult(isEligible = failures.isEmpty(), failedReasons = failures)
    }
}