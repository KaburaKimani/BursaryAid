package com.example.bursaryaid.feature.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.BursaryApplication
import com.example.bursaryaid.repository.BursaryApplicationRepository
import com.example.bursaryaid.repository.BursaryRepository
import com.example.bursaryaid.repository.StudentProfileRepository
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val isDeleted: Boolean = false
)

class ApplicationDetailsViewModel(
    private val userId: Long,
    selectedApplicationId: Long,
    private val studentProfileRepository: StudentProfileRepository,
    private val applicationRepository: BursaryApplicationRepository,
    private val bursaryRepository: BursaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationDetailsUiState(selectedApplicationId = selectedApplicationId))
    val uiState: StateFlow<ApplicationDetailsUiState> = _uiState.asStateFlow()
    private var observationJob: Job? = null

    init {
        getApplicationById(selectedApplicationId)
    }

    /**
     * Observe the application and bursary tables rather than taking a one-time snapshot.
     * This keeps the student's detail screen in sync when an administrator changes a status,
     * or when the bursary is renamed/closed while this screen is open.
     */
    fun getApplicationById(applicationId: Long) {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profile = studentProfileRepository.getStudentProfile(userId)
                if (profile == null) {
                    _uiState.update {
                        it.copy(isLoading = false, application = null, errorMessage = "This application could not be found.")
                    }
                    return@launch
                }

                combine(
                    applicationRepository.observeApplicationById(applicationId),
                    bursaryRepository.getAllBursaries()
                ) { application, bursaries ->
                    application to bursaries
                }.collect { (application, bursaries) ->
                    if (application == null || application.studentId != profile.studentId) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                application = null,
                                canEdit = false,
                                canDelete = false,
                                errorMessage = "This application could not be found."
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                application = application,
                                bursaryName = bursaries.firstOrNull { bursary ->
                                    bursary.bursaryId == application.bursaryId
                                }?.bursaryName ?: "Unknown bursary",
                                canEdit = ApplicationRules.canEdit(application.applicationStatus),
                                canDelete = ApplicationRules.canDelete(application.applicationStatus),
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Could not load this application. Please try again.")
                }
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
