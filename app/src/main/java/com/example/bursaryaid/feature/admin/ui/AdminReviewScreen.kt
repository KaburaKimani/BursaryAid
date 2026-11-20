package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.admin.logic.AdminApplicationDetail
import com.example.bursaryaid.feature.admin.logic.DecisionForm
import com.example.bursaryaid.feature.admin.logic.DecisionValidator
import com.example.bursaryaid.feature.admin.viewmodel.AdminReviewUiState
import com.example.bursaryaid.feature.admin.viewmodel.AdminReviewViewModel
import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.ErrorMessage
import com.example.bursaryaid.ui.components.LoadingState
import com.example.bursaryaid.ui.components.SuccessMessage

/** Connected screen: reads state from the ViewModel and forwards the user's actions to it. */
@Composable
fun AdminReviewScreen(
    viewModel: AdminReviewViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    AdminReviewContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::loadApplication,
        onStartReview = viewModel::startReview,
        onAmountChanged = viewModel::onAmountChanged,
        onReasonChanged = viewModel::onReasonChanged,
        onInformationMessageChanged = viewModel::onInformationMessageChanged,
        onApprove = viewModel::approve,
        onReject = viewModel::reject,
        onRequestInformation = viewModel::requestInformation,
        modifier = modifier
    )
}

/** Stateless screen: draws exactly what it is given and reports taps upward. */
@Composable
fun AdminReviewContent(
    state: AdminReviewUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onStartReview: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onReasonChanged: (String) -> Unit,
    onInformationMessageChanged: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestInformation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detail = state.detail
    when {
        state.isLoading && detail == null -> LoadingState(modifier)

        state.isApplicationMissing -> Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This application no longer exists.", style = MaterialTheme.typography.bodyLarge)
            AppButton(text = "Back", onClick = onBack, modifier = Modifier.padding(top = 16.dp))
        }

        detail == null -> Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ErrorMessage(state.errorMessage ?: "Could not load the application.")
            AppButton(text = "Try again", onClick = onRetry)
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Back")
            }
        }

        else -> ReviewBody(
            state = state,
            detail = detail,
            onBack = onBack,
            onStartReview = onStartReview,
            onAmountChanged = onAmountChanged,
            onReasonChanged = onReasonChanged,
            onInformationMessageChanged = onInformationMessageChanged,
            onApprove = onApprove,
            onReject = onReject,
            onRequestInformation = onRequestInformation,
            modifier = modifier
        )
    }
}

@Composable
private fun ReviewBody(
    state: AdminReviewUiState,
    detail: AdminApplicationDetail,
    onBack: () -> Unit,
    onStartReview: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onReasonChanged: (String) -> Unit,
    onInformationMessageChanged: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestInformation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Review application",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            StatusBadge(detail.applicationStatus)
        }

        SectionCard("Application") {
            DetailRow("Requested", formatKes(detail.requestedAmountKes))
            DetailRow("Submitted", formatDate(detail.submittedAtMillis))
            Text("Reason for the request", style = MaterialTheme.typography.labelMedium)
            Text(detail.justification, style = MaterialTheme.typography.bodyMedium)
        }

        SectionCard("Student") {
            DetailRow("Name", detail.studentName)
            DetailRow("Admission no.", detail.admissionNumber)
            DetailRow("Institution", detail.institutionName)
            DetailRow("Course", detail.courseName)
            DetailRow("Year of study", detail.yearOfStudy.toString())
            DetailRow("Household income", detail.householdIncomeKes?.let { formatKes(it) } ?: "Not provided")
        }

        SectionCard("Bursary and budget") {
            DetailRow("Bursary", detail.bursaryName)
            DetailRow("Deadline", formatDate(detail.applicationDeadlineMillis))
            DetailRow("Income limit", detail.incomeLimitKes?.let { formatKes(it) } ?: "No limit")
            DetailRow("Maximum award", formatKes(detail.maximumAwardKes))
            DetailRow("Total budget", formatKes(detail.totalBudgetKes))
            DetailRow("Awarded so far", formatKes(detail.approvedAwardsKes.sum()))
            DetailRow("Remaining", formatKes(detail.remainingBudgetKes))
        }

        // The rules describe an application that is still open, so they only make sense here.
        val eligibility = state.eligibility
        if (eligibility != null && (state.canStartReview || state.canDecide)) {
            SectionCard("Eligibility") {
                if (eligibility.isEligible) {
                    Text(
                        text = "Meets all criteria",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Does not meet the criteria:",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    eligibility.failedReasons.forEach { reason ->
                        Text("- $reason", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        ErrorMessage(state.errorMessage)
        SuccessMessage(state.successMessage)

        when {
            state.canStartReview -> SectionCard("Next step") {
                Text("Start the review to make a decision.", style = MaterialTheme.typography.bodyMedium)
                AppButton(text = "Start review", onClick = onStartReview, isLoading = state.isSaving)
            }

            state.canDecide -> {
                SectionCard("Decision") {
                    AppTextField(
                        value = state.awardedAmountText,
                        onValueChange = onAmountChanged,
                        label = "Award amount (KSh)",
                        errorText = state.formErrors[DecisionForm.FIELD_AMOUNT]
                    )
                    AppTextField(
                        value = state.decisionReason,
                        onValueChange = onReasonChanged,
                        label = "Reason (required to reject)",
                        errorText = state.formErrors[DecisionForm.FIELD_REASON]
                    )
                    AppButton(text = "Approve", onClick = onApprove, isLoading = state.isSaving)
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Reject") }
                }
                SectionCard("Request more information") {
                    AppTextField(
                        value = state.informationMessage,
                        onValueChange = onInformationMessageChanged,
                        label = "Message to the student",
                        errorText = state.formErrors[DecisionValidator.FIELD_MESSAGE]
                    )
                    OutlinedButton(
                        onClick = onRequestInformation,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Send request") }
                }
            }

            state.isFinal -> SectionCard("Decision") {
                Text(
                    text = "This application was ${formatStatusLabel(detail.applicationStatus).lowercase()}. " +
                            "Decisions are final.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> SectionCard("Waiting for the student") {
                Text(
                    text = "Information was requested. The application returns to the review queue " +
                            "when the student resubmits.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}