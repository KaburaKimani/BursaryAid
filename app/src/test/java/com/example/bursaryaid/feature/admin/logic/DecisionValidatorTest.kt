package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionValidatorTest {

    private fun detail(
        status: ApplicationStatus = ApplicationStatus.UNDER_REVIEW,
        requestedAmountKes: Long = 50_000L,
        maximumAwardKes: Long = 60_000L,
        totalBudgetKes: Long = 1_000_000L,
        approvedAwardsKes: List<Long> = emptyList(),
        householdIncomeKes: Long? = 20_000L
    ) = AdminApplicationDetail(
        applicationId = 1L,
        applicationStatus = status,
        requestedAmountKes = requestedAmountKes,
        justification = "Needs support with fees",
        submittedAtMillis = 1_000L,
        studentId = 5L,
        studentName = "Mary Wanjiku",
        admissionNumber = "ADM001",
        institutionName = "Strathmore University",
        courseName = "BBIT",
        yearOfStudy = 2,
        householdIncomeKes = householdIncomeKes,
        bursaryId = 2L,
        bursaryName = "County Bursary",
        applicationDeadlineMillis = 2_000L,
        incomeLimitKes = 50_000L,
        maximumAwardKes = maximumAwardKes,
        totalBudgetKes = totalBudgetKes,
        approvedAwardsKes = approvedAwardsKes,
        hasApprovedAllocation = false
    )

    @Test
    fun validAmount_isAccepted() {
        val check = DecisionValidator.validateApproval(detail(), "40000")
        assertTrue(check.isValid)
        assertEquals(40_000L, check.awardedAmountKes)
    }

    @Test
    fun amountWithCommas_isAccepted() {
        assertEquals(40_000L, DecisionValidator.validateApproval(detail(), "40,000").awardedAmountKes)
    }

    @Test
    fun blankAmount_isRejected() {
        val check = DecisionValidator.validateApproval(detail(), "   ")
        assertFalse(check.isValid)
        assertTrue(check.formErrors.containsKey(DecisionForm.FIELD_AMOUNT))
    }

    @Test
    fun nonNumericAmount_isRejected() {
        val check = DecisionValidator.validateApproval(detail(), "forty thousand")
        assertFalse(check.isValid)
        assertNull(check.awardedAmountKes)
    }

    @Test
    fun zeroAmount_isRejected() {
        assertFalse(DecisionValidator.validateApproval(detail(), "0").isValid)
    }

    @Test
    fun amountAboveMaximum_isRejected() {
        val check = DecisionValidator.validateApproval(
            detail(requestedAmountKes = 90_000L, maximumAwardKes = 60_000L), "70000"
        )
        assertFalse(check.isValid)
        assertTrue(check.formErrors.getValue(DecisionForm.FIELD_AMOUNT).contains("Maximum award"))
    }

    @Test
    fun amountAboveRemainingBudget_isRejected() {
        val check = DecisionValidator.validateApproval(
            detail(totalBudgetKes = 100_000L, approvedAwardsKes = listOf(80_000L)), "40000"
        )
        assertFalse(check.isValid)
        assertTrue(check.formErrors.getValue(DecisionForm.FIELD_AMOUNT).contains("remaining budget"))
    }

    @Test
    fun amountAboveRequested_isRejected() {
        val check = DecisionValidator.validateApproval(detail(requestedAmountKes = 30_000L), "40000")
        assertFalse(check.isValid)
    }

    @Test
    fun applicationNotUnderReview_isRejected() {
        val check = DecisionValidator.validateApproval(
            detail(status = ApplicationStatus.SUBMITTED), "40000"
        )
        assertFalse(check.isValid)
    }

    @Test
    fun ineligibleApplication_isRejected() {
        val check = DecisionValidator.validateApproval(detail(householdIncomeKes = 90_000L), "40000")
        assertFalse(check.isValid)
    }

    @Test
    fun reason_tooShort_givesAnErrorOnThatField() {
        val errors = DecisionValidator.validateReason("short", DecisionForm.FIELD_REASON)
        assertTrue(errors.containsKey(DecisionForm.FIELD_REASON))
    }

    @Test
    fun reason_longEnough_givesNoErrors() {
        assertTrue(
            DecisionValidator.validateReason("Income above the limit", DecisionForm.FIELD_REASON).isEmpty()
        )
    }

    @Test
    fun informationMessage_usesItsOwnFieldKey() {
        val errors = DecisionValidator.validateReason("", DecisionValidator.FIELD_MESSAGE)
        assertTrue(errors.containsKey(DecisionValidator.FIELD_MESSAGE))
    }
}