package com.example.bursaryaid.feature.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.data.model.ApplicationRepository
import com.example.bursaryaid.data.model.ApplicationStatus
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.data.model.BursaryRepository
import com.example.bursaryaid.data.model.StudentProfileRepository
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyApplicationsUiState(
    val studentId: Long? = null,
    /** Applications currently displayed (already filtered by selectedFilter, newest first). */
    val applications: List<BursaryApplication> = emptyList(),
    val totalApplicationCount: Int = 0,
    /** bursaryId -> bursaryName, for the bursaries known to be open. Missing ids fall back in the UI. */
    val bursaryNames: Map<Long, String> = emptyMap(),
    val selectedFilter: ApplicationStatus? = null,
    val selectedApplicationId: Long? = null,
    val isProfileMissing: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MyApplicationsViewModel(
    private val userId: Long,
    private val studentProfileRepository: StudentProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val bursaryRepository: BursaryRepository
) : ViewModel() {

    private val baseState = MutableStateFlow(MyApplicationsUiState())
    private val allApplications = MutableStateFlow<List<BursaryApplication>>(emptyList())
    private var applicationsJob: Job? = null

    /** The list updates automatically whenever the database changes (Room Flow) or the filter changes. */
    val uiState: StateFlow<MyApplicationsUiState> = combine(baseState, allApplications) { state, all ->
        state.copy(
            applications = ApplicationRules.filterByStatus(all, state.selectedFilter),
            totalApplicationCount = all.size
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MyApplicationsUiState())

    init {
        loadStudentAndApplications()
    }

    fun loadStudentAndApplications() {
        viewModelScope.launch {
            baseState.update { it.copy(isLoading = true, errorMessage = null, isProfileMissing = false) }
            try {
                val profile = studentProfileRepository.getStudentProfile(userId)
                if (profile == null) {
                    baseState.update { it.copy(isLoading = false, isProfileMissing = true) }
                    return@launch
                }
                val names = try {
                    bursaryRepository.getOpenBursaries().first().associate { it.bursaryId to it.bursaryName }
                } catch (e: Exception) {
                    e.rethrowIfCancellation()
                    emptyMap() // names are cosmetic; the list still works with fallback labels
                }
                baseState.update { it.copy(studentId = profile.studentId, bursaryNames = names) }
                getApplicationsByStudent(profile.studentId)
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                baseState.update { it.copy(isLoading = false, errorMessage = "Could not load your applications. Please try again.") }
            }
        }
    }

    /** READ: starts observing this student's applications. */
    fun getApplicationsByStudent(studentId: Long) {
        applicationsJob?.cancel()
        applicationsJob = viewModelScope.launch {
            try {
                applicationRepository.getApplicationsByStudent(studentId).collect { list ->
                    allApplications.value = list
                    baseState.update { it.copy(isLoading = false, errorMessage = null) }
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                baseState.update { it.copy(isLoading = false, errorMessage = "Could not load your applications. Please try again.") }
            }
        }
    }

    fun onFilterSelected(selectedFilter: ApplicationStatus?) =
        baseState.update { it.copy(selectedFilter = selectedFilter) }

    fun onApplicationSelected(selectedApplicationId: Long?) =
        baseState.update { it.copy(selectedApplicationId = selectedApplicationId) }

    /** DELETE a draft. Non-drafts are refused (see ApplicationRules.canDelete). */
    fun deleteApplication(applicationId: Long) {
        val studentId = baseState.value.studentId ?: return
        viewModelScope.launch {
            baseState.update { it.copy(errorMessage = null, successMessage = null) }
            try {
                val error = deleteDraftApplication(applicationRepository, studentId, applicationId)
                baseState.update {
                    if (error == null) it.copy(successMessage = "Draft deleted.") else it.copy(errorMessage = error)
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                baseState.update { it.copy(errorMessage = "Could not delete the draft. Please try again.") }
            }
        }
    }

    fun clearMessages() = baseState.update { it.copy(errorMessage = null, successMessage = null) }
}
