package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.AppConstants
import com.example.bursaryaid.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EligibilityRulesTest {

    private fun validInput() = EligibilityInput(
        applicationStatus = ApplicationStatus.SUBMITTED,
        submittedAtMillis = 1_000L,
        applicationDeadlineMillis = 2_000L,
        yearOfStudy = 2,
        householdIncomeKes = 20_000L,
        incomeLimitKes = 50_000L,
        requestedAmountKes = 30_000L,
        hasApprovedAllocation = false
    )

    @Test
    fun validApplication_isEligible() {
        val result = EligibilityRules.check(validInput())
        assertTrue(result.isEligible)
        assertTrue(result.failedReasons.isEmpty())
    }

    @Test
    fun draftApplication_isNotEligible() {
        val result = EligibilityRules.check(validInput().copy(applicationStatus = ApplicationStatus.DRAFT))
        assertFalse(result.isEligible)
    }

    @Test
    fun submittedAfterDeadline_isNotEligible() {
        val result = EligibilityRules.check(validInput().copy(submittedAtMillis = 3_000L))
        assertFalse(result.isEligible)
    }

    @Test
    fun submittedExactlyOnDeadline_isEligible() {
        val result = EligibilityRules.check(validInput().copy(submittedAtMillis = 2_000L))
        assertTrue(result.isEligible)
    }

    @Test
    fun incomeAboveLimit_isNotEligible() {
        val result = EligibilityRules.check(validInput().copy(householdIncomeKes = 80_000L))
        assertFalse(result.isEligible)
    }

    @Test
    fun incomeEqualToLimit_isEligible() {
        val result = EligibilityRules.check(validInput().copy(householdIncomeKes = 50_000L))
        assertTrue(result.isEligible)
    }

    @Test
    fun noIncomeLimit_skipsIncomeCheck() {
        val result = EligibilityRules.check(
            validInput().copy(incomeLimitKes = null, householdIncomeKes = null)
        )
        assertTrue(result.isEligible)
    }

    @Test
    fun missingIncomeWhenLimitIsSet_isNotEligible() {
        val result = EligibilityRules.check(validInput().copy(householdIncomeKes = null))
        assertFalse(result.isEligible)
    }

    @Test
    fun yearOfStudyOutsideRange_isNotEligible() {
        val result = EligibilityRules.check(
            validInput().copy(yearOfStudy = AppConstants.MAX_YEAR_OF_STUDY + 1)
        )
        assertFalse(result.isEligible)
    }

    @Test
    fun existingApprovedAllocation_isNotEligible() {
        val result = EligibilityRules.check(validInput().copy(hasApprovedAllocation = true))
        assertFalse(result.isEligible)
    }

    @Test
    fun multipleFailures_listsEveryReason() {
        val result = EligibilityRules.check(
            validInput().copy(applicationStatus = ApplicationStatus.DRAFT, requestedAmountKes = 0L)
        )
        assertEquals(2, result.failedReasons.size)
    }
}