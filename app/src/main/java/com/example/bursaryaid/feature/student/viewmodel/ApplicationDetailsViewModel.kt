package com.example.bursaryaid.feature.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.data.model.ApplicationRepository
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.data.model.BursaryRepository
import com.example.bursaryaid.data.model.StudentProfileRepository
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApplicationDetailsUiState(
    val selectedApplicationId: Long,
    val application: BursaryApplication? = null,
    val bursaryName: String? = null,
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    /** One-shot signal: the draft was deleted and the screen should navigate back. */
    val isDeleted: Boolean = false
)

class ApplicationDetailsViewModel(
    private val userId: Long,
    selectedApplicationId: Long,
    private val studentProfileRepository: StudentProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val bursaryRepository: BursaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationDetailsUiState(selectedApplicationId = selectedApplicationId))
    val uiState: StateFlow<ApplicationDetailsUiState> = _uiState.asStateFlow()

    init {
        getApplicationById(selectedApplicationId)
    }

    /** READ one application; refuses to show applications that belong to another student. */
    fun getApplicationById(applicationId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profile = studentProfileRepository.getStudentProfile(userId)
                val application = applicationRepository.getApplicationById(applicationId)
                if (profile == null || application == null || application.studentId != profile.studentId) {
                    _uiState.update { it.copy(isLoading = false, application = null, errorMessage = "This application could not be found.") }
                    return@launch
                }
                val bursaryName = try {
                    bursaryRepository.getOpenBursaries().first().firstOrNull { it.bursaryId == application.bursaryId }?.bursaryName
                } catch (e: Exception) {
                    e.rethrowIfCancellation()
                    null
                }
                _uiState.update {
                    it.copy(
                        application = application,
                        bursaryName = bursaryName,
                        canEdit = ApplicationRules.canEdit(application.applicationStatus),
                        canDelete = ApplicationRules.canDelete(application.applicationStatus),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load this application. Please try again.") }
            }
        }
    }

    /** DELETE this application if it is a draft. */
    fun deleteApplication(applicationId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val studentId = studentProfileRepository.getStudentProfile(userId)?.studentId
                val error = if (studentId == null) "Complete your student profile first."
                else deleteDraftApplication(applicationRepository, studentId, applicationId)
                _uiState.update {
                    if (error == null) it.copy(isLoading = false, successMessage = "Draft deleted.", isDeleted = true)
                    else it.copy(isLoading = false, errorMessage = error)
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not delete the draft. Please try again.") }
            }
        }
    }

    fun consumeDeleted() = _uiState.update { it.copy(isDeleted = false) }
}
