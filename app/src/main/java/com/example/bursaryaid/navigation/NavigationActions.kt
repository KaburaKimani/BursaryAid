package com.example.bursaryaid.navigation

import androidx.navigation.NavController

fun NavController.navigateToDashboard(isAdmin: Boolean) {
    val destination = if (isAdmin) Routes.ADMIN_DASHBOARD else Routes.STUDENT_DASHBOARD
    navigate(destination) {
        popUpTo(Routes.LOGIN) { inclusive = true }
    }
}

fun NavController.navigateToApplicationDetails(applicationId: Long) {
    navigate(Routes.details(applicationId))
}

fun NavController.navigateToBursaryDetails(bursaryId: Long) {
    navigate(Routes.bursaryDetails(bursaryId))
}