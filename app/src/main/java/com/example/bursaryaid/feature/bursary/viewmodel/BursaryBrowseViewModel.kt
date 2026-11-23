package com.example.bursaryaid.feature.bursary.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.repository.BursaryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BursaryBrowseViewModel(
    private val bursaryRepository: BursaryRepository
) : ViewModel() {

    var allBursaries by mutableStateOf<List<Bursary>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val filteredBursaries: List<Bursary>
        get() {
            val query = searchQuery.trim()
            if (query.isEmpty()) return allBursaries
            return allBursaries.filter { bursary ->
                bursary.bursaryName.contains(query, ignoreCase = true) ||
                bursary.fundingOrganization.contains(query, ignoreCase = true) ||
                bursary.bursaryDescription.contains(query, ignoreCase = true)
            }
        }

    init {
        loadBursaries()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun retryLoading() {
        loadBursaries()
    }

    fun getBursaryById(bursaryId: Long): Bursary? {
        return allBursaries.find { it.bursaryId == bursaryId }
    }

    private fun loadBursaries() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            bursaryRepository.getOpenBursaries()
                .catch { e ->
                    isLoading = false
                    errorMessage = e.message ?: "Failed to load bursary opportunities."
                }
                .collectLatest { list ->
                    allBursaries = list
                    isLoading = false
                    errorMessage = null
                }
        }
    }
}
