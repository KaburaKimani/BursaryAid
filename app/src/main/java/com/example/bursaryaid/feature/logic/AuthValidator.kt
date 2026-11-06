package com.example.bursaryaid.feature.auth.logic

object AuthValidator {

    private const val MIN_PASSWORD_LENGTH = 6
    private const val MIN_PHONE_LENGTH = 10

    /** Returns an error message, or null if the input is valid. */
    fun validateLogin(email: String, password: String): String? = when {
        !isValidEmail(email) -> "Enter a valid email address"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    /** Returns an error message, or null if the input is valid. */
    fun validateRegistration(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): String? = when {
        fullName.isBlank() -> "Enter your full name"
        !isValidEmail(email) -> "Enter a valid email address"
        phoneNumber.length < MIN_PHONE_LENGTH || !phoneNumber.all { it.isDigit() || it == '+' } ->
            "Enter a valid phone number"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }

    private fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        val atIndex = trimmed.indexOf('@')
        return atIndex > 0 && trimmed.indexOf('.', atIndex) > atIndex + 1 && !trimmed.endsWith(".")
    }
}