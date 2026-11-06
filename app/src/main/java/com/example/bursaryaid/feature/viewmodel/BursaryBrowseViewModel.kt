package com.example.bursaryaid.feature.bursary.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/** TEMPORARY display model. Replace with the shared BursaryOpportunity once the repository is ready. */
data class BursarySummary(
    val bursaryId: Long,
    val bursaryName: String,
    val bursaryDescription: String,
    val fundingOrganization: String,
    val totalBudgetKes: Long,
    val minimumIncomeKes: Long,
    val maximumAwardKes: Long,
    val applicationDeadlineMillis: Long,
    val isBursaryOpen: Boolean
)

class BursaryBrowseViewModel : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    // TEMPORARY sample data until the real bursary repository is connected
    private val allBursaries = listOf(
        BursarySummary(
            bursaryId = 1L,
            bursaryName = "County Education Fund",
            bursaryDescription = "Support with tuition fees for students from low-income households in the county.",
            fundingOrganization = "County Government",
            totalBudgetKes = 5_000_000L,
            minimumIncomeKes = 0L,
            maximumAwardKes = 50_000L,
            applicationDeadlineMillis = 1801353600000L,
            isBursaryOpen = true
        ),
        BursarySummary(
            bursaryId = 2L,
            bursaryName = "Women in STEM Scholarship",
            bursaryDescription = "Financial aid for female students pursuing science, technology, engineering or mathematics.",
            fundingOrganization = "STEM Foundation",
            totalBudgetKes = 3_000_000L,
            minimumIncomeKes = 0L,
            maximumAwardKes = 80_000L,
            applicationDeadlineMillis = 1803772800000L,
            isBursaryOpen = true
        ),
        BursarySummary(
            bursaryId = 3L,
            bursaryName = "Needy Student Support Grant",
            bursaryDescription = "Emergency fee support for students facing sudden financial hardship.",
            fundingOrganization = "Alumni Association",
            totalBudgetKes = 1_500_000L,
            minimumIncomeKes = 0L,
            maximumAwardKes = 30_000L,
            applicationDeadlineMillis = 1797292800000L,
            isBursaryOpen = true
        ),
        BursarySummary(
            bursaryId = 4L,
            bursaryName = "Orphans and Vulnerable Children Bursary",
            bursaryDescription = "Fee support for orphaned and vulnerable students in higher education.",
            fundingOrganization = "Community Trust",
            totalBudgetKes = 2_000_000L,
            minimumIncomeKes = 0L,
            maximumAwardKes = 60_000L,
            applicationDeadlineMillis = 1790000000000L,
            isBursaryOpen = false
        )
    )

    val filteredBursaries: List<BursarySummary>
        get() = if (searchQuery.isBlank()) {
            allBursaries
        } else {
            allBursaries.filter {
                it.bursaryName.contains(searchQuery, ignoreCase = true) ||
                        it.fundingOrganization.contains(searchQuery, ignoreCase = true)
            }
        }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun getBursaryById(bursaryId: Long): BursarySummary? =
        allBursaries.firstOrNull { it.bursaryId == bursaryId }
}