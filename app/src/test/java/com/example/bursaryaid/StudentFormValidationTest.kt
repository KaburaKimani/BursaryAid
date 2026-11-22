package com.example.bursaryaid

import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.logic.StudentFormValidator
import com.example.bursaryaid.model.Bursary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentFormValidationTest {

    private val validBursary = Bursary(
        bursaryId = 1L,
        bursaryName = "County Higher Education Bursary",
        bursaryDescription = "Financial aid for needy university students",
        fundingOrganization = "County Government",
        totalBudgetKes = 5_000_000L,
        minimumIncomeKes = 50_000L,
        maximumAwardKes = 30_000L,
        applicationDeadlineMillis = System.currentTimeMillis() + 864000000L, // 10 days in future
        isBursaryOpen = true
    )

    @Test
    fun validateApplicationForm_validInput_returnsNoErrors() {
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = 1L,
            bursary = validBursary,
            academicYear = "2025/2026",
            requestedAmountKesInput = "20000",
            applicationReason = "I require financial aid to clear tuition balance for semester 2.",
            supportingDocuments = listOf("doc1.pdf", "doc2.pdf"),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun validateApplicationForm_nullBursary_returnsError() {
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = null,
            bursary = null,
            academicYear = "2025/2026",
            requestedAmountKesInput = "20000",
            applicationReason = "Financial aid needed for tuition.",
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.BURSARY_ID))
        assertEquals("Select a bursary.", errors[FormField.BURSARY_ID])
    }

    @Test
    fun validateApplicationForm_closedBursary_returnsError() {
        val closedBursary = validBursary.copy(isBursaryOpen = false)
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = 1L,
            bursary = closedBursary,
            academicYear = "2025/2026",
            requestedAmountKesInput = "20000",
            applicationReason = "Financial aid needed for tuition.",
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.BURSARY_ID))
        assertEquals("This bursary is no longer open for applications.", errors[FormField.BURSARY_ID])
    }

    @Test
    fun validateApplicationForm_expiredDeadline_returnsError() {
        val expiredBursary = validBursary.copy(applicationDeadlineMillis = System.currentTimeMillis() - 10000L)
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = 1L,
            bursary = expiredBursary,
            academicYear = "2025/2026",
            requestedAmountKesInput = "20000",
            applicationReason = "Financial aid needed for tuition.",
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.BURSARY_ID))
        assertEquals("The deadline for this bursary has passed.", errors[FormField.BURSARY_ID])
    }

    @Test
    fun validateApplicationForm_invalidAcademicYearFormat_returnsError() {
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = 1L,
            bursary = validBursary,
            academicYear = "2025-2026", // Wrong delimiter
            requestedAmountKesInput = "20000",
            applicationReason = "Financial aid needed for tuition.",
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.ACADEMIC_YEAR))
        assertEquals("Use the format 2026/2027 (consecutive years).", errors[FormField.ACADEMIC_YEAR])
    }

    @Test
    fun validateApplicationForm_nonConsecutiveAcademicYear_returnsError() {
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = 1L,
            bursary = validBursary,
            academicYear = "2024/2026", // Non-consecutive years
            requestedAmountKesInput = "20000",
            applicationReason = "Financial aid needed for tuition.",
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.ACADEMIC_YEAR))
    }

    @Test
    fun validateApplicationForm_nonNumericRequestedAmount_returnsError() {
        val errors = StudentFormValidator.validateApplicationForm(
            bursaryId = 1L,
            bursary = validBursary,
            academicYear = "2025/2026",
            requestedAmountKesInput = "20,000 KES", // Contains invalid characters
            applicationReason = "Financial aid needed for tuition.",
            supportingDocuments = emptyList(),
            nowMillis = System.currentTimeMillis(),
            requireComplete = true
        )
        assertTrue(errors.containsKey(FormField.REQUESTED_AMOUNT_KES))
    }

    @Test
    fun testInvalidInputHandling() {
        // Test student profile form invalid inputs
        val profileErrors = StudentFormValidator.validateProfileForm(
            registrationNumber = "", // Empty reg number
            institutionName = "  ", // Blank institution
            courseName = "", // Empty course
            yearOfStudyInput = "10", // Year out of bounds (1..8)
            householdSizeInput = "0", // Household size < 1
            monthlyHouseholdIncomeKesInput = "abc", // Non-numeric income
            guardianName = "John Doe",
            guardianPhoneNumber = "123" // Invalid phone number
        )

        assertTrue(profileErrors.containsKey(FormField.REGISTRATION_NUMBER))
        assertTrue(profileErrors.containsKey(FormField.INSTITUTION_NAME))
        assertTrue(profileErrors.containsKey(FormField.COURSE_NAME))
        assertTrue(profileErrors.containsKey(FormField.YEAR_OF_STUDY))
        assertTrue(profileErrors.containsKey(FormField.HOUSEHOLD_SIZE))
        assertTrue(profileErrors.containsKey(FormField.MONTHLY_HOUSEHOLD_INCOME_KES))
        assertTrue(profileErrors.containsKey(FormField.GUARDIAN_PHONE_NUMBER))

        // Phone validation helper checks
        assertTrue(StudentFormValidator.isValidKenyanPhone("0712345678"))
        assertTrue(StudentFormValidator.isValidKenyanPhone("+254712345678"))
        assertTrue(StudentFormValidator.isValidKenyanPhone("0112345678"))
        assertFalse(StudentFormValidator.isValidKenyanPhone("0201234567"))
        assertFalse(StudentFormValidator.isValidKenyanPhone("12345"))
    }
}
