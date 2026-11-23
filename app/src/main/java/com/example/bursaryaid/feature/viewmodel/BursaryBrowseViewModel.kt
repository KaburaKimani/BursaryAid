
package com.example.bursaryaid.feature.bursary.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.repository.BursaryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BursaryBrowseViewModel(
    private val bursaryRepository: BursaryRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var allBursaries by mutableStateOf<List<Bursary>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var observeJob: Job? = null

    init {
        observeBursaries()
    }

    private fun observeBursaries() {

        observeJob?.cancel()

        observeJob = viewModelScope.launch {

            isLoading = true
            errorMessage = null

            try {

                bursaryRepository.getOpenBursaries().collect { bursaries ->

                    allBursaries = bursaries
                    isLoading = false
                    errorMessage = null
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                isLoading = false

                errorMessage =
                    "Unable to load bursaries. Please try again."
            }
        }
    }

    val filteredBursaries: List<Bursary>
        get() {

            if (searchQuery.isBlank()) {
                return allBursaries
            }

            return allBursaries.filter { bursary ->

                bursary.bursaryName.contains(
                    searchQuery,
                    ignoreCase = true
                ) ||

                        bursary.fundingOrganization.contains(
                            searchQuery,
                            ignoreCase = true
                        )
            }
        }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun getBursaryById(bursaryId: Long): Bursary? =
        allBursaries.firstOrNull {
            it.bursaryId == bursaryId
        }

    fun retryLoading() {
        observeBursaries()
    }
}
