package com.example.bursaryaid.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun LoginScreen(
    onLoginSuccess: (userId: Long, isAdmin: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bursary Aid", style = MaterialTheme.typography.headlineLarge)
        Text("Log in to continue", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        AppTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = "Email"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = "Password",
            isPassword = true
        )

        ErrorMessage(errorMessage)

        Spacer(Modifier.height(12.dp))
        AppButton(
            text = "Log in",
            onClick = {
                val error = AuthValidator.validateLogin(email, password)
                if (error != null) {
                    errorMessage = error
                } else {
                    // TEMPORARY fake login until Member 3's database is ready.
                    // Any email containing "admin" logs in as admin.
                    val isAdmin = email.contains("admin", ignoreCase = true)
                    onLoginSuccess(101L, isAdmin)
                }
            }
        )

        TextButton(onClick = onNavigateToRegister) {
            Text("No account? Register")
        }
    }
}