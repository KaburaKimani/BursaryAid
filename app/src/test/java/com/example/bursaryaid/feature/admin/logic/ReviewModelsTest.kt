package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewModelsTest {

    private fun detail(
        status: ApplicationStatus = ApplicationStatus.UNDER_REVIEW,
        totalBudgetKes: Long = 1_000_000L,
        approvedAwardsKes: List<Long> = emptyList(),
        householdIncomeKes: Long? = 20_000L,
        incomeLimitKes: Long? = 50_000L
    ) = AdminApplicationDetail(
        applicationId = 1L,
        applicationStatus = status,
        requestedAmountKes = 30_000L,
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
        incomeLimitKes = incomeLimitKes,
        maximumAwardKes = 60_000L,
        totalBudgetKes = totalBudgetKes,
        approvedAwardsKes = approvedAwardsKes,
        hasApprovedAllocation = false
    )

    @Test
    fun remainingBudget_isTheTotalMinusApprovedAwards() {
        val result = detail(approvedAwardsKes = listOf(300_000L, 250_000L))
        assertEquals(450_000L, result.remainingBudgetKes)
    }

    @Test
    fun remainingBudget_withNoAwards_isTheTotal() {
        assertEquals(1_000_000L, detail().remainingBudgetKes)
    }

    @Test
    fun eligibilityInput_copiesTheFieldsTheRulesNeed() {
        val input = detail().toEligibilityInput()
        assertEquals(ApplicationStatus.UNDER_REVIEW, input.applicationStatus)
        assertEquals(30_000L, input.requestedAmountKes)
        assertEquals(20_000L, input.householdIncomeKes)
        assertEquals(50_000L, input.incomeLimitKes)
        assertEquals(2_000L, input.applicationDeadlineMillis)
    }

    @Test
    fun detailWithValidData_passesTheEligibilityRules() {
        assertTrue(EligibilityRules.check(detail().toEligibilityInput()).isEligible)
    }

    @Test
    fun detailWithIncomeAboveLimit_failsTheEligibilityRules() {
        val result = EligibilityRules.check(detail(householdIncomeKes = 80_000L).toEligibilityInput())
        assertFalse(result.isEligible)
    }
}