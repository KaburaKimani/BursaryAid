package com.example.bursaryaid.feature.admin.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bursaryaid.feature.admin.data.AdminReviewRepository
import com.example.bursaryaid.feature.admin.logic.DecisionEvent

/**
 * Plain factories, in the same style as the student factories, so Member 1 can call
 * them where the admin screens are created.
 */
fun adminApplicationsViewModelFactory(
    adminReviewRepository: AdminReviewRepository,
    bursaryId: Long? = null
): ViewModelProvider.Factory = viewModelFactory {
    initializer { AdminApplicationsViewModel(adminReviewRepository, bursaryId) }
}

fun adminReviewViewModelFactory(
    selectedApplicationId: Long,
    reviewedByUserId: Long,
    adminReviewRepository: AdminReviewRepository,
    onDecisionRecorded: suspend (DecisionEvent) -> Unit = {}
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        AdminReviewViewModel(selectedApplicationId, reviewedByUserId, adminReviewRepository, onDecisionRecorded)
    }
}