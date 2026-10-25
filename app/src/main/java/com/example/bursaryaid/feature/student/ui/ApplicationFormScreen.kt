package com.example.bursaryaid.feature.student.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.data.model.ApplicationStatus
import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.logic.StudentFeatureConstants
import com.example.bursaryaid.feature.student.viewmodel.ApplicationFormViewModel

/**
 * Create / edit-draft / answer-information-request form.
 * @param onApplicationSaved called after a successful save or submit (navigate back / to the list).
 * @param onCompleteProfile called when the student has no profile yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationFormScreen(
    viewModel: ApplicationFormViewModel,
    onApplicationSaved: () -> Unit,
    onCompleteProfile: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isEditing = state.selectedApplicationId != null
    val isAnsweringRequest = state.applicationStatus == ApplicationStatus.NEEDS_INFORMATION

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onApplicationSaved()
        }
    }

    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            // Keep read access after the app restarts; ignore providers that do not support persistence.
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.onSupportingDocumentAdded(uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit application" else "New application") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isProfileMissing -> Column(
                Modifier.padding(padding).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Complete your student profile first", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Your profile tells reviewers about your course and household. You only need to do this once.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onCompleteProfile) { Text("Create profile") }
            }

            state.isLoading && state.studentId == null && state.errorMessage == null -> StudentLoadingState(Modifier.padding(padding))

            state.studentId == null -> Column(
                Modifier.padding(padding).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StudentMessageBanner(state.errorMessage ?: "Could not load the form.", isError = true)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = viewModel::loadFormData) { Text("Try again") }
            }

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.errorMessage?.let { StudentMessageBanner(it, isError = true) }
                state.successMessage?.let { StudentMessageBanner(it, isError = false) }
                if (isAnsweringRequest) {
                    StudentMessageBanner(
                        "Reviewers asked for more information. Update your application and resubmit it.",
                        isError = false
                    )
                }

                // Bursary selector
                var isMenuExpanded by remember { mutableStateOf(false) }
                val selectedBursary = state.bursaries.firstOrNull { it.bursaryId == state.bursaryId }
                val bursaryError = state.formErrors[FormField.BURSARY_ID]
                val canChangeBursary = !isAnsweringRequest && !state.isLoading
                ExposedDropdownMenuBox(
                    expanded = isMenuExpanded,
                    onExpandedChange = { if (canChangeBursary) isMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBursary?.bursaryName ?: state.bursaryId?.let { "Bursary #$it" }.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bursary") },
                        isError = bursaryError != null,
                        supportingText = (bursaryError
                            ?: if (state.bursaries.isEmpty() && !isAnsweringRequest) "No bursaries are open right now." else null)
                            ?.let { { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                        enabled = canChangeBursary,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                        state.bursaries.forEach { bursary ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(bursary.bursaryName)
                                        Text(
                                            "${bursary.fundingOrganization} - up to ${formatKes(bursary.maximumAwardKes)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onBursarySelected(bursary.bursaryId)
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                selectedBursary?.let {
                    Text(
                        "Deadline: ${formatDateTime(it.applicationDeadlineMillis)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                StudentFormTextField(state.academicYear, viewModel::onAcademicYearChanged,
                    "Academic year", state.formErrors[FormField.ACADEMIC_YEAR],
                    enabled = !state.isLoading, helperText = "For example 2026/2027")
                StudentFormTextField(state.requestedAmountKesInput, viewModel::onRequestedAmountKesChanged,
                    "Amount requested (KES)", state.formErrors[FormField.REQUESTED_AMOUNT_KES],
                    keyboardType = KeyboardType.Number, enabled = !state.isLoading)
                StudentFormTextField(state.applicationReason, viewModel::onApplicationReasonChanged,
                    "Why do you need this support?", state.formErrors[FormField.APPLICATION_REASON],
                    singleLine = false, minLines = 4, enabled = !state.isLoading,
                    helperText = "${state.applicationReason.length}/${StudentFeatureConstants.MAX_APPLICATION_REASON_LENGTH}")

                // Supporting documents
                Text("Supporting documents", style = MaterialTheme.typography.titleSmall)
                state.supportingDocuments.forEach { documentUri ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Uri.parse(documentUri).lastPathSegment ?: documentUri,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        IconButton(
                            onClick = { viewModel.onSupportingDocumentRemoved(documentUri) },
                            enabled = !state.isLoading
                        ) { Icon(Icons.Default.Close, contentDescription = "Remove document") }
                    }
                }
                state.formErrors[FormField.SUPPORTING_DOCUMENTS]?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(
                    onClick = { documentPicker.launch(arrayOf("application/pdf", "image/*")) },
                    enabled = !state.isLoading &&
                        state.supportingDocuments.size < StudentFeatureConstants.MAX_SUPPORTING_DOCUMENTS
                ) { Text("Attach a document (PDF or image)") }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = viewModel::saveApplicationDraft,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) { Text(if (isAnsweringRequest) "Save changes" else "Save draft") }
                    Button(
                        onClick = viewModel::submitApplication,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) { Text(if (isAnsweringRequest) "Resubmit" else "Submit") }
                }
            }
        }
    }
}
