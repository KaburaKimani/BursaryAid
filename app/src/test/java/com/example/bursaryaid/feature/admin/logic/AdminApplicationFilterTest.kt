package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminApplicationFilterTest {

    private fun item(
        id: Long,
        status: ApplicationStatus = ApplicationStatus.SUBMITTED,
        studentName: String = "Mary Wanjiku",
        admissionNumber: String = "ADM001",
        bursaryName: String = "County Bursary",
        submittedAtMillis: Long = id * 1_000L
    ) = AdminApplicationItem(
        applicationId = id,
        studentId = id,
        studentName = studentName,
        admissionNumber = admissionNumber,
        bursaryId = 1L,
        bursaryName = bursaryName,
        requestedAmountKes = 30_000L,
        applicationStatus = status,
        submittedAtMillis = submittedAtMillis
    )

    private fun ids(list: List<AdminApplicationItem>) = list.map { it.applicationId }

    @Test
    fun drafts_areHidden() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1, ApplicationStatus.DRAFT), item(2)), "", null
        )
        assertEquals(listOf(2L), ids(result))
    }

    @Test
    fun statusFilter_keepsOnlyThatStatus() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1), item(2, ApplicationStatus.UNDER_REVIEW)),
            "",
            ApplicationStatus.UNDER_REVIEW
        )
        assertEquals(listOf(2L), ids(result))
    }

    @Test
    fun filterSelectingDraft_stillShowsNothing() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1, ApplicationStatus.DRAFT)), "", ApplicationStatus.DRAFT
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun search_matchesStudentName_ignoringCase() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1, studentName = "Mary Wanjiku"), item(2, studentName = "John Kamau")),
            "wanjiku",
            null
        )
        assertEquals(listOf(1L), ids(result))
    }

    @Test
    fun search_matchesAdmissionNumber() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1, admissionNumber = "ADM001"), item(2, admissionNumber = "ADM002")),
            "adm002",
            null
        )
        assertEquals(listOf(2L), ids(result))
    }

    @Test
    fun search_matchesBursaryName() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1, bursaryName = "County Bursary"), item(2, bursaryName = "Orphans Fund")),
            "orphans",
            null
        )
        assertEquals(listOf(2L), ids(result))
    }

    @Test
    fun blankSearch_returnsEverythingExceptDrafts() {
        val result = AdminApplicationFilter.apply(
            listOf(item(1), item(2), item(3, ApplicationStatus.DRAFT)), "   ", null
        )
        assertEquals(2, result.size)
    }

    @Test
    fun results_areNewestFirst() {
        val result = AdminApplicationFilter.apply(
            listOf(
                item(1, submittedAtMillis = 1_000L),
                item(2, submittedAtMillis = 3_000L),
                item(3, submittedAtMillis = 2_000L)
            ),
            "",
            null
        )
        assertEquals(listOf(2L, 3L, 1L), ids(result))
    }

    @Test
    fun searchWithNoMatch_returnsEmptyList() {
        val result = AdminApplicationFilter.apply(listOf(item(1)), "zzz", null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun searchAndFilter_applyTogether() {
        val result = AdminApplicationFilter.apply(
            listOf(
                item(1, ApplicationStatus.SUBMITTED, studentName = "Mary Wanjiku"),
                item(2, ApplicationStatus.UNDER_REVIEW, studentName = "Mary Achieng"),
                item(3, ApplicationStatus.UNDER_REVIEW, studentName = "John Kamau")
            ),
            "mary",
            ApplicationStatus.UNDER_REVIEW
        )
        assertEquals(listOf(2L), ids(result))
    }
}