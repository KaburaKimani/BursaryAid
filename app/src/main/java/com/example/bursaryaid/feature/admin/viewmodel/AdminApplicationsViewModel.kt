package com.example.bursaryaid.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.feature.admin.data.AdminReviewRepository
import com.example.bursaryaid.feature.admin.logic.AdminApplicationFilter
import com.example.bursaryaid.feature.admin.logic.AdminApplicationItem
import com.example.bursaryaid.model.ApplicationStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminApplicationsUiState(
    /** Applications currently displayed: drafts hidden, filtered, searched, newest first. */
    val applications: List<AdminApplicationItem> = emptyList(),
    /** All reviewable applications (drafts excluded), ignoring search and filter. */
    val totalApplicationCount: Int = 0,
    val searchQuery: String = "",
    val selectedFilter: ApplicationStatus? = null,
    val selectedApplicationId: Long? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AdminApplicationsViewModel(
    private val adminReviewRepository: AdminReviewRepository,
    private val bursaryId: Long? = null
) : ViewModel() {

    private val baseState = MutableStateFlow(AdminApplicationsUiState())
    private val allApplications = MutableStateFlow<List<AdminApplicationItem>>(emptyList())
    private var loadJob: Job? = null

    /** Recalculated whenever the database list, the search or the filter changes. */
    val uiState: StateFlow<AdminApplicationsUiState> =
        combine(baseState, allApplications) { state, all ->
            val scopedApplications = all.filter { item ->
                bursaryId == null || item.bursaryId == bursaryId
            }
            state.copy(
                applications = AdminApplicationFilter.apply(
                    scopedApplications,
                    state.searchQuery,
                    state.selectedFilter
                ),
                totalApplicationCount = scopedApplications.count {
                    it.applicationStatus != ApplicationStatus.DRAFT
                }
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AdminApplicationsUiState())

    init {
        loadApplications()
    }

    /** READ: starts observing the applications. Safe to call again as a "retry". */
    fun loadApplications() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            baseState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                adminReviewRepository.observeApplications().collect { list ->
                    allApplications.value = list
                    baseState.update { it.copy(isLoading = false, errorMessage = null) }
                }
            } catch (e: CancellationException) {
                throw e // cancelling a coroutine is normal, never an error to show
            } catch (e: Exception) {
                baseState.update {
                    it.copy(isLoading = false, errorMessage = "Could not load applications. Please try again.")
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) =
        baseState.update { it.copy(searchQuery = query) }

    fun onFilterSelected(filter: ApplicationStatus?) =
        baseState.update { it.copy(selectedFilter = filter) }

    fun onApplicationSelected(applicationId: Long?) =
        baseState.update { it.copy(selectedApplicationId = applicationId) }

    fun clearMessages() =
        baseState.update { it.copy(errorMessage = null) }
}