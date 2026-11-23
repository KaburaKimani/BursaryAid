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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.admin.viewmodel.AdminCreateBursaryViewModel
import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.ErrorMessage
import com.example.bursaryaid.ui.components.SuccessMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateBursaryScreen(
    viewModel: AdminCreateBursaryViewModel,
    onBursaryCreated: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel

    // Pop-up confirming the publish. The admin leaves the page only after pressing OK.
    if (state.isCreated) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Bursary published") },
            text = {
                Text(
                    "You have successfully published \"${state.bursaryName}\". " +
                            "Students can now see it and apply until the deadline."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeCreated()
                    onBursaryCreated()
                }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publish Bursary Opportunity") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            ErrorMessage(state.errorMessage)
            SuccessMessage(state.successMessage)

            Text("Bursary Details", style = MaterialTheme.typography.titleMedium)
            AppTextField(
                value = state.bursaryName,
                onValueChange = viewModel::onBursaryNameChanged,
                label = "Bursary Title / Name (e.g. National Higher Education Bursary)"
            )
            AppTextField(
                value = state.bursaryDescription,
                onValueChange = viewModel::onBursaryDescriptionChanged,
                label = "Description & Eligibility Summary"
            )
            AppTextField(
                value = state.fundingOrganization,
                onValueChange = viewModel::onFundingOrganizationChanged,
                label = "Sponsor / Organization Name (e.g. Ministry of Education)"
            )

            Spacer(Modifier.height(4.dp))
            Text("Financial Allocation", style = MaterialTheme.typography.titleMedium)
            AppTextField(
                value = state.totalBudgetKesInput,
                onValueChange = viewModel::onTotalBudgetChanged,
                label = "Total Budget (KES)"
            )
            AppTextField(
                value = state.maximumAwardKesInput,
                onValueChange = viewModel::onMaximumAwardChanged,
                label = "Maximum Award per Student (KES)"
            )
            AppTextField(
                value = state.deadlineDaysInput,
                onValueChange = viewModel::onDeadlineDaysChanged,
                label = "Application Deadline (Days from now)"
            )

            Spacer(Modifier.height(16.dp))
            AppButton(
                text = "Publish Bursary Opportunity",
                isLoading = state.isLoading,
                onClick = viewModel::createBursary
            )
        }
    }
}