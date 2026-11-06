package com.example.bursaryaid.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.auth.logic.AuthValidator
import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.ErrorMessage

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        AppTextField(
            value = fullName,
            onValueChange = { fullName = it; errorMessage = null },
            label = "Full name"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = "Email"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it; errorMessage = null },
            label = "Phone number"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = "Password",
            isPassword = true
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            label = "Confirm password",
            isPassword = true
        )

        ErrorMessage(errorMessage)

        Spacer(Modifier.height(12.dp))
        AppButton(
            text = "Register",
            onClick = {
                val error = AuthValidator.validateRegistration(
                    fullName, email, phoneNumber, password, confirmPassword
                )
                if (error != null) {
                    errorMessage = error
                } else {
                    // TEMPORARY: saving to the database comes when Member 3's repository is ready
                    onRegisterSuccess()
                }
            }
        )

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in")
        }
    }
}