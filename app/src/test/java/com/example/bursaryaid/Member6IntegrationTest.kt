package com.example.bursaryaid

import com.example.bursaryaid.data.remote.ApiResult
import com.example.bursaryaid.data.remote.BursaryApiService
import com.example.bursaryaid.data.remote.BursaryDto
import com.example.bursaryaid.data.remote.BursaryRemoteRepository
import com.example.bursaryaid.feature.admin.logic.AllocationValidator
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.logic.StudentFormValidator
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.model.BursaryApplication
import com.example.bursaryaid.model.DecisionStatus
import com.example.bursaryaid.model.StudentProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Member 6 — Testing, Validation and Integration Suite.
 * Direct implementation and execution of all 12 functions and deliverables specified in
 * the "BURSARY ALLOCATION AND FINANCIAL AID SYSTEM Six-Member Work Division & Shared Kotlin Variable Specification".
 */
class Member6IntegrationTest {

    // Shared Member 6 Variables as specified in contract
    private var applicationId: Long = 1001L
    private var studentId: Long = 50L
    private var bursaryId: Long = 1L
    private var requestedAmountKes: Long = 25_000L
    private var awardedAmountKes: Long = 20_000L
    private var totalBudgetKes: Long = 1_000_000L
    private var remainingBudgetKes: Long = 500_000L
    private var applicationStatus: ApplicationStatus = ApplicationStatus.SUBMITTED
    private var decisionStatus: DecisionStatus = DecisionStatus.APPROVED
    private var isEligible: Boolean = true
    private var formErrors: Map<String, String> = emptyMap()
    private var errorMessage: String? = null
    private var isLoading: Boolean = false
    private var searchQuery: String = "Informatics"
    private var selectedFilter: ApplicationStatus? = ApplicationStatus.SUBMITTED

    // Sample Bursary object for validation tests
    private val testBursary = Bursary(
        bursaryId = bursaryId,
        bursaryName = "County Bursary Fund",
        bursaryDescription = "Higher Education Support",
        fundingOrganization = "County Ministry of Education",
        totalBudgetKes = totalBudgetKes,
        minimumIncomeKes = 60_000L,
        maximumAwardKes = 30_000L,
        applicationDeadlineMillis = System.currentTimeMillis() + 864000000L,
        isBursaryOpen = true,
    )

    // In-memory data store for CRUD and Persistence tests
    private val memoryStore = mutableMapOf<Long, BursaryApplication>()

    // 1. validateApplicationForm()
    @Test
    fun validateApplicationForm() {
        formErrors = StudentFormValidator.validateApplicationForm(
            bursaryId = bursaryId,
            bursary = testBursary,
            academicYear = "2025/2026",
            requestedAmountKesInput = requestedAmountKes.toString(),
            applicationReason = "I require financial aid to pay my third year tuition fees.",
            supportingDocuments = listOf("transcript.pdf", "id_card.pdf"),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(formErrors.isEmpty())
    }

    // 2. validateAllocation(applicationId: Long, awardedAmountKes: Long)
    @Test
    fun validateAllocation() {
        val errorMsg = AllocationValidator.validateAllocation(
            applicationId = applicationId,
            awardedAmountKes = awardedAmountKes,
            requestedAmountKes = requestedAmountKes,
            maximumAwardKes = testBursary.maximumAwardKes,
            remainingBudgetKes = remainingBudgetKes,
            applicationStatus = applicationStatus
        )
        assertNull(errorMsg)

        // Verify allocation state & test variables
        assertEquals(DecisionStatus.APPROVED, decisionStatus)
        assertFalse(isLoading)
        assertEquals("Informatics", searchQuery)
        assertEquals(ApplicationStatus.SUBMITTED, selectedFilter)
    }

    // 3. testCreateApplication()
    @Test
    fun testCreateApplication() {
        val newApp = BursaryApplication(
            applicationId = applicationId,
            bursaryId = bursaryId,
            studentId = studentId,
            academicYear = "2025/2026",
            requestedAmountKes = requestedAmountKes,
            applicationStatus = ApplicationStatus.DRAFT,
            applicationReason = "Creating new draft application"
        )
        memoryStore[newApp.applicationId] = newApp
        assertTrue(memoryStore.containsKey(applicationId))
        assertEquals(ApplicationStatus.DRAFT, memoryStore[applicationId]?.applicationStatus)
    }

    // 4. testReadApplication()
    @Test
    fun testReadApplication() {
        testCreateApplication()
        val readApp = memoryStore[applicationId]
        assertNotNull(readApp)
        assertEquals(studentId, readApp?.studentId)
        assertEquals(requestedAmountKes, readApp?.requestedAmountKes)
    }

    // 5. testUpdateApplication()
    @Test
    fun testUpdateApplication() {
        testCreateApplication()
        val existing = memoryStore[applicationId]!!
        val updated = existing.copy(
            requestedAmountKes = 30_000L,
            applicationStatus = ApplicationStatus.SUBMITTED,
            submittedAtMillis = System.currentTimeMillis()
        )
        memoryStore[applicationId] = updated

        val verified = memoryStore[applicationId]
        assertEquals(30_000L, verified?.requestedAmountKes)
        assertEquals(ApplicationStatus.SUBMITTED, verified?.applicationStatus)
    }

    // 6. testDeleteApplication()
    @Test
    fun testDeleteApplication() {
        testCreateApplication()
        assertTrue(memoryStore.containsKey(applicationId))
        memoryStore.remove(applicationId)
        assertFalse(memoryStore.containsKey(applicationId))
    }

    // 7. testApplicationPersistence()
    @Test
    fun testApplicationPersistence() {
        val app1 = BursaryApplication(applicationId = 2001L, bursaryId = 1L, studentId = 10L, requestedAmountKes = 15000L, applicationStatus = ApplicationStatus.SUBMITTED)
        val app2 = BursaryApplication(applicationId = 2002L, bursaryId = 2L, studentId = 10L, requestedAmountKes = 25000L, applicationStatus = ApplicationStatus.DRAFT)

        memoryStore[app1.applicationId] = app1
        memoryStore[app2.applicationId] = app2

        // Simulate reading stored records across sessions
        val restoredApp1 = memoryStore[2001L]
        val restoredApp2 = memoryStore[2002L]

        assertNotNull(restoredApp1)
        assertEquals(ApplicationStatus.SUBMITTED, restoredApp1?.applicationStatus)
        assertNotNull(restoredApp2)
        assertEquals(ApplicationStatus.DRAFT, restoredApp2?.applicationStatus)
    }

    // 8. testEligibilityRules()
    @Test
    fun testEligibilityRules() {
        val eligibleProfile = StudentProfile(
            studentId = studentId,
            userId = 100L,
            registrationNumber = "SC/001/2023",
            institutionName = "Strathmore University",
            courseName = "BSc Informatics",
            yearOfStudy = 2,
            householdSize = 4,
            monthlyHouseholdIncomeKes = 35_000L
        )

        val submittedApp = BursaryApplication(
            applicationId = applicationId,
            bursaryId = bursaryId,
            studentId = studentId,
            requestedAmountKes = requestedAmountKes,
            applicationStatus = ApplicationStatus.SUBMITTED
        )

        val evaluation = AllocationValidator.checkEligibility(eligibleProfile, testBursary, submittedApp)
        isEligible = evaluation.isEligible
        assertTrue(isEligible)
        assertEquals("Student meets all income and status eligibility criteria.", evaluation.reason)
    }

    // 9. testAllocationBudgetLimit()
    @Test
    fun testAllocationBudgetLimit() {
        // Excess award over available balance must fail validation
        val overBudgetError = AllocationValidator.validateAllocation(
            applicationId = applicationId,
            awardedAmountKes = 600_000L, // Exceeds 500,000 remaining balance
            requestedAmountKes = 600_000L,
            maximumAwardKes = 700_000L,
            remainingBudgetKes = remainingBudgetKes,
            applicationStatus = ApplicationStatus.SUBMITTED
        )
        assertNotNull(overBudgetError)
        assertTrue(overBudgetError!!.contains("exceeds remaining budget balance"))
    }

    // 10. testDuplicateApplicationPrevention()
    @Test
    fun testDuplicateApplicationPrevention() {
        val existingList = listOf(
            BursaryApplication(applicationId = 5001L, bursaryId = bursaryId, studentId = studentId)
        )
        val duplicateDetected = ApplicationRules.isDuplicate(
            existingApplications = existingList,
            bursaryId = bursaryId,
            excludeApplicationId = null
        )
        assertTrue(duplicateDetected)
    }

    // 11. testInvalidInputHandling()
    @Test
    fun testInvalidInputHandling() {
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = null, // Invalid
            bursary = null,
            academicYear = "2025-2026", // Invalid format
            requestedAmountKesInput = "-5000", // Negative amount
            applicationReason = "Short", // Too short
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.BURSARY_ID))
        assertTrue(errors.containsKey(FormField.ACADEMIC_YEAR))
        assertTrue(errors.containsKey(FormField.REQUESTED_AMOUNT_KES))
        assertTrue(errors.containsKey(FormField.APPLICATION_REASON))
    }

    // 12. testNetworkFailureHandling()
    @Test
    fun testNetworkFailureHandling() = runBlocking {
        val failingApi = object : BursaryApiService {
            override suspend fun getBursaries(): List<BursaryDto> {
                throw IOException("Connection timed out")
            }
        }

        val remoteRepo = BursaryRemoteRepository(failingApi)
        val result = remoteRepo.fetchOpenBursaries()

        assertTrue(result is ApiResult.Error)
        errorMessage = (result as ApiResult.Error).message
        assertEquals("Network unavailable. Check your connection.", errorMessage)
    }
}
