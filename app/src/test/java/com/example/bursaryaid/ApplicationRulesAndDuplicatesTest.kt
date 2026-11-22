package com.example.bursaryaid

import com.example.bursaryaid.feature.student.logic.ApplicationRules
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.BursaryApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationRulesAndDuplicatesTest {

    @Test
    fun testDuplicateApplicationPrevention() {
        val existingApplications = listOf(
            BursaryApplication(
                applicationId = 101L,
                bursaryId = 1L,
                studentId = 50L,
                applicationStatus = ApplicationStatus.SUBMITTED
            )
        )

        // Attempting to apply again for bursaryId 1 should be detected as a duplicate
        val isDuplicateNew = ApplicationRules.isDuplicate(
            existingApplications = existingApplications,
            bursaryId = 1L,
            excludeApplicationId = null
        )
        assertTrue(isDuplicateNew)

        // Editing the existing application (excluding itself) should NOT be flagged as duplicate
        val isDuplicateSelf = ApplicationRules.isDuplicate(
            existingApplications = existingApplications,
            bursaryId = 1L,
            excludeApplicationId = 101L
        )
        assertFalse(isDuplicateSelf)

        // Applying for a different bursary (bursaryId 2) should NOT be flagged as duplicate
        val isDuplicateOtherBursary = ApplicationRules.isDuplicate(
            existingApplications = existingApplications,
            bursaryId = 2L,
            excludeApplicationId = null
        )
        assertFalse(isDuplicateOtherBursary)
    }

    @Test
    fun testEditableStatuses() {
        assertTrue(ApplicationRules.canEdit(ApplicationStatus.DRAFT))
        assertTrue(ApplicationRules.canEdit(ApplicationStatus.NEEDS_INFORMATION))

        assertFalse(ApplicationRules.canEdit(ApplicationStatus.SUBMITTED))
        assertFalse(ApplicationRules.canEdit(ApplicationStatus.UNDER_REVIEW))
        assertFalse(ApplicationRules.canEdit(ApplicationStatus.APPROVED))
        assertFalse(ApplicationRules.canEdit(ApplicationStatus.REJECTED))
    }

    @Test
    fun testDeleteDraftOnly() {
        assertTrue(ApplicationRules.canDelete(ApplicationStatus.DRAFT))

        assertFalse(ApplicationRules.canDelete(ApplicationStatus.NEEDS_INFORMATION))
        assertFalse(ApplicationRules.canDelete(ApplicationStatus.SUBMITTED))
        assertFalse(ApplicationRules.canDelete(ApplicationStatus.UNDER_REVIEW))
        assertFalse(ApplicationRules.canDelete(ApplicationStatus.APPROVED))
        assertFalse(ApplicationRules.canDelete(ApplicationStatus.REJECTED))
    }

    @Test
    fun testFilterByStatus() {
        val app1 = BursaryApplication(applicationId = 1L, bursaryId = 1L, studentId = 50L, applicationStatus = ApplicationStatus.DRAFT, updatedAtMillis = 1000L)
        val app2 = BursaryApplication(applicationId = 2L, bursaryId = 1L, studentId = 50L, applicationStatus = ApplicationStatus.SUBMITTED, updatedAtMillis = 2000L)
        val app3 = BursaryApplication(applicationId = 3L, bursaryId = 2L, studentId = 50L, applicationStatus = ApplicationStatus.APPROVED, updatedAtMillis = 3000L)

        val allApps = listOf(app1, app2, app3)

        // Null filter returns all apps sorted descending by updatedAtMillis
        val filteredAll = ApplicationRules.filterByStatus(allApps, null)
        assertEquals(3, filteredAll.size)
        assertEquals(3L, filteredAll[0].applicationId)

        // Filter by SUBMITTED returns only app2
        val filteredSubmitted = ApplicationRules.filterByStatus(allApps, ApplicationStatus.SUBMITTED)
        assertEquals(1, filteredSubmitted.size)
        assertEquals(2L, filteredSubmitted[0].applicationId)
    }
}
