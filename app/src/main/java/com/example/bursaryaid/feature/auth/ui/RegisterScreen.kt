package com.example.bursaryaid.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
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
    var isAdministrator by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.clearMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Header Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isAdministrator) {
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            } else {
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            }
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.padding(start = 14.dp))

                    Column {
                        Text(
                            text = if (isAdministrator) "Admin Registration" else "Student Registration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Bursary Aid System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Role Selector Chips (Coexisting Student and Admin Registration)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = !isAdministrator,
                onClick = { isAdministrator = false; viewModel.clearMessages() },
                label = { Text("👤 Student Account") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isAdministrator,
                onClick = { isAdministrator = true; viewModel.clearMessages() },
                label = { Text("🛡️ Administrator") },
                modifier = Modifier.weight(1f)
            )
        }

        AppTextField(
            value = fullName,
            onValueChange = { fullName = it; viewModel.clearMessages() },
            label = "Full Name"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = "Email Address"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it; viewModel.clearMessages() },
            label = "Phone Number (e.g. 0712345678)"
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
            label = "Confirm Password",
            isPassword = true
        )

        ErrorMessage(viewModel.errorMessage)

        Spacer(Modifier.height(16.dp))
        AppButton(
            text = if (isAdministrator) "Register as Administrator" else "Register Account",
            isLoading = viewModel.isLoading,
            onClick = {
                viewModel.registerUser(
                    fullName = fullName,
                    email = email,
                    phoneNumber = phoneNumber,
                    password = password,
                    confirmPassword = confirmPassword,
                    asAdmin = isAdministrator
                ) {
                    onRegisterSuccess()
                }
            }
        )

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {
            viewModel.clearMessages()
            onNavigateToLogin()
        }) {
            Text(
                "Already have an account? Log In",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
