package com.example.bursaryaid

import com.example.bursaryaid.feature.admin.logic.AllocationValidator
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.model.BursaryApplication
import com.example.bursaryaid.model.StudentProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AllocationAndEligibilityTest {

    @Test
    fun validateAllocation_validAward_returnsNull() {
        val result = AllocationValidator.validateAllocation(
            applicationId = 10L,
            awardedAmountKes = 25_000L,
            requestedAmountKes = 30_000L,
            maximumAwardKes = 30_000L,
            remainingBudgetKes = 500_000L,
            applicationStatus = ApplicationStatus.SUBMITTED
        )
        assertNull(result)
    }

    @Test
    fun validateAllocation_zeroOrNegativeAmount_returnsError() {
        val result = AllocationValidator.validateAllocation(
            applicationId = 10L,
            awardedAmountKes = 0L,
            requestedAmountKes = 30_000L,
            maximumAwardKes = 30_000L,
            remainingBudgetKes = 500_000L,
            applicationStatus = ApplicationStatus.SUBMITTED
        )
        assertEquals("Awarded amount must be greater than zero.", result)
    }

    @Test
    fun validateAllocation_exceedsRequestedAmount_returnsError() {
        val result = AllocationValidator.validateAllocation(
            applicationId = 10L,
            awardedAmountKes = 35_000L, // Exceeds 30,000 requested
            requestedAmountKes = 30_000L,
            maximumAwardKes = 50_000L,
            remainingBudgetKes = 500_000L,
            applicationStatus = ApplicationStatus.SUBMITTED
        )
        assertTrue(result != null && result.contains("cannot exceed requested amount"))
    }

    @Test
    fun validateAllocation_exceedsMaximumAward_returnsError() {
        val result = AllocationValidator.validateAllocation(
            applicationId = 10L,
            awardedAmountKes = 25_000L,
            requestedAmountKes = 30_000L,
            maximumAwardKes = 20_000L, // Exceeds 20,000 max award limit
            remainingBudgetKes = 500_000L,
            applicationStatus = ApplicationStatus.SUBMITTED
        )
        assertTrue(result != null && result.contains("exceeds maximum permitted award"))
    }

    @Test
    fun testAllocationBudgetLimit() {
        val result = AllocationValidator.validateAllocation(
            applicationId = 10L,
            awardedAmountKes = 25_000L,
            requestedAmountKes = 30_000L,
            maximumAwardKes = 30_000L,
            remainingBudgetKes = 10_000L, // Budget only has 10,000 left
            applicationStatus = ApplicationStatus.SUBMITTED
        )
        assertTrue(result != null && result.contains("exceeds remaining budget balance"))
    }

    @Test
    fun validateAllocation_draftStatus_returnsError() {
        val result = AllocationValidator.validateAllocation(
            applicationId = 10L,
            awardedAmountKes = 20_000L,
            requestedAmountKes = 20_000L,
            maximumAwardKes = 30_000L,
            remainingBudgetKes = 500_000L,
            applicationStatus = ApplicationStatus.DRAFT // Unsubmitted draft cannot be allocated
        )
        assertEquals("Only submitted or under-review applications can receive fund allocation.", result)
    }

    @Test
    fun testEligibilityRules() {
        val profileIneligible = StudentProfile(
            studentId = 1L,
            userId = 100L,
            registrationNumber = "SC/123/2022",
            institutionName = "Strathmore",
            courseName = "BSc CS",
            yearOfStudy = 3,
            monthlyHouseholdIncomeKes = 75_000L // Exceeds max threshold of 50,000
        )

        val profileEligible = profileIneligible.copy(monthlyHouseholdIncomeKes = 30_000L)

        val bursary = Bursary(
            bursaryId = 1L,
            bursaryName = "Need-based Financial Aid",
            bursaryDescription = "Support for low-income families",
            totalBudgetKes = 1_000_000L,
            minimumIncomeKes = 50_000L, // Max allowable income
            maximumAwardKes = 40_000L,
            applicationDeadlineMillis = System.currentTimeMillis() + 100000L
        )

        val submittedApp = BursaryApplication(
            applicationId = 1001L,
            bursaryId = 1L,
            studentId = 1L,
            requestedAmountKes = 25_000L,
            applicationStatus = ApplicationStatus.SUBMITTED
        )

        val draftApp = submittedApp.copy(applicationStatus = ApplicationStatus.DRAFT)

        // 1. High income applicant -> Ineligible
        val resultIneligible = AllocationValidator.checkEligibility(profileIneligible, bursary, submittedApp)
        assertFalse(resultIneligible.isEligible)
        assertTrue(resultIneligible.reason.contains("exceeds maximum threshold"))

        // 2. Draft status application -> Ineligible
        val resultDraft = AllocationValidator.checkEligibility(profileEligible, bursary, draftApp)
        assertFalse(resultDraft.isEligible)
        assertTrue(resultDraft.reason.contains("not eligible for review"))

        // 3. Valid income and status -> Eligible
        val resultEligible = AllocationValidator.checkEligibility(profileEligible, bursary, submittedApp)
        assertTrue(resultEligible.isEligible)
    }
}
