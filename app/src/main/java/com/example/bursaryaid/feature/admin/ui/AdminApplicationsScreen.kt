package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.admin.logic.AdminApplicationFilter
import com.example.bursaryaid.feature.admin.viewmodel.AdminApplicationsUiState
import com.example.bursaryaid.feature.admin.viewmodel.AdminApplicationsViewModel
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.EmptyState
import com.example.bursaryaid.ui.components.ErrorMessage

@Composable
fun AdminApplicationsScreen(
    viewModel: AdminApplicationsViewModel,
    onOpenApplication: (applicationId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    AdminApplicationsContent(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onOpenApplication = onOpenApplication,
        onRetry = viewModel::loadApplications,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationsContent(
    state: AdminApplicationsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (ApplicationStatus?) -> Unit,
    onOpenApplication: (applicationId: Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Applications") }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            AppTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                label = "Search by student name, admission no, or bursary"
            )

            Spacer(Modifier.height(12.dp))

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.selectedFilter == null,
                        onClick = { onFilterSelected(null) },
                        label = { Text("All (${state.totalApplicationCount})") }
                    )
                }
                items(AdminApplicationFilter.FILTERABLE_STATUSES) { status ->
                    FilterChip(
                        selected = state.selectedFilter == status,
                        onClick = { onFilterSelected(status) },
                        label = { Text(formatStatusLabel(status)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                state.errorMessage != null && state.applications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        ErrorMessage(state.errorMessage)
                        Spacer(Modifier.height(8.dp))
                        AppButton(text = "Try again", onClick = onRetry)
                    }
                }

                state.applications.isEmpty() -> {
                    EmptyState(
                        message = if (state.searchQuery.isBlank() && state.selectedFilter == null) {
                            "No applications to review."
                        } else {
                            "No applications match your search or filter."
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.applications, key = { it.applicationId }) { item ->
                            AdminApplicationCard(
                                item = item,
                                onClick = { onOpenApplication(item.applicationId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
