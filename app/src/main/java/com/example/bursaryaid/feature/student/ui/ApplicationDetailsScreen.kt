package com.example.bursaryaid.feature.student.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.feature.student.viewmodel.ApplicationDetailsViewModel

/**
 * Read-only view of one application with Edit (drafts and information requests) and Delete (drafts only).
 * @param onEditApplication opens ApplicationFormScreen with this applicationId as selectedApplicationId.
 * @param onApplicationDeleted called after a draft is deleted (navigate back to the list).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailsScreen(
    viewModel: ApplicationDetailsViewModel,
    onEditApplication: (applicationId: Long) -> Unit,
    onApplicationDeleted: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            viewModel.consumeDeleted()
            onApplicationDeleted()
        }
    }

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            title = { Text("Delete this draft?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    isDeleteDialogVisible = false
                    viewModel.deleteApplication(state.selectedApplicationId)
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { isDeleteDialogVisible = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val application = state.application
        when {
            state.isLoading && application == null -> StudentLoadingState(Modifier.padding(padding))

            application == null -> Column(Modifier.padding(padding).fillMaxSize()) {
                CenteredMessage(
                    title = "Application unavailable",
                    body = state.errorMessage ?: "This application could not be found.",
                    actionLabel = "Try again",
                    onAction = { viewModel.getApplicationById(state.selectedApplicationId) }
                )
            }

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.errorMessage?.let { StudentMessageBanner(it, isError = true) }
                ApplicationSummary(application, state.bursaryName)

                if (state.canEdit || state.canDelete) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (state.canDelete) {
                            OutlinedButton(
                                onClick = { isDeleteDialogVisible = true },
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) { Text("Delete") }
                        }
                        if (state.canEdit) {
                            Button(
                                onClick = { onEditApplication(application.applicationId) },
                                enabled = !state.isLoading,
                                modifier = Modifier.weight(1f)
                            ) { Text(if (state.canDelete) "Edit draft" else "Update application") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationSummary(application: BursaryApplication, bursaryName: String?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(bursaryName ?: "Bursary #${application.bursaryId}", style = MaterialTheme.typography.titleLarge)
            StudentStatusChip(application.applicationStatus)
            DetailRow("Academic year", application.academicYear.ifBlank { "Not set" })
            DetailRow("Amount requested", formatKes(application.requestedAmountKes))
            DetailRow("Submitted", application.submittedAtMillis?.let(::formatDateTime) ?: "Not submitted yet")
            DetailRow("Last updated", formatDateTime(application.updatedAtMillis))
            DetailRow("Reason", application.applicationReason.ifBlank { "Not provided" })
            DetailRow(
                "Supporting documents",
                if (application.supportingDocuments.isEmpty()) "None attached"
                else application.supportingDocuments.joinToString("\n") { Uri.parse(it).lastPathSegment ?: it }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
