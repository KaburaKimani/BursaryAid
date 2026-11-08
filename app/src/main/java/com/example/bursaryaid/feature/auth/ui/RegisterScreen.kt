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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.auth.viewmodel.AuthViewModel
import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.ErrorMessage

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearMessages() }

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
            onValueChange = { fullName = it; viewModel.clearMessages() },
            label = "Full name"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = "Email"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it; viewModel.clearMessages() },
            label = "Phone number"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessages() },
            label = "Password",
            isPassword = true
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; viewModel.clearMessages() },
            label = "Confirm password",
            isPassword = true
        )

        ErrorMessage(viewModel.errorMessage)

        Spacer(Modifier.height(12.dp))
        AppButton(
            text = "Register",
            isLoading = viewModel.isLoading,
            onClick = {
                if (viewModel.registerUser(fullName, email, phoneNumber, password, confirmPassword)) {
                    onRegisterSuccess()
                }
            }
        )

        TextButton(onClick = {
            viewModel.clearMessages()
            onNavigateToLogin()
        }) {
            Text("Already have an account? Log in")
        }
    }
}