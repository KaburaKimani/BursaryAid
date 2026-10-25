package com.example.bursaryaid.feature.student.logic

/**
 * Constants private to Member 2's module. Shared constants (CURRENCY_CODE,
 * MIN/MAX_YEAR_OF_STUDY, MAX_APPLICATIONS_PER_BURSARY, DATABASE_NAME) stay in
 * Member 3's AppConstants and are imported, never copied.
 *
 * All values below are PROPOSED defaults, not lecturer-specified rules.
 */
object StudentFeatureConstants {
    const val MIN_APPLICATION_REASON_LENGTH = 20
    const val MAX_APPLICATION_REASON_LENGTH = 1000
    const val MAX_SUPPORTING_DOCUMENTS = 5
    const val MAX_HOUSEHOLD_SIZE = 30
    const val MAX_AMOUNT_KES = 10_000_000L
    const val MAX_REGISTRATION_NUMBER_LENGTH = 30
}

/** Keys of the formErrors map. Each key equals the property name it describes. */
object FormField {
    const val REGISTRATION_NUMBER = "registrationNumber"
    const val INSTITUTION_NAME = "institutionName"
    const val COURSE_NAME = "courseName"
    const val YEAR_OF_STUDY = "yearOfStudy"
    const val HOUSEHOLD_SIZE = "householdSize"
    const val MONTHLY_HOUSEHOLD_INCOME_KES = "monthlyHouseholdIncomeKes"
    const val GUARDIAN_NAME = "guardianName"
    const val GUARDIAN_PHONE_NUMBER = "guardianPhoneNumber"
    const val BURSARY_ID = "bursaryId"
    const val ACADEMIC_YEAR = "academicYear"
    const val REQUESTED_AMOUNT_KES = "requestedAmountKes"
    const val APPLICATION_REASON = "applicationReason"
    const val SUPPORTING_DOCUMENTS = "supportingDocuments"
}
