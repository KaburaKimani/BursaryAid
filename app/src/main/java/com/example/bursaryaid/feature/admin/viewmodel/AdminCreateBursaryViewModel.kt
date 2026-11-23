package com.example.bursaryaid.feature.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.repository.BursaryRepository
import kotlinx.coroutines.launch

class AdminCreateBursaryViewModel(
    private val bursaryRepository: BursaryRepository
) : ViewModel() {

    var bursaryName by mutableStateOf("")
        private set
    var bursaryDescription by mutableStateOf("")
        private set
    var fundingOrganization by mutableStateOf("")
        private set
    var totalBudgetKesInput by mutableStateOf("")
        private set
    var maximumAwardKesInput by mutableStateOf("")
        private set
    var deadlineDaysInput by mutableStateOf("30")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var isCreated by mutableStateOf(false)
        private set

    fun onBursaryNameChanged(value: String) { bursaryName = value; errorMessage = null }
    fun onBursaryDescriptionChanged(value: String) { bursaryDescription = value; errorMessage = null }
    fun onFundingOrganizationChanged(value: String) { fundingOrganization = value; errorMessage = null }
    fun onTotalBudgetChanged(value: String) { totalBudgetKesInput = value; errorMessage = null }
    fun onMaximumAwardChanged(value: String) { maximumAwardKesInput = value; errorMessage = null }
    fun onDeadlineDaysChanged(value: String) { deadlineDaysInput = value; errorMessage = null }

    fun createBursary() {
        val name = bursaryName.trim()
        val desc = bursaryDescription.trim()
        val sponsor = fundingOrganization.trim()
        val budget = totalBudgetKesInput.toLongOrNull() ?: 0L
        val maxAward = maximumAwardKesInput.toLongOrNull() ?: 0L
        val days = deadlineDaysInput.toLongOrNull() ?: 30L

        if (name.isBlank() || desc.isBlank() || budget <= 0L || maxAward <= 0L) {
            errorMessage = "Please fill in all required fields with valid positive numbers for budget and max award."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val deadlineMillis = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
                val bursary = Bursary(
                    bursaryName = name,
                    bursaryDescription = desc,
                    fundingOrganization = sponsor.ifBlank { "Government / Sponsor" },
                    totalBudgetKes = budget,
                    minimumIncomeKes = 0L,
                    maximumAwardKes = maxAward,
                    applicationDeadlineMillis = deadlineMillis,
                    isBursaryOpen = true
                )
                bursaryRepository.insertBursary(bursary)
                successMessage = "Bursary opportunity published successfully!"
                isCreated = true
            } catch (e: Exception) {
                errorMessage = "Failed to publish bursary opportunity. Please try again."
            } finally {
                isLoading = false
            }
        }
    }

    fun consumeCreated() {
        isCreated = false
    }
}

fun adminCreateBursaryViewModelFactory(
    bursaryRepository: BursaryRepository
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminCreateBursaryViewModel::class.java)) {
            return AdminCreateBursaryViewModel(bursaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
