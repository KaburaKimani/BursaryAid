package com.example.bursaryaid.feature.student.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.data.model.ApplicationStatus
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import com.example.bursaryaid.feature.student.viewmodel.MyApplicationsViewModel

/**
 * "My Applications" list with status filter, loading/empty/error states and draft deletion.
 * Navigation is injected so Member 1 keeps ownership of route names.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    viewModel: MyApplicationsViewModel,
    onNewApplication: () -> Unit,
    onApplicationClick: (applicationId: Long) -> Unit,
    onCompleteProfile: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var applicationPendingDeletion by remember { mutableStateOf<BursaryApplication?>(null) }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        val message = state.successMessage ?: state.errorMessage.takeIf { state.applications.isNotEmpty() }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    applicationPendingDeletion?.let { application ->
        AlertDialog(
            onDismissRequest = { applicationPendingDeletion = null },
            title = { Text("Delete this draft?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteApplication(application.applicationId)
                    applicationPendingDeletion = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { applicationPendingDeletion = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My applications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isProfileMissing) {
                FloatingActionButton(onClick = onNewApplication) {
                    Icon(Icons.Default.Add, contentDescription = "New application")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            when {
                state.isProfileMissing -> CenteredMessage(
                    title = "Complete your student profile first",
                    body = "You need a profile before you can apply for a bursary.",
                    actionLabel = "Create profile",
                    onAction = onCompleteProfile
                )

                state.errorMessage != null && state.totalApplicationCount == 0 && !state.isLoading -> CenteredMessage(
                    title = "Something went wrong",
                    body = state.errorMessage.orEmpty(),
                    actionLabel = "Try again",
                    onAction = viewModel::loadStudentAndApplications
                )

                !state.isLoading && state.totalApplicationCount == 0 -> CenteredMessage(
                    title = "No applications yet",
                    body = "Start a bursary application and it will appear here.",
                    actionLabel = "New application",
                    onAction = onNewApplication
                )

                else -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedFilter == null,
                                onClick = { viewModel.onFilterSelected(null) },
                                label = { Text("All") }
                            )
                        }
                        items(ApplicationStatus.values().toList()) { status ->
                            FilterChip(
                                selected = state.selectedFilter == status,
                                onClick = { viewModel.onFilterSelected(status) },
                                label = { Text(status.displayName()) }
                            )
                        }
                    }

                    if (state.applications.isEmpty() && !state.isLoading) {
                        CenteredMessage(
                            title = "Nothing with this status",
                            body = "Try a different filter.",
                            actionLabel = "Show all",
                            onAction = { viewModel.onFilterSelected(null) }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.applications, key = { it.applicationId }) { application ->
                                ApplicationCard(
                                    application = application,
                                    bursaryName = state.bursaryNames[application.bursaryId]
                                        ?: "Bursary #${application.bursaryId}",
                                    onClick = {
                                        viewModel.onApplicationSelected(application.applicationId)
                                        onApplicationClick(application.applicationId)
                                    },
                                    onDeleteClick = { applicationPendingDeletion = application }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    application: BursaryApplication,
    bursaryName: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(bursaryName, style = MaterialTheme.typography.titleMedium)
                Text("${application.academicYear.ifBlank { "Academic year not set" }} - ${formatKes(application.requestedAmountKes)}")
                Text(
                    text = application.submittedAtMillis?.let { "Submitted ${formatDateTime(it)}" }
                        ?: "Not submitted - last edited ${formatDateTime(application.updatedAtMillis)}",
                    style = MaterialTheme.typography.bodySmall
                )
                StudentStatusChip(application.applicationStatus)
            }
            if (ApplicationRules.canDelete(application.applicationStatus)) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete draft")
                }
            }
        }
    }
}

@Composable
internal fun CenteredMessage(title: String, body: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}
