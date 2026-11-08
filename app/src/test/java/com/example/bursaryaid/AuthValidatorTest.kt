package com.example.bursaryaid

import com.example.bursaryaid.feature.auth.logic.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun login_validInput_returnsNull() {
        assertNull(AuthValidator.validateLogin("student@strath.ac.ke", "secret123"))
    }

    @Test
    fun login_emailWithoutAt_returnsError() {
        assertEquals("Enter a valid email address", AuthValidator.validateLogin("studentmail.com", "secret123"))
    }

    @Test
    fun login_shortPassword_returnsError() {
        assertEquals(
            "Password must be at least 6 characters",
            AuthValidator.validateLogin("student@strath.ac.ke", "123")
        )
    }

    @Test
    fun register_validInput_returnsNull() {
        assertNull(
            AuthValidator.validateRegistration("Tracy", "t@strath.ac.ke", "0712345678", "secret123", "secret123")
        )
    }

    @Test
    fun register_blankName_returnsError() {
        assertEquals(
            "Enter your full name",
            AuthValidator.validateRegistration("  ", "t@strath.ac.ke", "0712345678", "secret123", "secret123")
        )
    }

    @Test
    fun register_passwordMismatch_returnsError() {
        assertEquals(
            "Passwords do not match",
            AuthValidator.validateRegistration("Tracy", "t@strath.ac.ke", "0712345678", "secret123", "different")
        )
    }

    @Test
    fun register_shortPhone_returnsError() {
        assertEquals(
            "Enter a valid phone number",
            AuthValidator.validateRegistration("Tracy", "t@strath.ac.ke", "0712", "secret123", "secret123")
        )
    }
}