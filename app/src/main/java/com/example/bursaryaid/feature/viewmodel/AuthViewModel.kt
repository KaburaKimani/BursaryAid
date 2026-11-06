package com.example.bursaryaid.feature.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bursaryaid.feature.auth.logic.AuthValidator

class AuthViewModel : ViewModel() {

    var isLoggedIn by mutableStateOf(false)
        private set
    var isAdmin by mutableStateOf(false)
        private set
    var currentUserId by mutableLongStateOf(0L)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    /** Returns true if login succeeded. */
    fun loginUser(email: String, password: String): Boolean {
        val error = AuthValidator.validateLogin(email, password)
        if (error != null) {
            errorMessage = error
            return false
        }
        // TEMPORARY fake login until Member 3's repository is ready.
        // Any email containing "admin" logs in as admin.
        isAdmin = email.contains("admin", ignoreCase = true)
        currentUserId = 101L
        isLoggedIn = true
        errorMessage = null
        successMessage = null
        return true
    }

    /** Returns true if registration succeeded. */
    fun registerUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val error = AuthValidator.validateRegistration(
            fullName, email, phoneNumber, password, confirmPassword
        )
        if (error != null) {
            errorMessage = error
            return false
        }
        // TEMPORARY: saving the account comes when Member 3's repository is ready
        errorMessage = null
        successMessage = "Account created. Please log in."
        return true
    }

    fun logoutUser() {
        isLoggedIn = false
        isAdmin = false
        currentUserId = 0L
        errorMessage = null
        successMessage = null
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}