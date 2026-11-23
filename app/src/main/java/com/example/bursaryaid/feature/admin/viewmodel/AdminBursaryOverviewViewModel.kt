package com.example.bursaryaid.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.repository.BursaryApplicationRepository
import com.example.bursaryaid.repository.BursaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BursaryOverviewItem(
    val bursary: Bursary,
    val applicantCount: Int,
    val pendingCount: Int,
    val approvedCount: Int,
    val rejectedCount: Int
)

class AdminBursaryOverviewViewModel(
    bursaryRepository: BursaryRepository,
    applicationRepository: BursaryApplicationRepository
) : ViewModel() {
    val bursaryOverviews: StateFlow<List<BursaryOverviewItem>> =
        combine(bursaryRepository.getAllBursaries(), applicationRepository.getAllApplications()) { bursaries, applications ->
            bursaries.map { bursary ->
                val submitted = applications.filter {
                    it.bursaryId == bursary.bursaryId && it.applicationStatus != ApplicationStatus.DRAFT
                }
                fun count(statuses: Set<ApplicationStatus>) = submitted
                    .filter { it.applicationStatus in statuses }.map { it.studentId }.distinct().size

                BursaryOverviewItem(
                    bursary = bursary,
                    applicantCount = submitted.map { it.studentId }.distinct().size,
                    pendingCount = count(setOf(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.NEEDS_INFORMATION)),
                    approvedCount = count(setOf(ApplicationStatus.APPROVED)),
                    rejectedCount = count(setOf(ApplicationStatus.REJECTED))
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}

fun adminBursaryOverviewViewModelFactory(
    bursaryRepository: BursaryRepository,
    applicationRepository: BursaryApplicationRepository
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminBursaryOverviewViewModel::class.java)) {
            return AdminBursaryOverviewViewModel(bursaryRepository, applicationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}