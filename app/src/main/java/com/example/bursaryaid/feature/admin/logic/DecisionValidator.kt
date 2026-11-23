package com.example.bursaryaid.feature.admin.logic

/** The result of checking an approval form: the parsed amount, plus any field errors. */
data class ApprovalCheck(
    val awardedAmountKes: Long?,
    val formErrors: Map<String, String>
) {
    val isValid: Boolean get() = formErrors.isEmpty() && awardedAmountKes != null
}

/**
 * Checks what the administrator typed before anything is saved.
 * Plain Kotlin so every rule can be unit tested without a screen or a database.
 */
object DecisionValidator {

    const val FIELD_MESSAGE = "informationMessage"
    const val DEFAULT_APPROVAL_REMARKS = "Approved"

    fun validateApproval(detail: AdminApplicationDetail, amountText: String): ApprovalCheck {
        val errors = mutableMapOf<String, String>()
        val amount = DecisionForm.parseAmount(amountText)

        if (amountText.isBlank()) {
            errors[DecisionForm.FIELD_AMOUNT] = "Enter an award amount greater than 0"
        } else if (amount == null) {
            errors[DecisionForm.FIELD_AMOUNT] =
                "Enter the amount as a whole number of shillings, for example 40000"
        } else {
            val eligible = EligibilityRules.check(detail.toEligibilityInput()).isEligible
            val validation = AllocationRules.validate(
                AllocationInput(
                    applicationStatus = detail.applicationStatus,
                    isEligible = eligible,
                    awardedAmountKes = amount,
                    requestedAmountKes = detail.requestedAmountKes,
                    maximumAwardKes = detail.maximumAwardKes,
                    remainingBudgetKes = detail.remainingBudgetKes,
                    hasApprovedAllocation = detail.hasApprovedAllocation
                )
            )
            if (!validation.isValid) {
                errors[DecisionForm.FIELD_AMOUNT] = validation.errors.joinToString("\n")
            }
        }
        return ApprovalCheck(amount, errors)
    }

    /** Used for both a rejection reason and a request for information. */
    fun validateReason(reason: String, fieldKey: String): Map<String, String> {
        val error = AllocationRules.reasonError(reason)
        return if (error == null) emptyMap() else mapOf(fieldKey to error)
    }
}