package com.example.bursaryaid.model

object AppConstants {
    const val DATABASE_NAME = "bursary_aid_database"
    const val CURRENCY_CODE = "KES"
    const val MIN_YEAR_OF_STUDY = 1
    const val MAX_YEAR_OF_STUDY = 8
    const val MAX_APPLICATIONS_PER_BURSARY = 1
}

enum class UserRole {
    STUDENT,
    ADMIN
}

enum class ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    NEEDS_INFORMATION,
    APPROVED,
    REJECTED
}

enum class DecisionStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class NotificationType {
    APPLICATION_SUBMITTED,
    APPLICATION_STATUS_CHANGED,
    INFORMATION_REQUESTED,
    BURSARY_DEADLINE_REMINDER,
    ALLOCATION_CONFIRMED
}