package com.example.bursaryaid.feature.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.UserRole
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

    init { loadAdminProfile() }

    private fun loadAdminProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val user = userRepository.getUserById(adminUserId)
                if (user == null || user.userRole != UserRole.ADMIN) {
                    errorMessage = "Administrator account could not be found."
                } else {
                    fullName = user.fullName
                    email = user.email
                    phoneNumber = user.phoneNumber
                }
            } catch (e: Exception) {
                errorMessage = "Could not load administrator profile."
            } finally {
                isLoading = false
            }
        }
    }

    fun onFullNameChanged(value: String) { fullName = value; clearMessages() }
    fun onEmailChanged(value: String) { email = value; clearMessages() }
    fun onPhoneNumberChanged(value: String) { phoneNumber = value; clearMessages() }

    private fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun saveProfile() {
        if (isLoading) return
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phoneNumber.trim()

        if (cleanName.isBlank() || cleanEmail.isBlank()) {
            errorMessage = "Administrator name and email are required."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "Enter a valid email address."
            return
        }

        viewModelScope.launch {
            isLoading = true
            clearMessages()
            try {
                val currentUser = userRepository.getUserById(adminUserId)
                if (currentUser == null || currentUser.userRole != UserRole.ADMIN) {
                    errorMessage = "Administrator account could not be found. Sign in again."
                    return@launch
                }
                val accountUsingEmail = userRepository.getUserByEmail(cleanEmail)
                if (accountUsingEmail != null && accountUsingEmail.userId != adminUserId) {
                    errorMessage = "That email address is already used by another account."
                    return@launch
                }
                val updatedRows = userRepository.updateUser(
                    currentUser.copy(fullName = cleanName, email = cleanEmail, phoneNumber = cleanPhone)
                )
                if (updatedRows == 0) {
                    errorMessage = "No administrator profile was updated. Please try again."
                    return@launch
                }
                fullName = cleanName
                email = cleanEmail
                phoneNumber = cleanPhone
                successMessage = "Administrator profile updated successfully."
                isSaved = true
            } catch (e: Exception) {
                errorMessage = "Failed to save administrator profile. Please try again."
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