package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.admin.viewmodel.AdminProfileViewModel
import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.ErrorMessage
import com.example.bursaryaid.ui.components.SuccessMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    viewModel: AdminProfileViewModel,
    onProfileSaved: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onProfileSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrator Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            ErrorMessage(state.errorMessage)
            SuccessMessage(state.successMessage)

            Text("Admin Credentials & Contact", style = MaterialTheme.typography.titleMedium)
            AppTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChanged,
                label = "Full Name"
            )
            AppTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = "Email Address"
            )
            AppTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChanged,
                label = "Phone Number"
            )

            Spacer(Modifier.height(16.dp))
            AppButton(
                text = "Update Admin Profile",
                isLoading = state.isLoading,
                onClick = viewModel::saveProfile
            )
        }
    }
}
