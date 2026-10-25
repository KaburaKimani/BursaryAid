package com.example.bursaryaid.feature.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.data.model.StudentProfile
import com.example.bursaryaid.data.model.StudentProfileRepository
import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.logic.StudentFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Text fields are held as Strings while the user types, hence the "Input" suffix on the numeric ones;
 * they are parsed into the shared model types (Int/Long) only when saving.
 */
data class StudentProfileUiState(
    val studentId: Long = 0L,
    val userId: Long,
    val registrationNumber: String = "",
    val institutionName: String = "",
    val courseName: String = "",
    val yearOfStudyInput: String = "",
    val householdSizeInput: String = "",
    val monthlyHouseholdIncomeKesInput: String = "",
    val guardianName: String = "",
    val guardianPhoneNumber: String = "",
    val hasExistingProfile: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val formErrors: Map<String, String> = emptyMap(),
    val isSaved: Boolean = false
)

class StudentProfileViewModel(
    private val userId: Long,
    private val studentProfileRepository: StudentProfileRepository,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState(userId = userId))
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        getStudentProfile(userId)
    }

    /** READ: loads the profile (if one exists) into the form. */
    fun getStudentProfile(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profile = studentProfileRepository.getStudentProfile(userId)
                _uiState.update { state ->
                    if (profile == null) state.copy(isLoading = false)
                    else state.copy(
                        studentId = profile.studentId,
                        registrationNumber = profile.registrationNumber,
                        institutionName = profile.institutionName,
                        courseName = profile.courseName,
                        yearOfStudyInput = profile.yearOfStudy.toString(),
                        householdSizeInput = profile.householdSize.toString(),
                        monthlyHouseholdIncomeKesInput = profile.monthlyHouseholdIncomeKes.toString(),
                        guardianName = profile.guardianName,
                        guardianPhoneNumber = profile.guardianPhoneNumber,
                        hasExistingProfile = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load your profile. Please try again.") }
            }
        }
    }

    /** CREATE (first save) or UPDATE (later saves) of the profile. */
    fun saveStudentProfile() {
        val state = _uiState.value
        if (state.isLoading) return

        val formErrors = StudentFormValidator.validateProfileForm(
            registrationNumber = state.registrationNumber,
            institutionName = state.institutionName,
            courseName = state.courseName,
            yearOfStudyInput = state.yearOfStudyInput,
            householdSizeInput = state.householdSizeInput,
            monthlyHouseholdIncomeKesInput = state.monthlyHouseholdIncomeKesInput,
            guardianName = state.guardianName,
            guardianPhoneNumber = state.guardianPhoneNumber
        )
        if (formErrors.isNotEmpty()) {
            _uiState.update { it.copy(formErrors = formErrors, errorMessage = "Please fix the highlighted fields.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formErrors = emptyMap(), errorMessage = null, successMessage = null) }
            try {
                val profile = StudentProfile(
                    studentId = state.studentId,
                    userId = userId,
                    registrationNumber = state.registrationNumber.trim(),
                    institutionName = state.institutionName.trim(),
                    courseName = state.courseName.trim(),
                    yearOfStudy = state.yearOfStudyInput.trim().toInt(),
                    householdSize = state.householdSizeInput.trim().toInt(),
                    monthlyHouseholdIncomeKes = state.monthlyHouseholdIncomeKesInput.trim().toLong(),
                    guardianName = state.guardianName.trim(),
                    guardianPhoneNumber = state.guardianPhoneNumber.trim().replace(" ", ""),
                    profileUpdatedAtMillis = currentTimeMillis()
                )
                val savedStudentId = if (state.hasExistingProfile) {
                    studentProfileRepository.updateStudentProfile(profile)
                    state.studentId
                } else {
                    studentProfileRepository.insertStudentProfile(profile)
                }
                _uiState.update {
                    it.copy(
                        studentId = savedStudentId, hasExistingProfile = true, isLoading = false,
                        successMessage = "Profile saved.", isSaved = true
                    )
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not save your profile. Please try again.") }
            }
        }
    }

    fun onRegistrationNumberChanged(value: String) = edit(FormField.REGISTRATION_NUMBER) { it.copy(registrationNumber = value) }
    fun onInstitutionNameChanged(value: String) = edit(FormField.INSTITUTION_NAME) { it.copy(institutionName = value) }
    fun onCourseNameChanged(value: String) = edit(FormField.COURSE_NAME) { it.copy(courseName = value) }
    fun onYearOfStudyChanged(value: String) = edit(FormField.YEAR_OF_STUDY) { it.copy(yearOfStudyInput = value.filter(Char::isDigit)) }
    fun onHouseholdSizeChanged(value: String) = edit(FormField.HOUSEHOLD_SIZE) { it.copy(householdSizeInput = value.filter(Char::isDigit)) }
    fun onMonthlyHouseholdIncomeKesChanged(value: String) = edit(FormField.MONTHLY_HOUSEHOLD_INCOME_KES) { it.copy(monthlyHouseholdIncomeKesInput = value.filter(Char::isDigit)) }
    fun onGuardianNameChanged(value: String) = edit(FormField.GUARDIAN_NAME) { it.copy(guardianName = value) }
    fun onGuardianPhoneNumberChanged(value: String) = edit(FormField.GUARDIAN_PHONE_NUMBER) { it.copy(guardianPhoneNumber = value) }

    fun consumeSaved() = _uiState.update { it.copy(isSaved = false) }

    private fun edit(field: String, change: (StudentProfileUiState) -> StudentProfileUiState) {
        _uiState.update { change(it).copy(formErrors = it.formErrors - field, successMessage = null) }
    }
}
