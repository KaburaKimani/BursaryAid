package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.bursaryaid.ui.components.LoadingState

/** Connected screen: reads state from the ViewModel and forwards the user's actions to it. */
@Composable
fun AdminApplicationsScreen(
    viewModel: AdminApplicationsViewModel,
    onOpenApplication: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    AdminApplicationsContent(
        state = state,
        onSearchChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onRetry = viewModel::loadApplications,
        onOpenApplication = onOpenApplication,
        modifier = modifier
    )
}

/** Stateless screen: draws exactly what it is given and reports taps upward. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationsContent(
    state: AdminApplicationsUiState,
    onSearchChanged: (String) -> Unit,
    onFilterSelected: (ApplicationStatus?) -> Unit,
    onRetry: () -> Unit,
    onOpenApplication: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Applications", style = MaterialTheme.typography.headlineSmall)

        AppTextField(
            value = state.searchQuery,
            onValueChange = onSearchChanged,
            label = "Search name, admission no. or bursary",
            modifier = Modifier.padding(top = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.selectedFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text("All") }
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

        when {
            state.isLoading -> LoadingState(Modifier.weight(1f))

            state.errorMessage != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                ErrorMessage(state.errorMessage)
                AppButton(text = "Try again", onClick = onRetry)
            }

            state.applications.isEmpty() -> EmptyState(
                message = if (state.totalApplicationCount == 0) {
                    "No applications have been submitted yet."
                } else {
                    "No applications match your search or filter."
                },
                modifier = Modifier.weight(1f)
            )

            else -> {
                Text(
                    text = "Showing ${state.applications.size} of ${state.totalApplicationCount}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.applications, key = { it.applicationId }) { application ->
                        AdminApplicationCard(
                            item = application,
                            onClick = { onOpenApplication(application.applicationId) }
                        )
                    }
                }
            }
        }
    }
}