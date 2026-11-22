package com.example.bursaryaid.feature.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.feature.auth.logic.AuthValidator
import com.example.bursaryaid.feature.auth.logic.PasswordHasher
import com.example.bursaryaid.model.UserAccount
import com.example.bursaryaid.model.UserRole
import com.example.bursaryaid.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

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

    init {
        seedDefaultAdmin()
    }

    /** Makes sure one administrator account exists so admin features can be tested. */
    private fun seedDefaultAdmin() {
        viewModelScope.launch {
            if (userRepository.getUserByEmail(DEFAULT_ADMIN_EMAIL) == null) {
                userRepository.insertUser(
                    UserAccount(
                        fullName = "System Administrator",
                        email = DEFAULT_ADMIN_EMAIL,
                        phoneNumber = "0700000000",
                        passwordHash = PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD),
                        userRole = UserRole.ADMIN,
                        isAccountActive = true,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun loginUser(email: String, password: String, onSuccess: (isAdmin: Boolean) -> Unit) {
        val validationError = AuthValidator.validateLogin(email, password)
        if (validationError != null) {
            errorMessage = validationError
            return
        }
        isLoading = true
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email.trim().lowercase())
            when {
                user == null || !PasswordHasher.verify(password, user.passwordHash) ->
                    errorMessage = "Incorrect email or password"
                !user.isAccountActive ->
                    errorMessage = "This account has been deactivated"
                else -> {
                    isAdmin = user.userRole == UserRole.ADMIN
                    currentUserId = user.userId
                    isLoggedIn = true
                    errorMessage = null
                    successMessage = null
                    onSuccess(isAdmin)
                }
            }
            isLoading = false
        }
    }

    fun registerUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val validationError = AuthValidator.validateRegistration(
            fullName, email, phoneNumber, password, confirmPassword
        )
        if (validationError != null) {
            errorMessage = validationError
            return
        }
        isLoading = true
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            if (userRepository.getUserByEmail(cleanEmail) != null) {
                errorMessage = "An account with this email already exists"
            } else {
                userRepository.insertUser(
                    UserAccount(
                        fullName = fullName.trim(),
                        email = cleanEmail,
                        phoneNumber = phoneNumber.trim(),
                        passwordHash = PasswordHasher.hash(password),
                        userRole = UserRole.STUDENT,
                        isAccountActive = true,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )
                errorMessage = null
                successMessage = "Account created. Please log in."
                onSuccess()
            }
            isLoading = false
        }
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

    companion object {
        const val DEFAULT_ADMIN_EMAIL = "admin@bursaryaid.com"
        const val DEFAULT_ADMIN_PASSWORD = "Admin@123"
    }
}

class AuthViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(userRepository) as T
}