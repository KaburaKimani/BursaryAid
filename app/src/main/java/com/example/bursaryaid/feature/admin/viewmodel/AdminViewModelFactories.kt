package com.example.bursaryaid.feature.admin.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bursaryaid.feature.admin.data.AdminReviewRepository

/**
 * Plain factory, in the same style as the student factories, so Member 1 can call it
 * where the admin list screen is created.
 */
fun adminApplicationsViewModelFactory(
    adminReviewRepository: AdminReviewRepository
): ViewModelProvider.Factory = viewModelFactory {
    initializer { AdminApplicationsViewModel(adminReviewRepository) }
}