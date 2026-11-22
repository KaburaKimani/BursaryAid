package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AllocationRulesTest {

    private fun validInput() = AllocationInput(
        applicationStatus = ApplicationStatus.UNDER_REVIEW,
        isEligible = true,
        awardedAmountKes = 40_000L,
        requestedAmountKes = 50_000L,
        maximumAwardKes = 60_000L,
        remainingBudgetKes = 450_000L,
        hasApprovedAllocation = false
    )

    @Test
    fun validAward_passes() {
        val result = AllocationRules.validate(validInput())
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun awardAboveMaximum_fails() {
        val result = AllocationRules.validate(
            validInput().copy(awardedAmountKes = 70_000L, requestedAmountKes = 80_000L)
        )
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Maximum award") })
    }

    @Test
    fun awardAboveRemainingBudget_fails() {
        val result = AllocationRules.validate(
            validInput().copy(remainingBudgetKes = 30_000L)
        )
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("remaining budget") })
    }

    @Test
    fun awardEqualToRemainingBudget_passes() {
        val result = AllocationRules.validate(
            validInput().copy(awardedAmountKes = 40_000L, remainingBudgetKes = 40_000L)
        )
        assertTrue(result.isValid)
    }

    @Test
    fun awardAboveRequested_fails() {
        val result = AllocationRules.validate(
            validInput().copy(awardedAmountKes = 55_000L)
        )
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("requested") })
    }

    @Test
    fun zeroAward_fails() {
        val result = AllocationRules.validate(validInput().copy(awardedAmountKes = 0L))
        assertFalse(result.isValid)
    }

    @Test
    fun negativeAward_fails() {
        val result = AllocationRules.validate(validInput().copy(awardedAmountKes = -5_000L))
        assertFalse(result.isValid)
    }

    @Test
    fun notUnderReview_fails() {
        val result = AllocationRules.validate(
            validInput().copy(applicationStatus = ApplicationStatus.SUBMITTED)
        )
        assertFalse(result.isValid)
    }

    @Test
    fun notEligible_fails() {
        val result = AllocationRules.validate(validInput().copy(isEligible = false))
        assertFalse(result.isValid)
    }

    @Test
    fun alreadyApproved_fails() {
        val result = AllocationRules.validate(validInput().copy(hasApprovedAllocation = true))
        assertFalse(result.isValid)
    }

    @Test
    fun remainingBudget_subtractsApprovedAwards() {
        // 1,000,000 budget, awards of 300,000 and 250,000 leave 450,000
        assertEquals(
            450_000L,
            BudgetCalculator.remainingBudgetKes(1_000_000L, listOf(300_000L, 250_000L))
        )
    }

    @Test
    fun remainingBudget_withNoAwards_isTheTotal() {
        assertEquals(1_000_000L, BudgetCalculator.remainingBudgetKes(1_000_000L, emptyList()))
    }

    @Test
    fun remainingBudget_neverBelowZero() {
        assertEquals(0L, BudgetCalculator.remainingBudgetKes(100_000L, listOf(80_000L, 50_000L)))
    }

    @Test
    fun reason_tooShort_returnsError() {
        assertNotNull(AllocationRules.reasonError("short"))
    }

    @Test
    fun reason_whitespaceOnly_returnsError() {
        assertNotNull(AllocationRules.reasonError("             "))
    }

    @Test
    fun reason_longEnough_returnsNull() {
        assertNull(AllocationRules.reasonError("Income above the limit"))
    }
}