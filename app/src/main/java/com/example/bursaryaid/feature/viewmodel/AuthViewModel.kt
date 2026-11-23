package com.example.bursaryaid.feature.auth.viewmodel

import android.database.sqlite.SQLiteConstraintException

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.bursaryaid.BuildConfig
import com.example.bursaryaid.feature.auth.logic.AuthValidator
import com.example.bursaryaid.feature.auth.logic.PasswordHasher
import com.example.bursaryaid.model.UserAccount
import com.example.bursaryaid.model.UserRole
import com.example.bursaryaid.repository.UserRepository

import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoggedIn by mutableStateOf(false)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var currentUserId by mutableLongStateOf(0L)
        private set

    var currentUserName by mutableStateOf("")
        private set

    var currentUserEmail by mutableStateOf("")
        private set

    // Student / Administrator toggle shared by the Login and Register screens,
    // so the role chosen on Login is still selected after tapping "Sign Up".
    @set:JvmName("setAdminModeProperty")
    var isAdminMode by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    init {
        seedDebugAdmin()
    }

    private fun seedDebugAdmin() {

        if (!BuildConfig.DEBUG) return

        val email = BuildConfig.DEFAULT_ADMIN_EMAIL
            .trim()
            .lowercase()

        val password = BuildConfig.DEFAULT_ADMIN_PASSWORD

        if (email.isBlank() || password.isBlank()) return

        viewModelScope.launch {

            try {

                if (!userRepository.emailExists(email)) {

                    userRepository.insertUser(
                        UserAccount(
                            fullName = "System Administrator",
                            email = email,
                            phoneNumber = "",
                            passwordHash = PasswordHasher.hash(password),
                            userRole = UserRole.ADMIN,
                            isAccountActive = true,
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: SQLiteConstraintException) {
                // Account may already have been inserted.
            } catch (e: Exception) {
                errorMessage = "Unable to initialise the demonstration administrator."
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        asAdmin: Boolean = false,
        onSuccess: (isAdmin: Boolean) -> Unit
    ) {

        val validationError =
            AuthValidator.validateLogin(email, password)

        if (validationError != null) {
            errorMessage = validationError
            return
        }

        if (isLoading) return

        isLoading = true
        errorMessage = null

        viewModelScope.launch {

            try {

                val cleanEmail = email.trim().lowercase()

                val user = userRepository.getUserByEmail(cleanEmail)

                when {

                    user == null ||
                            !PasswordHasher.verify(password, user.passwordHash) -> {
                        errorMessage = "Incorrect email or password"
                    }

                    !user.isAccountActive -> {
                        errorMessage = "This account has been deactivated"
                    }

                    // The sign-in tab chosen (Student / Administrator) must match the
                    // role stored on the account, otherwise the user is told which tab to use.
                    asAdmin && user.userRole != UserRole.ADMIN -> {
                        errorMessage =
                            "This is a student account. Switch to Student sign in."
                    }

                    !asAdmin && user.userRole == UserRole.ADMIN -> {
                        errorMessage =
                            "This is an administrator account. Switch to Administrator sign in."
                    }

                    else -> {

                        if (PasswordHasher.needsUpgrade(user.passwordHash)) {

                            userRepository.updateUser(
                                user.copy(
                                    passwordHash = PasswordHasher.hash(password)
                                )
                            )
                        }

                        isAdmin = user.userRole == UserRole.ADMIN
                        currentUserId = user.userId
                        currentUserName = user.fullName
                        currentUserEmail = user.email
                        isLoggedIn = true

                        errorMessage = null
                        successMessage = null

                        onSuccess(isAdmin)
                    }
                }

            } catch (e: Exception) {

                errorMessage = "Login failed. Please try again."

            } finally {

                isLoading = false
            }
        }
    }

    fun registerUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String,
        asAdmin: Boolean = false,
        onSuccess: () -> Unit
    ) {

        val validationError = AuthValidator.validateRegistration(
            fullName,
            email,
            phoneNumber,
            password,
            confirmPassword
        )

        if (validationError != null) {
            errorMessage = validationError
            return
        }

        if (isLoading) return

        isLoading = true
        errorMessage = null

        viewModelScope.launch {

            try {

                val cleanEmail = email.trim().lowercase()

                if (userRepository.emailExists(cleanEmail)) {

                    errorMessage =
                        "An account with this email already exists"

                } else {

                    userRepository.insertUser(
                        UserAccount(
                            fullName = fullName.trim(),
                            email = cleanEmail,
                            phoneNumber = phoneNumber.trim(),
                            passwordHash = PasswordHasher.hash(password),
                            userRole = if (asAdmin) UserRole.ADMIN else UserRole.STUDENT,
                            isAccountActive = true,
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )

                    val registeredUser = userRepository.getUserByEmail(cleanEmail)
                    if (registeredUser != null) {
                        isAdmin = registeredUser.userRole == UserRole.ADMIN
                        currentUserId = registeredUser.userId
                        currentUserName = registeredUser.fullName
                        currentUserEmail = registeredUser.email
                        isLoggedIn = true
                    }

                    errorMessage = null
                    successMessage = "Account created and logged in successfully!"

                    onSuccess()
                }

            } catch (e: SQLiteConstraintException) {

                errorMessage =
                    "An account with this email already exists."

            } catch (e: Exception) {

                errorMessage =
                    "Registration failed. Please try again."

            } finally {

                isLoading = false
            }
        }
    }

    fun logoutUser() {

        isLoggedIn = false
        isAdmin = false
        currentUserId = 0L
        currentUserName = ""
        currentUserEmail = ""

        errorMessage = null
        successMessage = null
    }

    fun setAdminMode(admin: Boolean) {

        isAdminMode = admin
        errorMessage = null
        successMessage = null
    }

    fun clearMessages() {

        errorMessage = null
        successMessage = null
    }
}

class AuthViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}