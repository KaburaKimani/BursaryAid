package com.example.bursaryaid.feature.student.logic

import com.example.bursaryaid.data.model.AppConstants
import com.example.bursaryaid.data.model.BursaryOpportunity

/**
 * Pure (Android-free) validation so Member 6 can unit test it directly.
 * Every function returns a formErrors map: field key -> message. Empty map = valid.
 * Text-field values arrive as Strings; numeric parsing happens here.
 */
object StudentFormValidator {

    // Kenyan mobile numbers: 07xxxxxxxx, 01xxxxxxxx, +2547xxxxxxxx, +2541xxxxxxxx
    private val KENYAN_PHONE_REGEX = Regex("^(?:\\+254|0)[17]\\d{8}$")
    private val ACADEMIC_YEAR_REGEX = Regex("^(\\d{4})/(\\d{4})$")
    private val DIGITS_ONLY_REGEX = Regex("^\\d+$")

    fun validateProfileForm(
        registrationNumber: String,
        institutionName: String,
        courseName: String,
        yearOfStudyInput: String,
        householdSizeInput: String,
        monthlyHouseholdIncomeKesInput: String,
        guardianName: String,
        guardianPhoneNumber: String
    ): Map<String, String> {
        val formErrors = linkedMapOf<String, String>()

        val regNo = registrationNumber.trim()
        when {
            regNo.isEmpty() -> formErrors[FormField.REGISTRATION_NUMBER] = "Enter your registration number."
            regNo.length > StudentFeatureConstants.MAX_REGISTRATION_NUMBER_LENGTH ->
                formErrors[FormField.REGISTRATION_NUMBER] = "Registration number is too long."
        }
        if (institutionName.isBlank()) formErrors[FormField.INSTITUTION_NAME] = "Enter your institution."
        if (courseName.isBlank()) formErrors[FormField.COURSE_NAME] = "Enter your course or programme."

        val year = yearOfStudyInput.trim().toIntOrNull()
        if (year == null || year < AppConstants.MIN_YEAR_OF_STUDY || year > AppConstants.MAX_YEAR_OF_STUDY) {
            formErrors[FormField.YEAR_OF_STUDY] =
                "Enter a year between ${AppConstants.MIN_YEAR_OF_STUDY} and ${AppConstants.MAX_YEAR_OF_STUDY}."
        }

        val household = householdSizeInput.trim().toIntOrNull()
        if (household == null || household < 1 || household > StudentFeatureConstants.MAX_HOUSEHOLD_SIZE) {
            formErrors[FormField.HOUSEHOLD_SIZE] =
                "Enter a household size between 1 and ${StudentFeatureConstants.MAX_HOUSEHOLD_SIZE}."
        }

        val incomeText = monthlyHouseholdIncomeKesInput.trim()
        val income = incomeText.toLongOrNull()
        if (incomeText.isEmpty() || !DIGITS_ONLY_REGEX.matches(incomeText) || income == null ||
            income > StudentFeatureConstants.MAX_AMOUNT_KES
        ) {
            formErrors[FormField.MONTHLY_HOUSEHOLD_INCOME_KES] =
                "Enter monthly household income in whole shillings (0 if none), digits only."
        }

        // Guardian details are optional, but if one is given both are required.
        val hasGuardianName = guardianName.isNotBlank()
        val hasGuardianPhone = guardianPhoneNumber.isNotBlank()
        if (hasGuardianName || hasGuardianPhone) {
            if (!hasGuardianName) formErrors[FormField.GUARDIAN_NAME] = "Enter the guardian's name."
            if (!hasGuardianPhone) {
                formErrors[FormField.GUARDIAN_PHONE_NUMBER] = "Enter the guardian's phone number."
            } else if (!isValidKenyanPhone(guardianPhoneNumber)) {
                formErrors[FormField.GUARDIAN_PHONE_NUMBER] =
                    "Enter a valid phone number, e.g. 0712345678 or +254712345678."
            }
        }
        return formErrors
    }

    /**
     * @param requireComplete true when submitting (or editing a NEEDS_INFORMATION application);
     *                        false when saving a draft, where only the bursary choice is mandatory
     *                        and anything typed must still be well-formed.
     * @param bursary         the selected bursary, or null if it is not in the open list.
     * @param enforceBursaryOpen whether the bursary must still be open and before its deadline.
     *                        True for a first submission; false when answering a NEEDS_INFORMATION
     *                        request, which must stay possible after the bursary has closed.
     */
    fun validateApplicationForm(
        bursaryId: Long?,
        bursary: BursaryOpportunity?,
        academicYear: String,
        requestedAmountKesInput: String,
        applicationReason: String,
        supportingDocuments: List<String>,
        nowMillis: Long,
        requireComplete: Boolean,
        enforceBursaryOpen: Boolean = requireComplete
    ): Map<String, String> {
        val formErrors = linkedMapOf<String, String>()

        if (bursaryId == null) {
            formErrors[FormField.BURSARY_ID] = "Select a bursary."
        } else if (enforceBursaryOpen) {
            if (bursary == null || !bursary.isBursaryOpen) {
                formErrors[FormField.BURSARY_ID] = "This bursary is no longer open for applications."
            } else if (bursary.applicationDeadlineMillis < nowMillis) {
                formErrors[FormField.BURSARY_ID] = "The deadline for this bursary has passed."
            }
        }

        val year = academicYear.trim()
        if (year.isEmpty()) {
            if (requireComplete) formErrors[FormField.ACADEMIC_YEAR] = "Enter the academic year, e.g. 2026/2027."
        } else {
            val match = ACADEMIC_YEAR_REGEX.matchEntire(year)
            val first = match?.groupValues?.get(1)?.toIntOrNull()
            val second = match?.groupValues?.get(2)?.toIntOrNull()
            if (first == null || second == null || second != first + 1) {
                formErrors[FormField.ACADEMIC_YEAR] = "Use the format 2026/2027 (consecutive years)."
            }
        }

        val amountText = requestedAmountKesInput.trim()
        if (amountText.isEmpty()) {
            if (requireComplete) formErrors[FormField.REQUESTED_AMOUNT_KES] = "Enter the amount you are requesting."
        } else {
            val amount = if (DIGITS_ONLY_REGEX.matches(amountText)) amountText.toLongOrNull() else null
            if (amount == null) {
                formErrors[FormField.REQUESTED_AMOUNT_KES] =
                    "Use whole shillings, digits only (e.g. 15000)."
            } else if (amount <= 0L) {
                formErrors[FormField.REQUESTED_AMOUNT_KES] = "The amount must be greater than zero."
            } else if (amount > StudentFeatureConstants.MAX_AMOUNT_KES) {
                formErrors[FormField.REQUESTED_AMOUNT_KES] = "That amount is too large."
            }
        }

        val reason = applicationReason.trim()
        if (reason.isEmpty()) {
            if (requireComplete) formErrors[FormField.APPLICATION_REASON] = "Explain why you need this support."
        } else if (requireComplete && reason.length < StudentFeatureConstants.MIN_APPLICATION_REASON_LENGTH) {
            formErrors[FormField.APPLICATION_REASON] =
                "Please write at least ${StudentFeatureConstants.MIN_APPLICATION_REASON_LENGTH} characters."
        } else if (reason.length > StudentFeatureConstants.MAX_APPLICATION_REASON_LENGTH) {
            formErrors[FormField.APPLICATION_REASON] =
                "Keep this under ${StudentFeatureConstants.MAX_APPLICATION_REASON_LENGTH} characters."
        }

        if (supportingDocuments.size > StudentFeatureConstants.MAX_SUPPORTING_DOCUMENTS) {
            formErrors[FormField.SUPPORTING_DOCUMENTS] =
                "You can attach at most ${StudentFeatureConstants.MAX_SUPPORTING_DOCUMENTS} documents."
        }
        return formErrors
    }

    fun isValidKenyanPhone(phoneNumber: String): Boolean =
        KENYAN_PHONE_REGEX.matches(phoneNumber.trim().replace(" ", ""))
}
