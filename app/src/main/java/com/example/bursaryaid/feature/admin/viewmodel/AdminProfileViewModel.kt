package com.example.bursaryaid.feature.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.repository.UserRepository
import kotlinx.coroutines.launch

class AdminProfileViewModel(
    private val adminUserId: Long,
    private val userRepository: UserRepository
) : ViewModel() {

    var fullName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var phoneNumber by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var isSaved by mutableStateOf(false)
        private set

    init {
        loadAdminProfile()
    }

    private fun loadAdminProfile() {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = userRepository.getUserById(adminUserId)
                if (user != null) {
                    fullName = user.fullName
                    email = user.email
                    phoneNumber = user.phoneNumber
                }
            } catch (e: Exception) {
                errorMessage = "Could not load admin profile."
            } finally {
                isLoading = false
            }
        }
    }

    fun onFullNameChanged(value: String) { fullName = value; errorMessage = null }
    fun onEmailChanged(value: String) { email = value; errorMessage = null }
    fun onPhoneNumberChanged(value: String) { phoneNumber = value; errorMessage = null }

    fun saveProfile() {
        if (fullName.isBlank() || email.isBlank()) {
            errorMessage = "Name and email cannot be blank."
            return
        }
        viewModelScope.launch {
            isLoading = true
            try {
                val user = userRepository.getUserById(adminUserId)
                if (user != null) {
                    userRepository.updateUser(
                        user.copy(
                            fullName = fullName.trim(),
                            email = email.trim().lowercase(),
                            phoneNumber = phoneNumber.trim()
                        )
                    )
                    successMessage = "Admin profile updated successfully."
                    isSaved = true
                }
            } catch (e: Exception) {
                errorMessage = "Failed to update profile."
            } finally {
                isLoading = false
            }
        }
    }

    fun consumeSaved() { isSaved = false }
}

fun adminProfileViewModelFactory(
    adminUserId: Long,
    userRepository: UserRepository
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminProfileViewModel::class.java)) {
            return AdminProfileViewModel(adminUserId, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
