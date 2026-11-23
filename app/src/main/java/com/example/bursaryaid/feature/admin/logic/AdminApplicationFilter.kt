package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus

/**
 * One row of the admin list, as plain values. An adapter builds these from the database,
 * so the list logic never depends on the entity classes. Amounts are whole shillings.
 */
data class AdminApplicationItem(
    val applicationId: Long,
    val studentId: Long,
    val studentName: String,
    val admissionNumber: String,
    val bursaryId: Long,
    val bursaryName: String,
    val requestedAmountKes: Long,
    val applicationStatus: ApplicationStatus,
    val submittedAtMillis: Long
)

object AdminApplicationFilter {

    /** DRAFT applications never reach the administrator. */
    private val HIDDEN_STATUSES = setOf(ApplicationStatus.DRAFT)
    /** Statuses an administrator can filter by; the chips on the list screen come from this. */
    val FILTERABLE_STATUSES: List<ApplicationStatus> =
        ApplicationStatus.values().filter { it !in HIDDEN_STATUSES }

    /** Hides drafts, applies the status filter and the search, and lists the newest first. */
    fun apply(
        items: List<AdminApplicationItem>,
        searchQuery: String,
        selectedFilter: ApplicationStatus?
    ): List<AdminApplicationItem> {
        val query = searchQuery.trim()
        return items
            .filter { it.applicationStatus !in HIDDEN_STATUSES }
            .filter { selectedFilter == null || it.applicationStatus == selectedFilter }
            .filter { query.isEmpty() || it.matches(query) }
            .sortedByDescending { it.submittedAtMillis }
    }

    private fun AdminApplicationItem.matches(query: String): Boolean =
        studentName.contains(query, ignoreCase = true) ||
                admissionNumber.contains(query, ignoreCase = true) ||
                bursaryName.contains(query, ignoreCase = true)
}