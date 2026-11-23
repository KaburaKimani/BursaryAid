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
    val applicantCount: Int
)

class AdminBursaryOverviewViewModel(
    bursaryRepository: BursaryRepository,
    applicationRepository: BursaryApplicationRepository
) : ViewModel() {

    val bursaryOverviews: StateFlow<List<BursaryOverviewItem>> =
        combine(
            bursaryRepository.getAllBursaries(),
            applicationRepository.getAllApplications()
        ) { bursaries, applications ->
            val counts = applications
                .filter { it.applicationStatus != ApplicationStatus.DRAFT }
                .groupBy { it.bursaryId }
                .mapValues { entry -> entry.value.size }

            bursaries.map { bursary ->
                BursaryOverviewItem(
                    bursary = bursary,
                    applicantCount = counts[bursary.bursaryId] ?: 0
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
